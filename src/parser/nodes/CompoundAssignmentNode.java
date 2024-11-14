package parser.nodes;

public class CompoundAssignmentNode extends ASTNode {
    private final String name;
    private final String compoundOp;
    private final ASTNode value;

    public CompoundAssignmentNode(String variableName, String compoundOp, ASTNode value) {
        this.name = variableName;
        this.compoundOp = compoundOp;
        this.value = value;
    }

    public String getVariableName() {
        return name;
    }

    public String getCompoundOp() {
        return compoundOp;
    }

    public ASTNode getValue() {
        return value;
    }
}
