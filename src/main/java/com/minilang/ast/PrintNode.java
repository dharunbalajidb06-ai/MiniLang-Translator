package com.minilang.ast;

/**
 * Represents an output print statement, e.g.:
 * print(result);
 */
public class PrintNode extends StatementNode {
    private final ExpressionNode expression;

    public PrintNode(ExpressionNode expression, int line, int column) {
        super(line, column);
        this.expression = expression;
    }

    public ExpressionNode getExpression() {
        return expression;
    }

    @Override
    public <R> R accept(ASTVisitor<R> visitor) {
        return visitor.visitPrint(this);
    }
}
