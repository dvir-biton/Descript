package parser.nodes;

public class WhileStatementNode extends ASTNode {
    private final ASTNode condition;
    private final ASTNode body;

    public WhileStatementNode(ASTNode condition, ASTNode body) {
        this.condition = condition;
        this.body = body;
    }

    public ASTNode getCondition() {
        return condition;
    }

    public ASTNode getBody() {
        return body;
    }
}

