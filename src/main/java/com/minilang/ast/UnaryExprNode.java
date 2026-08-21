package com.minilang.ast;

import com.minilang.lexer.TokenType;

/**
 * Represents a unary expression: <operator> operand
 * e.g.: -a, !flag
 */
public class UnaryExprNode extends ExpressionNode {
    private final TokenType operator;
    private final ExpressionNode operand;

    public UnaryExprNode(TokenType operator, ExpressionNode operand, int line, int column) {
        super(line, column);
        this.operator = operator;
        this.operand = operand;
    }

    public TokenType getOperator() {
        return operator;
    }

    public ExpressionNode getOperand() {
        return operand;
    }

    @Override
    public <R> R accept(ASTVisitor<R> visitor) {
        return visitor.visitUnaryExpr(this);
    }
}
