package com.minilang.ast;

/**
 * Represents a conditional if-else statement:
 * if (condition) { thenBlock } else { elseBlock }
 */
public class IfNode extends StatementNode {
    private final ExpressionNode condition;
    private final StatementNode thenBranch;
    private final StatementNode elseBranch; // optional, can be null

    public IfNode(ExpressionNode condition, StatementNode thenBranch, StatementNode elseBranch, int line, int column) {
        super(line, column);
        this.condition = condition;
        this.thenBranch = thenBranch;
        this.elseBranch = elseBranch;
    }

    public ExpressionNode getCondition() {
        return condition;
    }

    public StatementNode getThenBranch() {
        return thenBranch;
    }

    public StatementNode getElseBranch() {
        return elseBranch;
    }

    public boolean hasElseBranch() {
        return elseBranch != null;
    }

    @Override
    public <R> R accept(ASTVisitor<R> visitor) {
        return visitor.visitIf(this);
    }
}
