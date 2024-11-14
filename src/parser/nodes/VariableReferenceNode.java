package parser.nodes;

public class VariableReferenceNode extends ASTNode {
    private final String name;

    public VariableReferenceNode(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "Variable(name=" + name + ")";
    }
}

