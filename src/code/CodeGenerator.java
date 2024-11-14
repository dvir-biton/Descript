package code;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import parser.nodes.*;
import parser.variables.SymbolTable;
import parser.variables.VariableInfo;

public class CodeGenerator {
    private final ClassWriter classWriter;
    private MethodVisitor methodVisitor;
    private int currentVariableIndex = 1;
    private final SymbolTable symbolTable;

    public CodeGenerator(SymbolTable symbolTable) {
        this.symbolTable = symbolTable;
        classWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
        classWriter.visit(Opcodes.V1_8, Opcodes.ACC_PUBLIC, "GeneratedClass", null, "java/lang/Object", null);

        MethodVisitor constructor = classWriter.visitMethod(Opcodes.ACC_PUBLIC, "<init>", "()V", null, null);
        constructor.visitCode();
        constructor.visitVarInsn(Opcodes.ALOAD, 0);
        constructor.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
        constructor.visitInsn(Opcodes.RETURN);
        constructor.visitMaxs(1, 1);
        constructor.visitEnd();
    }

    public void startMainMethod() {
        methodVisitor = classWriter.visitMethod(Opcodes.ACC_PUBLIC + Opcodes.ACC_STATIC, "main", "([Ljava/lang/String;)V", null, null);
        methodVisitor.visitCode();
    }

    public void endMainMethod() {
        methodVisitor.visitInsn(Opcodes.RETURN);
        methodVisitor.visitMaxs(1, currentVariableIndex);
        methodVisitor.visitEnd();
    }

    public void generate(ASTNode node) {
        if (node instanceof VariableDeclarationNode) {
            generateVariableDeclaration((VariableDeclarationNode) node);
        } else if (node instanceof FunctionCallNode) {
            generateFunctionCall((FunctionCallNode) node);
        } else if (node instanceof NumberLiteral) {
            generateNumberLiteral((NumberLiteral) node);
        } else if (node instanceof StringLiteral) {
            generateStringLiteral((StringLiteral) node);
        } else if (node instanceof BooleanLiteral) {
            generateBooleanLiteral((BooleanLiteral) node);
        } else if (node instanceof BinaryExpressionNode) {
            generateBinaryExpression((BinaryExpressionNode) node);
        } else if (node instanceof ComparisonExpressionNode) {
            generateComparisonExpression((ComparisonExpressionNode) node);
        } else if (node instanceof IfStatementNode) {
            generateIfStatement((IfStatementNode) node);
        } else if (node instanceof WhileStatementNode) {
            generateWhileStatement((WhileStatementNode) node);
        } else if (node instanceof BlockNode) {
            generateBlock((BlockNode) node);
        } else if (node instanceof VariableReferenceNode) {
            generateVariableReference((VariableReferenceNode) node);
        } else if (node instanceof VariableAssignmentNode) {
            generateVariableAssignment((VariableAssignmentNode) node);
        } else if (node instanceof CompoundAssignmentNode) {
            generateCompoundAssignment((CompoundAssignmentNode) node);
        } else if (node instanceof IncrementDecrementNode) {
            generateIncrementDecrement((IncrementDecrementNode) node);
        } else {
            throw new UnsupportedOperationException("Unsupported AST Node: " + node.getClass().getSimpleName());
        }
    }

    private void generateVariableAssignment(VariableAssignmentNode node) {
        // Retrieve variable information from SymbolTable
        VariableInfo entry = symbolTable.getVariable(node.getVariableName());
        if (entry == null) {
            throw new RuntimeException("Variable not declared: " + node.getVariableName());
        }

        // Generate code for the new value (right-hand side of the assignment)
        generate(node.getValue());

        // Determine the correct opcode based on the variable's type
        String varType = entry.getType();
        int index = entry.getIndex();
        switch (varType) {
            case "Int", "Bool":
                methodVisitor.visitVarInsn(Opcodes.ISTORE, index);
                break;
            case "String":
                methodVisitor.visitVarInsn(Opcodes.ASTORE, index);
                break;
            default:
                throw new UnsupportedOperationException("Unsupported variable type: " + varType);
        }
    }

    private void generateCompoundAssignment(CompoundAssignmentNode node) {
        VariableInfo entry = symbolTable.getVariable(node.getVariableName());
        if (entry == null) {
            throw new RuntimeException("Variable not declared: " + node.getVariableName());
        }

        int index = entry.getIndex();
        String op = node.getCompoundOp();

        // Load the current value of the variable
        if ("Int".equals(entry.getType()) || "Bool".equals(entry.getType())) {
            methodVisitor.visitVarInsn(Opcodes.ILOAD, index);
        } else {
            throw new UnsupportedOperationException("Unsupported type for compound assignment: " + entry.getType());
        }

        // Generate the right-hand side expression
        generate(node.getValue());

        // Apply the compound operation
        switch (op) {
            case "+=":
                methodVisitor.visitInsn(Opcodes.IADD);
                break;
            case "-=":
                methodVisitor.visitInsn(Opcodes.ISUB);
                break;
            case "*=":
                methodVisitor.visitInsn(Opcodes.IMUL);
                break;
            case "/=":
                methodVisitor.visitInsn(Opcodes.IDIV);
                break;
            default:
                throw new UnsupportedOperationException("Unsupported compound operator: " + op);
        }

        // Store the result back in the variable
        methodVisitor.visitVarInsn(Opcodes.ISTORE, index);
    }

    private void generateIncrementDecrement(IncrementDecrementNode node) {
        VariableInfo entry = symbolTable.getVariable(node.getVariableName());
        if (entry == null) {
            throw new RuntimeException("Variable not declared: " + node.getVariableName());
        }

        int index = entry.getIndex();
        String op = node.getOp();

        // Load the current value of the variable
        methodVisitor.visitVarInsn(Opcodes.ILOAD, index);

        // Apply the increment or decrement
        if ("++".equals(op)) {
            methodVisitor.visitInsn(Opcodes.ICONST_1);
            methodVisitor.visitInsn(Opcodes.IADD);
        } else if ("--".equals(op)) {
            methodVisitor.visitInsn(Opcodes.ICONST_1);
            methodVisitor.visitInsn(Opcodes.ISUB);
        } else {
            throw new UnsupportedOperationException("Unsupported increment/decrement operator: " + op);
        }

        // Store the result back in the variable
        methodVisitor.visitVarInsn(Opcodes.ISTORE, index);
    }

    private void generateComparisonExpression(ComparisonExpressionNode node) {
        Label trueLabel = new Label();
        Label endLabel = new Label();

        generate(node.getLeft());  // Generate bytecode for the left operand
        generate(node.getRight()); // Generate bytecode for the right operand

        // Use appropriate jump instruction based on the comparison operator
        switch (node.getOperator()) {
            case "<":
                methodVisitor.visitJumpInsn(Opcodes.IF_ICMPLT, trueLabel);
                break;
            case "<=":
                methodVisitor.visitJumpInsn(Opcodes.IF_ICMPLE, trueLabel);
                break;
            case ">":
                methodVisitor.visitJumpInsn(Opcodes.IF_ICMPGT, trueLabel);
                break;
            case ">=":
                methodVisitor.visitJumpInsn(Opcodes.IF_ICMPGE, trueLabel);
                break;
            case "==":
                methodVisitor.visitJumpInsn(Opcodes.IF_ICMPEQ, trueLabel);
                break;
            case "!=":
                methodVisitor.visitJumpInsn(Opcodes.IF_ICMPNE, trueLabel);
                break;
            default:
                throw new UnsupportedOperationException("Unsupported comparison operator: " + node.getOperator());
        }

        // If the condition fails, push 0 (false) onto the stack and jump to end
        methodVisitor.visitInsn(Opcodes.ICONST_0);
        methodVisitor.visitJumpInsn(Opcodes.GOTO, endLabel);

        // If the condition is true, push 1 (true) onto the stack
        methodVisitor.visitLabel(trueLabel);
        methodVisitor.visitInsn(Opcodes.ICONST_1);

        // Mark the end label
        methodVisitor.visitLabel(endLabel);
    }

    private void generateVariableDeclaration(VariableDeclarationNode node) {
        // Determine the variable type and assign a slot index
        String type = node.getType();
        int index = currentVariableIndex++;

        // Store variable info in SymbolTable
        symbolTable.addVariable(node.getName(), type, index);

        // Generate code for the variable's initialization value
        generate(node.getValue());

        // Store the variable in the correct slot based on type
        if ("Int".equals(type)) {
            methodVisitor.visitVarInsn(Opcodes.ISTORE, index);
        } else if ("String".equals(type)) {
            methodVisitor.visitVarInsn(Opcodes.ASTORE, index);
        } else if ("Bool".equals(type)) {
            methodVisitor.visitVarInsn(Opcodes.ISTORE, index);
        }
    }

    private void generateVariableReference(VariableReferenceNode node) {
        VariableInfo varInfo = symbolTable.getVariable(node.getName());
        if (varInfo == null) {
            throw new RuntimeException("Undefined variable: " + node.getName());
        }

        int index = varInfo.getIndex();
        String type = varInfo.getType();

        // Load the variable based on its type
        if ("Int".equals(type) || "Bool".equals(type)) {
            methodVisitor.visitVarInsn(Opcodes.ILOAD, index);
        } else if ("String".equals(type)) {
            methodVisitor.visitVarInsn(Opcodes.ALOAD, index);
        }
    }

    private void generateFunctionCall(FunctionCallNode node) {
        if ("print".equals(node.getFunctionName())) {
            // Load System.out for print
            methodVisitor.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");

            // Generate code for the argument of the print function
            ASTNode argument = node.getArguments().get(0);
            generate(argument);  // Generate the argument, it should leave the value on the stack

            // Determine the type of the argument to call the correct println method
            String argumentType = getArgumentType(argument);

            // Invoke the appropriate println method based on the argument type
            switch (argumentType) {
                case "Int":
                    methodVisitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream", "println", "(I)V", false);
                    break;
                case "String":
                    methodVisitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false);
                    break;
                case "Bool":
                    methodVisitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Z)V", false);
                    break;
                default:
                    throw new UnsupportedOperationException("Unsupported print argument type: " + argumentType);
            }
        } else {
            throw new UnsupportedOperationException("Unsupported function: " + node.getFunctionName());
        }
    }

    // Helper method to determine the argument type for println calls
    private String getArgumentType(ASTNode argument) {
        if (argument instanceof VariableReferenceNode) {
            VariableInfo varInfo = symbolTable.getVariable(((VariableReferenceNode) argument).getName());
            return varInfo != null ? varInfo.getType() : null;
        } else if (argument instanceof NumberLiteral) {
            return "Int";
        } else if (argument instanceof StringLiteral) {
            return "String";
        } else if (argument instanceof BooleanLiteral) {
            return "Bool";
        }
        throw new UnsupportedOperationException("Unsupported argument type for print: " + argument.getClass().getSimpleName());
    }

    private void generateNumberLiteral(NumberLiteral node) {
        methodVisitor.visitLdcInsn(node.getValue());
    }

    private void generateStringLiteral(StringLiteral node) {
        methodVisitor.visitLdcInsn(node.getValue());
    }

    private void generateBooleanLiteral(BooleanLiteral node) {
        int value = node.getValue() ? 1 : 0;
        methodVisitor.visitLdcInsn(value);
    }

    private void generateBinaryExpression(BinaryExpressionNode node) {
        generate(node.getLeft());
        generate(node.getRight());

        switch (node.getOperator()) {
            case "+":
                methodVisitor.visitInsn(Opcodes.IADD);
                break;
            case "-":
                methodVisitor.visitInsn(Opcodes.ISUB);
                break;
            case "*":
                methodVisitor.visitInsn(Opcodes.IMUL);
                break;
            case "/":
                methodVisitor.visitInsn(Opcodes.IDIV);
                break;
            default:
                throw new UnsupportedOperationException("Unsupported operator: " + node.getOperator());
        }
    }

    private void generateIfStatement(IfStatementNode node) {
        Label elseLabel = new Label();
        Label endLabel = new Label();

        generate(node.getCondition());
        methodVisitor.visitJumpInsn(Opcodes.IFEQ, elseLabel);

        generate(node.getTrueBranch());
        methodVisitor.visitJumpInsn(Opcodes.GOTO, endLabel);

        methodVisitor.visitLabel(elseLabel);
        if (node.getFalseBranch() != null) {
            generate(node.getFalseBranch());
        }

        methodVisitor.visitLabel(endLabel);
    }

    private void generateWhileStatement(WhileStatementNode node) {
        Label startLabel = new Label();
        Label endLabel = new Label();

        methodVisitor.visitLabel(startLabel);
        generate(node.getCondition());
        methodVisitor.visitJumpInsn(Opcodes.IFEQ, endLabel);

        generate(node.getBody());
        methodVisitor.visitJumpInsn(Opcodes.GOTO, startLabel);

        methodVisitor.visitLabel(endLabel);
    }

    private void generateBlock(BlockNode node) {
        for (ASTNode statement : node.getStatements()) {
            generate(statement);
        }
    }

    public byte[] generateClass() {
        classWriter.visitEnd();
        return classWriter.toByteArray();
    }
}