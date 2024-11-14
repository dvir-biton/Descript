package parser.nodes;

public class IncrementDecrementNode extends ASTNode {
    private final String variableName;
    private final String op;

    public IncrementDecrementNode(String variableName, String op) {
        this.variableName = variableName;
        this.op = op;
    }

    public String getVariableName() {
        return variableName;
    }

    public String getOp() {
        return op;
    }
}