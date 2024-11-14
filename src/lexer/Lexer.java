package lexer;

import lexer.token.Token;
import lexer.token.TokenType;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Lexer {
    private final String input;
    private int currentPosition;
    private int currentLine;

    public Lexer(String input) {
        this.input = input;
        currentPosition = 0;
        currentLine = 1;
    }

    public List<Token> tokenize() throws RuntimeException {
        final List<Token> tokens = new ArrayList<>();

        while (currentPosition < input.length()) {
            char currentChar = input.charAt(currentPosition);

            if (Character.isWhitespace(currentChar) && currentChar != '\n') {
                currentPosition++;
                continue;
            }

            Token token = nextToken();
            if (token != null && token.getType() != TokenType.COMMENT) {
                if (token.getType() == TokenType.NEWLINE) {
                    currentLine++;
                } else {
                    tokens.add(token);
                }
            } else {
                throw new RuntimeException("Unknown character: " + currentChar);
            }
        }

        return tokens;
    }

    private Token nextToken() {
        if (currentPosition == input.length()) {
            return null;
        }

        String[] tokenPatterns = {
            "//.*",
            "(?s)/\\*.*?\\*/",                                       // Comment
            "(\\d+\\.\\d+|\\d+)",                                    // Number literals
            "\\b(true|false)\\b",                                    // Boolean literals
            "\"[^\"]*\"",                                            // String literals
            "\\b(if|else|while|for|Int|String|Bool|Double|func)\\b", // Keywords
            "[a-zA-Z_][a-zA-Z0-9_]*",                                // Identifiers (including `print` and `input`)
            "\\bor\\b",                                              // Or keyword
            "\\band\\b",                                             // And keyword
            "==|!=",                                                 // Equality operators
            "<=|>=|<|>",                                             // Comparison operators
            "\\+=|-=|\\*=|/=",                                       // Compound assignment operators
            "\\+\\+", "--",                                          // Increment and decrement operators
            "\\+",                                                   // Addition operator
            "-",                                                     // Subtraction operator
            "\\*",                                                   // Multiplication operator
            "/",                                                     // Division operator
            "=",                                                     // Assignment operator
            "!",                                                     // Logical NOT operator
            "[.,]",                                                  // Punctuation
            "\\(",                                                   // Left parentheses
            "\\)",                                                   // Right parentheses
            "\\{",                                                   // Left brace
            "}",                                                     // Right brace
            ";",                                                     // Semicolon
            "\\n",                                                   // New line
        };

        TokenType[] types = {
            TokenType.COMMENT,
            TokenType.COMMENT,
            TokenType.NUMBER_LITERAL,
            TokenType.BOOLEAN_LITERAL,
            TokenType.STRING_LITERAL,
            TokenType.KEYWORD,
            TokenType.IDENTIFIER,
            TokenType.OR_KEYWORD,
            TokenType.AND_KEYWORD,
            TokenType.EQUALITY_OPERATOR,
            TokenType.COMPARISON_OPERATOR,
            TokenType.COMPOUND_ASSIGNMENT_OPERATOR,
            TokenType.INCREMENT_OPERATOR,
            TokenType.DECREMENT_OPERATOR,
            TokenType.ADDITION_OPERATOR,
            TokenType.SUBTRACTION_OPERATOR,
            TokenType.MULTIPLICATION_OPERATOR,
            TokenType.DIVISION_OPERATOR,
            TokenType.ASSIGNMENT_OPERATOR,
            TokenType.NOT_OPERATOR,
            TokenType.PUNCTUATION,
            TokenType.LEFT_PARENTHESES,
            TokenType.RIGHT_PARENTHESES,
            TokenType.LEFT_BRACE,
            TokenType.RIGHT_BRACE,
            TokenType.SEMICOLON,
            TokenType.NEWLINE
        };

        for (int i = 0; i < tokenPatterns.length; i++) {
            Pattern pattern = Pattern.compile(tokenPatterns[i]);
            Matcher matcher = pattern.matcher(input.substring(currentPosition));

            if (matcher.lookingAt()) {
                String value = matcher.group();
                currentPosition += value.length();

                if (types[i] == TokenType.STRING_LITERAL) {
                    value = value.substring(1, value.length() - 1);
                }

                return new Token(types[i], value, currentLine);
            }
        }

        return null;
    }
}
