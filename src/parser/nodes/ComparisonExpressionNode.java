package parser.nodes;

public class ComparisonExpressionNode extends ASTNode {
    private final ASTNode left;
    private final ASTNode right;
    private final String operator;

    public ComparisonExpressionNode(ASTNode left, ASTNode right, String operator) {
        this.left = left;
        this.right = right;
        this.operator = operator;
    }

    public ASTNode getLeft() {
        return left;
    }

    public ASTNode getRight() {
        return right;
    }

    public String getOperator() {
        return operator;
    }
}
