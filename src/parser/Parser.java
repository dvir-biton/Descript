package parser;

import lexer.token.Token;
import lexer.token.TokenType;
import parser.nodes.*;
import parser.variables.SymbolTable;

import java.util.ArrayList;
import java.util.List;

public class Parser {
    private final List<Token> tokens;
    private int currentPosition;
    private final ErrorHandler errorHandler;
    private final SymbolTable symbolTable;

    public Parser(
        List<Token> tokens,
        ErrorHandler errorHandler,
        SymbolTable symbolTable
    ) {
        this.tokens = tokens;
        this.errorHandler = errorHandler;
        this.symbolTable = symbolTable;
        currentPosition = 0;
    }

    public ASTNode parse() {
        List<ASTNode> statements = new ArrayList<>();
        while (isNotEOF()) {
            statements.add(parseStatement());
        }
        return new BlockNode(statements);
    }

    private ASTNode parseStatement() {
        Token current = peek();

        // Handle different types of keywords like variable declarations and control structures
        if (current.getType() == TokenType.KEYWORD) {
            switch (current.getValue()) {
                case "Int", "String", "Bool", "Double" -> {
                    return parseVariableDeclaration();
                }
                case "if" -> {
                    return parseIfStatement();
                }
                case "while" -> {
                    return parseWhileStatement();
                }
                case "for" -> {
                    return parseForStatement();
                }
                case "func" -> {
                    return parseFunctionDeclaration();
                }
                default -> errorHandler.logError("Unexpected keyword: " + current.getValue(), current.getLine());
            }
        }

        // Handle statements that start with an identifier (could be function call or variable assignment)
        if (current.getType() == TokenType.IDENTIFIER) {
            return parseIdentifierStatement();
        }

        // Handle generic expressions
        ASTNode expression = parseExpression();
        consume(TokenType.SEMICOLON, "Expected ';' after expression.");
        return expression;
    }

    private ASTNode parseIdentifierStatement() {
        Token identifierToken = consume(TokenType.IDENTIFIER, "Expected variable name or function call.");
        String identifierName = identifierToken.getValue();

        // Check for function call
        if (check(TokenType.LEFT_PARENTHESES)) {
            List<ASTNode> arguments = parseArguments();
            consume(TokenType.SEMICOLON, "Expected ';' after function call.");
            return new FunctionCallNode(identifierName, arguments);
        }

        // Handle reassignment or compound assignment
        if (match(TokenType.ASSIGNMENT_OPERATOR)) {
            ASTNode newValue = parseExpression();
            consume(TokenType.SEMICOLON, "Expected ';' after reassignment.");
            return new VariableAssignmentNode(identifierName, newValue);
        } else if (matchCompoundAssignment()) {
            String compoundOp = advance().getValue();
            ASTNode right = parseExpression();
            consume(TokenType.SEMICOLON, "Expected ';' after compound assignment.");
            return new CompoundAssignmentNode(identifierName, compoundOp, right);
        } else if (matchIncrementDecrement()) {
            String op = advance().getValue();
            consume(TokenType.SEMICOLON, "Expected ';' after increment/decrement.");
            return new IncrementDecrementNode(identifierName, op);
        }

        // If none of the cases matched, log an error
        errorHandler.logError("Expected function call, assignment, or compound assignment after identifier: " + identifierName, identifierToken.getLine());
        return new ErrorNode("Invalid statement after identifier: " + identifierName);
    }

    private List<ASTNode> parseArguments() {
        List<ASTNode> arguments = new ArrayList<>();
        consume(TokenType.LEFT_PARENTHESES, "Expected '(' after function name.");
        if (!check(TokenType.RIGHT_PARENTHESES)) { // Handle non-empty argument lists
            do {
                arguments.add(parseExpression());
            } while (match(TokenType.PUNCTUATION) && peek().getValue().equals(",")); // Handle comma-separated arguments
        }
        consume(TokenType.RIGHT_PARENTHESES, "Expected ')' after arguments.");
        return arguments;
    }

    private boolean matchCompoundAssignment() {
        return check(TokenType.COMPOUND_ASSIGNMENT_OPERATOR);
    }

    private boolean matchIncrementDecrement() {
        return check(TokenType.INCREMENT_OPERATOR) || check(TokenType.DECREMENT_OPERATOR);
    }

    private ASTNode parseVariableDeclaration() {
        // Expect and consume the variable type (e.g., Int, String, etc.)
        Token typeToken = consume(TokenType.KEYWORD, "Expected type for variable declaration.");
        String type = typeToken.getValue();

        // Expect and consume the variable name
        Token nameToken = consume(TokenType.IDENTIFIER, "Expected variable name.");
        String name = nameToken.getValue();

        symbolTable.addVariable(name, type, currentPosition);

        ASTNode initializer = null;

        // If there's an assignment operator, parse the initializer
        if (match(TokenType.ASSIGNMENT_OPERATOR)) {
            initializer = parseExpression(); // Parse the right-hand side expression
        }

        consume(TokenType.SEMICOLON, "Expected ';'");

        // Return a VariableDeclarationNode with the type, name, and initializer
        return new VariableDeclarationNode(type, name, initializer);
    }

    private ASTNode parseIfStatement() {
        consume(TokenType.KEYWORD, "Expected 'if' keyword.");
        consume(TokenType.LEFT_PARENTHESES, "Expected '(' after 'if'.");
        ASTNode condition = parseExpression(); // Parse the if condition
        consume(TokenType.RIGHT_PARENTHESES, "Expected ')' after condition.");

        ASTNode trueBranch;
        if (check(TokenType.LEFT_BRACE)) { // Check for `{` to start a block
            trueBranch = parseBlock(); // Parse as a block if `{` is found
        } else {
            trueBranch = parseStatement(); // Otherwise, parse as a single statement
        }

        ASTNode falseBranch = null;
        if (match(TokenType.KEYWORD) && peek().getValue().equals("else")) {
            advance(); // Consume 'else'
            if (check(TokenType.LEFT_BRACE)) {
                falseBranch = parseBlock(); // Parse block for else if `{` is found
            } else {
                falseBranch = parseStatement(); // Parse as single statement otherwise
            }
        }

        return new IfStatementNode(condition, trueBranch, falseBranch);
    }

    private ASTNode parseWhileStatement() {
        consume(TokenType.KEYWORD, "Expected 'while' keyword.");
        consume(TokenType.LEFT_PARENTHESES, "Expected '(' after 'while'.");
        ASTNode condition = parseExpression(); // Parse the while condition
        consume(TokenType.RIGHT_PARENTHESES, "Expected ')' after condition.");
        ASTNode body = parseBlock(); // Parse the body of the loop

        return new WhileStatementNode(condition, body);
    }

    private ASTNode parseForStatement() {
        consume(TokenType.KEYWORD, "Expected 'for' keyword.");
        consume(TokenType.LEFT_PARENTHESES, "Expected '(' after 'for'.");

        ASTNode initialization = null;
        if (!check(TokenType.SEMICOLON)) { // Check if there's an initialization part
            initialization = parseStatement();
        }
        consume(TokenType.SEMICOLON, "Expected ';' after initialization.");

        ASTNode condition = null;
        if (!check(TokenType.SEMICOLON)) { // Check if there's a condition part
            condition = parseExpression();
        }
        consume(TokenType.SEMICOLON, "Expected ';' after condition.");

        ASTNode update = null;
        if (!check(TokenType.RIGHT_PARENTHESES)) { // Check if there's an update part
            update = parseExpression();
        }
        consume(TokenType.RIGHT_PARENTHESES, "Expected ')' after update.");

        ASTNode body = parseBlock(); // Parse the body of the loop

        return new ForStatementNode(initialization, condition, update, body);
    }

    private ASTNode parseFunctionDeclaration() {
        consume(TokenType.KEYWORD, "Expected 'func' keyword.");
        Token functionName = consume(TokenType.IDENTIFIER, "Expected function name.");
        consume(TokenType.LEFT_PARENTHESES, "Expected '(' after function name.");

        List<ParameterNode> parameters = new ArrayList<>();
        if (!check(TokenType.RIGHT_PARENTHESES)) { // Check if there are parameters
            do {
                String type = consume(TokenType.KEYWORD, "Expected parameter type.").getValue();
                String name = consume(TokenType.IDENTIFIER, "Expected parameter name.").getValue();
                parameters.add(new ParameterNode(type, name));
            } while (match(TokenType.PUNCTUATION) && peek().getValue().equals(",")); // Handle comma-separated parameters
        }
        consume(TokenType.RIGHT_PARENTHESES, "Expected ')' after parameters.");

        String returnType = "Void"; // Default return type
        if (check(TokenType.KEYWORD)) { // Optional return type
            returnType = advance().getValue();
        }

        ASTNode body = parseBlock(); // Parse the function body

        return new FunctionDeclarationNode(functionName.getValue(), parameters, returnType, body);
    }

    private BlockNode parseBlock() {
        List<ASTNode> statements = new ArrayList<>();
        consume(TokenType.LEFT_BRACE, "Expected '{' to start a block.");

        while (!check(TokenType.RIGHT_BRACE) && isNotEOF()) {
            statements.add(parseStatement());
        }

        consume(TokenType.RIGHT_BRACE, "Expected '}' to close the block.");
        return new BlockNode(statements);
    }

    private ASTNode parseExpression() {
        return parseLogicalOr();
    }

    private ASTNode parseLogicalOr() {
        ASTNode left = parseLogicalAnd();

        while (check(TokenType.OR_KEYWORD)) {
            final String operator = advance().getValue(); // advance past the operator
            ASTNode right = parseLogicalAnd(); // Parse the right operand
            left = new LogicalExpressionNode(left, right, operator);
        }

        return left;
    }

    private ASTNode parseLogicalAnd() {
        ASTNode left = parseEquality();

        while (check(TokenType.AND_KEYWORD)) {
            final String operator = advance().getValue(); // advance past the operator
            ASTNode right = parseEquality(); // Parse the right operand
            left = new LogicalExpressionNode(left, right, operator);
        }

        return left;
    }

    private ASTNode parseEquality() {
        ASTNode left = parseComparison();

        if (check(TokenType.EQUALITY_OPERATOR)) {
            final String operator = advance().getValue(); // advance past the operator
            ASTNode right = parseComparison(); // Parse the right operand
            left = new ComparisonExpressionNode(left, right, operator);
        }

        return left;
    }

    private ASTNode parseComparison() {
        ASTNode left = parseTerm();

        while (check(TokenType.COMPARISON_OPERATOR)) {
            final String operator = advance().getValue(); // advance past the operator
            ASTNode right = parseTerm(); // Parse the right operand
            left = new ComparisonExpressionNode(left, right, operator);
        }

        return left;
    }

    private ASTNode parseTerm() {
        ASTNode left = parseFactor();

        while (check(TokenType.ADDITION_OPERATOR) || check(TokenType.SUBTRACTION_OPERATOR)) {
            final String operator = advance().getValue(); // advance past the operator
            ASTNode right = parseFactor(); // Parse the right operand
            left = new BinaryExpressionNode(left, right, operator);
        }

        return left;
    }

    private ASTNode parseFactor() {
        ASTNode left = parseUnary();

        while (check(TokenType.MULTIPLICATION_OPERATOR) || check(TokenType.DIVISION_OPERATOR)) {
            final String operator = advance().getValue(); // advance past the operator
            ASTNode right = parseUnary(); // Parse the right operand
            left = new BinaryExpressionNode(left, right, operator);
        }

        return left;
    }

    private ASTNode parseUnary() {
        if (check(TokenType.NOT_OPERATOR) || check(TokenType.ADDITION_OPERATOR) || check(TokenType.SUBTRACTION_OPERATOR)
        ) {
            final String operator = advance().getValue(); // advance past the current unary
            final ASTNode operand = parseUnary();
            return new UnaryExpressionNode(operand, operator);
        }

        return parsePrimary();
    }

    private ASTNode parsePrimary() {
        final Token token = advance();

        return switch (token.getType()) {
            case IDENTIFIER -> {
                if (check(TokenType.LEFT_PARENTHESES)) {
                    advance(); // consume the '('
                    List<ASTNode> arguments = new ArrayList<>();

                    // Parse arguments until we find a ')'
                    if (!check(TokenType.RIGHT_PARENTHESES)) {
                        do {
                            arguments.add(parseExpression()); // Parse each argument
                        } while (
                            peek().getValue().equals(",")
                            && match(TokenType.PUNCTUATION)
                        ); // Check for commas between arguments
                    }

                    consume(TokenType.RIGHT_PARENTHESES, "Expected ')' after function arguments."); // Consume the ')'
                    yield new FunctionCallNode(token.getValue(), arguments); // Create a FunctionCallNode
                }

                // Otherwise, treat it as a variable reference
                yield new VariableReferenceNode(token.getValue());
            }
            case LEFT_PARENTHESES -> {
                ASTNode expression = parseExpression(); // Parse the expression inside the parentheses
                consume(TokenType.RIGHT_PARENTHESES, "Expected ')' after expression.");
                yield expression;
            }
            case NUMBER_LITERAL -> new NumberLiteral(Integer.parseInt(token.getValue()));
            case STRING_LITERAL -> new StringLiteral(token.getValue());
            case BOOLEAN_LITERAL -> {
                final boolean value = token.getValue().equals("true");
                yield new BooleanLiteral(value);
            }
            default -> new ErrorNode("Unexpected token: " + token.getValue());
        };
    }

    private Token consume(TokenType type, String error) throws RuntimeException {
        if (!check(type)) {
            errorHandler.logError(error, peek().getLine());
        }
        return advance();
    }

    // check for EOF, the end of the tokens list
    private boolean isNotEOF() {
        return currentPosition != tokens.size() - 1;
    }

    // advance to the next token while returning the current token
    private Token advance() {
        if (isNotEOF()) {
            return tokens.get(currentPosition++);
        }
        return tokens.get(currentPosition);
    }

    // peek the current token
    private Token peek() {
        return tokens.get(currentPosition);
    }

    // check if the current token is a certain type
    private boolean check(TokenType type) {
        return peek().getType() == type;
    }

    private boolean match(TokenType type) {
        if (check(type)) {
            advance();
            return true;
        }
        return false;
    }
}
