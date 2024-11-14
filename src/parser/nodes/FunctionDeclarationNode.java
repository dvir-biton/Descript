package parser.nodes;

import java.util.List;

public class FunctionDeclarationNode extends ASTNode {
    private final String name;
    private final List<ParameterNode> parameters;
    private final String returnType;
    private final ASTNode body;

    public FunctionDeclarationNode(
        String name,
        List<ParameterNode> parameters,
        String returnType,
        ASTNode body
    ) {
        this.name = name;
        this.parameters = parameters;
        this.returnType = returnType;
        this.body = body;
    }

    public String getName() {
        return name;
    }

    public List<ParameterNode> getParameters() {
        return parameters;
    }

    public String getReturnType() {
        return returnType;
    }

    public ASTNode getBody() {
        return body;
    }
}
