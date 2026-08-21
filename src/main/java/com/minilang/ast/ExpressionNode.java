package com.minilang.ast;

import com.minilang.symbol.Type;

/**
 * Base class for all expression nodes in MiniLang (e.g. binary operations, literals, variables).
 */
public abstract class ExpressionNode extends ASTNode {
    // Inferred type set by Semantic Analyzer during type checking
    private Type evaluatedType = Type.UNKNOWN;

    public ExpressionNode(int line, int column) {
        super(line, column);
    }

    public Type getEvaluatedType() {
        return evaluatedType;
    }

    public void setEvaluatedType(Type evaluatedType) {
        this.evaluatedType = evaluatedType;
    }
}
