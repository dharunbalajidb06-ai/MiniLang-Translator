package com.minilang.ast;

import java.util.List;

/**
 * Visualizer/Pretty Printer for MiniLang Abstract Syntax Trees.
 * Generates clean indented ASCII tree structures.
 */
public class ASTPrinter implements ASTVisitor<String> {

    public String print(ASTNode root) {
        if (root == null) return "<empty tree>";
        return root.accept(this);
    }

    @Override
    public String visitProgram(ProgramNode node) {
        StringBuilder sb = new StringBuilder();
        sb.append("Program\n");
        List<StatementNode> stmts = node.getStatements();
        for (int i = 0; i < stmts.size(); i++) {
            boolean isLast = (i == stmts.size() - 1);
            sb.append(indent(stmts.get(i).accept(this), isLast ? "└── " : "├── ", isLast ? "    " : "│   "));
        }
        return sb.toString();
    }

    @Override
    public String visitVarDecl(VarDeclNode node) {
        StringBuilder sb = new StringBuilder();
        sb.append("VarDecl (").append(node.getType()).append(" ").append(node.getVarName()).append(")\n");
        if (node.hasInitializer()) {
            sb.append(indent(node.getInitializer().accept(this), "└── Init: ", "    "));
        }
        return sb.toString();
    }

    @Override
    public String visitAssign(AssignNode node) {
        StringBuilder sb = new StringBuilder();
        sb.append("Assign (").append(node.getVarName()).append(" =)\n");
        sb.append(indent(node.getExpression().accept(this), "└── ", "    "));
        return sb.toString();
    }

    @Override
    public String visitPrint(PrintNode node) {
        StringBuilder sb = new StringBuilder();
        sb.append("Print\n");
        sb.append(indent(node.getExpression().accept(this), "└── ", "    "));
        return sb.toString();
    }

    @Override
    public String visitInput(InputNode node) {
        return "Input (" + node.getVarName() + ")\n";
    }

    @Override
    public String visitIf(IfNode node) {
        StringBuilder sb = new StringBuilder();
        sb.append("If\n");
        sb.append(indent(node.getCondition().accept(this), "├── Condition: ", "│   "));
        if (node.hasElseBranch()) {
            sb.append(indent(node.getThenBranch().accept(this), "├── Then: ", "│   "));
            sb.append(indent(node.getElseBranch().accept(this), "└── Else: ", "    "));
        } else {
            sb.append(indent(node.getThenBranch().accept(this), "└── Then: ", "    "));
        }
        return sb.toString();
    }

    @Override
    public String visitWhile(WhileNode node) {
        StringBuilder sb = new StringBuilder();
        sb.append("While\n");
        sb.append(indent(node.getCondition().accept(this), "├── Condition: ", "│   "));
        sb.append(indent(node.getBody().accept(this), "└── Body: ", "    "));
        return sb.toString();
    }

    @Override
    public String visitBlock(BlockNode node) {
        StringBuilder sb = new StringBuilder();
        sb.append("Block\n");
        List<StatementNode> stmts = node.getStatements();
        for (int i = 0; i < stmts.size(); i++) {
            boolean isLast = (i == stmts.size() - 1);
            sb.append(indent(stmts.get(i).accept(this), isLast ? "└── " : "├── ", isLast ? "    " : "│   "));
        }
        return sb.toString();
    }

    @Override
    public String visitBinaryExpr(BinaryExprNode node) {
        StringBuilder sb = new StringBuilder();
        sb.append("BinaryOp (").append(node.getOperator()).append(")\n");
        sb.append(indent(node.getLeft().accept(this), "├── Left: ", "│   "));
        sb.append(indent(node.getRight().accept(this), "└── Right: ", "    "));
        return sb.toString();
    }

    @Override
    public String visitUnaryExpr(UnaryExprNode node) {
        StringBuilder sb = new StringBuilder();
        sb.append("UnaryOp (").append(node.getOperator()).append(")\n");
        sb.append(indent(node.getOperand().accept(this), "└── ", "    "));
        return sb.toString();
    }

    @Override
    public String visitLiteral(LiteralNode node) {
        return "Literal (" + node.getType() + ": " + node.getValue() + ")\n";
    }

    @Override
    public String visitVariable(VariableNode node) {
        return "Variable (" + node.getName() + ")\n";
    }

    private String indent(String text, String firstPrefix, String restPrefix) {
        String[] lines = text.split("\n");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < lines.length; i++) {
            if (lines[i].isEmpty()) continue;
            if (i == 0) {
                sb.append(firstPrefix).append(lines[i]).append("\n");
            } else {
                sb.append(restPrefix).append(lines[i]).append("\n");
            }
        }
        return sb.toString();
    }
}
