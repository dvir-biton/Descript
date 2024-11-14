package parser.nodes;

public class ErrorNode extends ASTNode {
    private final String message;

    public ErrorNode(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
