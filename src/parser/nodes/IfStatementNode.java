package parser.nodes;

public class IfStatementNode extends ASTNode {
    private final ASTNode condition;
    private final ASTNode trueBranch;
    private final ASTNode falseBranch;

    public IfStatementNode(
        ASTNode condition,
        ASTNode trueBranch,
        ASTNode falseBranch
    ) {
        this.condition = condition;
        this.trueBranch = trueBranch;
        this.falseBranch = falseBranch;
    }

    public ASTNode getCondition() {
        return condition;
    }

    public ASTNode getTrueBranch() {
        return trueBranch;
    }

    public ASTNode getFalseBranch() {
        return falseBranch;
    }
}
