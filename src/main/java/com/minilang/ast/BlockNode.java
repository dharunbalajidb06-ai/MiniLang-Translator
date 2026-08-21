package com.minilang.ast;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a block of statements enclosed in curly braces { ... }.
 */
public class BlockNode extends StatementNode {
    private final List<StatementNode> statements;

    public BlockNode(List<StatementNode> statements, int line, int column) {
        super(line, column);
        this.statements = statements != null ? statements : new ArrayList<>();
    }

    public List<StatementNode> getStatements() {
        return statements;
    }

    @Override
    public <R> R accept(ASTVisitor<R> visitor) {
        return visitor.visitBlock(this);
    }
}
