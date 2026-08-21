package com.minilang.ast;

import com.minilang.lexer.TokenType;

/**
 * Represents a binary expression: left <operator> right
 * e.g.: a + b, x * y, a > 5, flag && ready
 */
public class BinaryExprNode extends ExpressionNode {
    private final ExpressionNode left;
    private final TokenType operator;
    private final ExpressionNode right;

    public BinaryExprNode(ExpressionNode left, TokenType operator, ExpressionNode right, int line, int column) {
        super(line, column);
        this.left = left;
        this.operator = operator;
        this.right = right;
    }

    public ExpressionNode getLeft() {
        return left;
    }

    public TokenType getOperator() {
        return operator;
    }

    public ExpressionNode getRight() {
        return right;
    }

    @Override
    public <R> R accept(ASTVisitor<R> visitor) {
        return visitor.visitBinaryExpr(this);
    }
}
