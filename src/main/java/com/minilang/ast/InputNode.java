package com.minilang.ast;

/**
 * Represents a user input statement, e.g.:
 * input(a);
 */
public class InputNode extends StatementNode {
    private final String varName;

    public InputNode(String varName, int line, int column) {
        super(line, column);
        this.varName = varName;
    }

    public String getVarName() {
        return varName;
    }

    @Override
    public <R> R accept(ASTVisitor<R> visitor) {
        return visitor.visitInput(this);
    }
}
