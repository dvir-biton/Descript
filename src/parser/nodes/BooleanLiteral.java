package parser.nodes;

public class BooleanLiteral extends LiteralNode {
    private final boolean value;

    public BooleanLiteral(boolean value) {
        this.value = value;
    }

    public boolean getValue() {
        return value;
    }
}
