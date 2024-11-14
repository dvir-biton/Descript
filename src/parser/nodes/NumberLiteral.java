package parser.nodes;

public class NumberLiteral extends LiteralNode {
    private final int value;

    public NumberLiteral(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "NumberLiteral { value: " + value + " }";
    }
}
