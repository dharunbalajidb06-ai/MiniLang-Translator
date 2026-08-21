package com.minilang.ast;

import java.util.ArrayList;
import java.util.List;

/**
 * Root node representing an entire MiniLang program.
 */
public class ProgramNode extends ASTNode {
    private final List<StatementNode> statements;

    public ProgramNode(List<StatementNode> statements, int line, int column) {
        super(line, column);
        this.statements = statements != null ? statements : new ArrayList<>();
    }

    public List<StatementNode> getStatements() {
        return statements;
    }

    @Override
    public <R> R accept(ASTVisitor<R> visitor) {
        return visitor.visitProgram(this);
    }
}
