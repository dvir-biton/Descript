package parser.variables;

public class VariableInfo {
    private final String type;
    private final int index;

    public VariableInfo(String type, int index) {
        this.type = type;
        this.index = index;
    }

    public String getType() {
        return type;
    }

    public int getIndex() {
        return index;
    }
}