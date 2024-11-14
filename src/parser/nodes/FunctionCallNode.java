package parser.nodes;

import java.util.List;

public class FunctionCallNode extends ASTNode {
    private final String functionName;
    private final List<ASTNode> arguments;

    public FunctionCallNode(String functionName, List<ASTNode> arguments) {
        this.functionName = functionName;
        this.arguments = arguments;
    }

    public String getFunctionName() {
        return functionName;
    }

    public List<ASTNode> getArguments() {
        return arguments;
    }

    @Override
    public String toString() {
        return "Function(name=" + functionName + ", args=" + arguments + ")";
    }
}

