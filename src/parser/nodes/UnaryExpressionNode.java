package parser.nodes;

public class UnaryExpressionNode extends ASTNode {
    private final ASTNode operand;
    private final String operator;

    public UnaryExpressionNode(ASTNode operand, String operator) {
        this.operand = operand;
        this.operator = operator;
    }

    public ASTNode getOperand() {
        return operand;
    }

    public String getOperator() {
        return operator;
    }
}
