package parser.nodes;

public class StringLiteral extends LiteralNode {
    private final String value;

    public StringLiteral(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
