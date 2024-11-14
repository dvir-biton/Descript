package parser.nodes;

import java.util.List;

public class BlockNode extends ASTNode {
    private final List<ASTNode> statements;

    public BlockNode(List<ASTNode> statements) {
        this.statements = statements;
    }

    public List<ASTNode> getStatements() {
        return statements;
    }
}

