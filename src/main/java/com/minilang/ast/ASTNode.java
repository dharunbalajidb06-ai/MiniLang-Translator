package com.minilang.ast;

/**
 * Base class for all Abstract Syntax Tree (AST) nodes in MiniLang.
 */
public abstract class ASTNode {
    private final int line;
    private final int column;

    public ASTNode(int line, int column) {
        this.line = line;
        this.column = column;
    }

    public int getLine() {
        return line;
    }

    public int getColumn() {
        return column;
    }

    public abstract <R> R accept(ASTVisitor<R> visitor);
}
