package parser.nodes;

public class VariableDeclarationNode extends ASTNode {
    private final String type;
    private final String name;
    private final ASTNode value;

    public VariableDeclarationNode(String type, String name, ASTNode value) {
        this.type = type;
        this.name = name;
        this.value = value;
    }

    public String getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public ASTNode getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "VariableDeclarationNode { type: " + type + ", name: " + name + ", value: " + value + " }";
    }
}
