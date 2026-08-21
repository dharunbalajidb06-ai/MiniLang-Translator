package com.minilang.ast;

/**
 * Represents a variable reference in an expression, e.g. a, price, x.
 */
public class VariableNode extends ExpressionNode {
    private final String name;

    public VariableNode(String name, int line, int column) {
        super(line, column);
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public <R> R accept(ASTVisitor<R> visitor) {
        return visitor.visitVariable(this);
    }
}
