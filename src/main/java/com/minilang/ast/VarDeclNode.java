package com.minilang.ast;

import com.minilang.symbol.Type;

/**
 * Represents a variable declaration statement, e.g.:
 * int a = 10;
 * float price;
 */
public class VarDeclNode extends StatementNode {
    private final Type type;
    private final String varName;
    private final ExpressionNode initializer; // optional, can be null

    public VarDeclNode(Type type, String varName, ExpressionNode initializer, int line, int column) {
        super(line, column);
        this.type = type;
        this.varName = varName;
        this.initializer = initializer;
    }

    public Type getType() {
        return type;
    }

    public String getVarName() {
        return varName;
    }

    public ExpressionNode getInitializer() {
        return initializer;
    }

    public boolean hasInitializer() {
        return initializer != null;
    }

    @Override
    public <R> R accept(ASTVisitor<R> visitor) {
        return visitor.visitVarDecl(this);
    }
}
