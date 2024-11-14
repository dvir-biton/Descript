package parser.nodes;

public class ReturnNode extends ASTNode {
    private final ASTNode returnValue;

    public ReturnNode(ASTNode returnValue) {
        this.returnValue = returnValue;
    }

    public ASTNode getReturnValue() {
        return returnValue;
    }
}
