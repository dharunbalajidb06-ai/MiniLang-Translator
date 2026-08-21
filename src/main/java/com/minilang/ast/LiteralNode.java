package com.minilang.ast;

import com.minilang.symbol.Type;

/**
 * Represents literal values (int, float, string, boolean) in MiniLang.
 */
public class LiteralNode extends ExpressionNode {
    private final Object value;
    private final Type type;

    public LiteralNode(Object value, Type type, int line, int column) {
        super(line, column);
        this.value = value;
        this.type = type;
        setEvaluatedType(type);
    }

    public Object getValue() {
        return value;
    }

    public Type getType() {
        return type;
    }

    @Override
    public <R> R accept(ASTVisitor<R> visitor) {
        return visitor.visitLiteral(this);
    }
}
