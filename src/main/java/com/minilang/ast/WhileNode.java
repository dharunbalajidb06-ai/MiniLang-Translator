package com.minilang.ast;

/**
 * Represents a while loop statement:
 * while (condition) { body }
 */
public class WhileNode extends StatementNode {
    private final ExpressionNode condition;
    private final StatementNode body;

    public WhileNode(ExpressionNode condition, StatementNode body, int line, int column) {
        super(line, column);
        this.condition = condition;
        this.body = body;
    }

    public ExpressionNode getCondition() {
        return condition;
    }

    public StatementNode getBody() {
        return body;
    }

    @Override
    public <R> R accept(ASTVisitor<R> visitor) {
        return visitor.visitWhile(this);
    }
}
