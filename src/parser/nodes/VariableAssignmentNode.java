package parser.nodes;

public class VariableAssignmentNode extends ASTNode {
    private final String name;
    private final ASTNode value;

    public VariableAssignmentNode(String variableName, ASTNode value) {
        this.name = variableName;
        this.value = value;
    }

    public String getVariableName() { return name; }
    public ASTNode getValue() { return value; }
}
