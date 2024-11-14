package parser.nodes;

public abstract class ASTNode {
    private int lineNumber;
    private int columnNumber;

    public void setPosition(int lineNumber, int columnNumber) {
        this.lineNumber = lineNumber;
        this.columnNumber = columnNumber;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public int getColumnNumber() {
        return columnNumber;
    }
}
