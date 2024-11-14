package parser.nodes;

public class ForStatementNode extends ASTNode {
    private final ASTNode initialization;
    private final ASTNode condition;
    private final ASTNode update;
    private final ASTNode body;

    public ForStatementNode(ASTNode initialization, ASTNode condition, ASTNode update, ASTNode body) {
        this.initialization = initialization;
        this.condition = condition;
        this.update = update;
        this.body = body;
    }

    public ASTNode getInitialization() {
        return initialization;
    }

    public ASTNode getCondition() {
        return condition;
    }

    public ASTNode getUpdate() {
        return update;
    }

    public ASTNode getBody() {
        return body;
    }
}

