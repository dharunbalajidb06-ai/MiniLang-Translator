package com.minilang.ast;

/**
 * Represents a variable assignment statement, e.g.:
 * a = b + 10;
 */
public class AssignNode extends StatementNode {
    private final String varName;
    private final ExpressionNode expression;

    public AssignNode(String varName, ExpressionNode expression, int line, int column) {
        super(line, column);
        this.varName = varName;
        this.expression = expression;
    }

    public String getVarName() {
        return varName;
    }

    public ExpressionNode getExpression() {
        return expression;
    }

    @Override
    public <R> R accept(ASTVisitor<R> visitor) {
        return visitor.visitAssign(this);
    }
}
