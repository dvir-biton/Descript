package parser.nodes;

public class ParameterNode extends ASTNode {
    private final String type;
    private final String name;

    public ParameterNode(String type, String name) {
        this.name = name;
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public String getName() {
        return name;
    }
}
