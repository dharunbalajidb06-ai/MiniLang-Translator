package com.minilang.ast;

/**
 * Base class for all statement nodes in MiniLang (e.g. declarations, assignments, if, while, print).
 */
public abstract class StatementNode extends ASTNode {
    public StatementNode(int line, int column) {
        super(line, column);
    }
}
