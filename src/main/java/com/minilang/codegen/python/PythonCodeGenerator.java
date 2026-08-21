package com.minilang.codegen.python;

import com.minilang.ast.*;
import com.minilang.codegen.CodeGenerator;
import com.minilang.lexer.TokenType;
import com.minilang.symbol.Symbol;
import com.minilang.symbol.SymbolTable;
import com.minilang.symbol.Type;

/**
 * Generates clean, idiomatic Python 3 source code from the MiniLang AST.
 */
public class PythonCodeGenerator implements CodeGenerator, ASTVisitor<String> {
    private SymbolTable symbolTable;
    private int indentLevel = 0;

    @Override
    public String getTargetLanguageName() {
        return "Python";
    }

    @Override
    public String getFileExtension() {
        return ".py";
    }

    @Override
    public String generate(ProgramNode ast, SymbolTable symbolTable) {
        this.symbolTable = symbolTable;
        this.indentLevel = 0;
        return visitProgram(ast);
    }

    private String getIndent() {
        return "    ".repeat(indentLevel);
    }

    @Override
    public String visitProgram(ProgramNode node) {
        StringBuilder sb = new StringBuilder();
        sb.append("# Generated Python code from MiniLang\n\n");
        if (node.getStatements().isEmpty()) {
            sb.append("pass\n");
            return sb.toString();
        }

        for (StatementNode stmt : node.getStatements()) {
            sb.append(stmt.accept(this)).append("\n");
        }
        return sb.toString();
    }

    @Override
    public String visitVarDecl(VarDeclNode node) {
        if (node.hasInitializer()) {
            return getIndent() + node.getVarName() + " = " + node.getInitializer().accept(this);
        } else {
            // Default initialization in Python
            String defaultVal = switch (node.getType()) {
                case INT -> "0";
                case FLOAT -> "0.0";
                case STRING -> "\"\"";
                case BOOLEAN -> "False";
                default -> "None";
            };
            return getIndent() + node.getVarName() + " = " + defaultVal;
        }
    }

    @Override
    public String visitAssign(AssignNode node) {
        return getIndent() + node.getVarName() + " = " + node.getExpression().accept(this);
    }

    @Override
    public String visitPrint(PrintNode node) {
        return getIndent() + "print(" + node.getExpression().accept(this) + ")";
    }

    @Override
    public String visitInput(InputNode node) {
        Type varType = symbolTable.lookup(node.getVarName())
                .map(Symbol::getType)
                .orElse(Type.STRING);

        return switch (varType) {
            case INT -> getIndent() + node.getVarName() + " = int(input())";
            case FLOAT -> getIndent() + node.getVarName() + " = float(input())";
            case BOOLEAN -> getIndent() + node.getVarName() + " = input().strip().lower() == 'true'";
            default -> getIndent() + node.getVarName() + " = input()";
        };
    }

    @Override
    public String visitIf(IfNode node) {
        StringBuilder sb = new StringBuilder();
        sb.append(getIndent()).append("if ").append(node.getCondition().accept(this)).append(":\n");
        
        indentLevel++;
        sb.append(renderBlockOrStatement(node.getThenBranch()));
        indentLevel--;

        if (node.hasElseBranch()) {
            sb.append(getIndent()).append("else:\n");
            indentLevel++;
            sb.append(renderBlockOrStatement(node.getElseBranch()));
            indentLevel--;
        }
        return sb.toString().stripTrailing();
    }

    @Override
    public String visitWhile(WhileNode node) {
        StringBuilder sb = new StringBuilder();
        sb.append(getIndent()).append("while ").append(node.getCondition().accept(this)).append(":\n");

        indentLevel++;
        sb.append(renderBlockOrStatement(node.getBody()));
        indentLevel--;

        return sb.toString().stripTrailing();
    }

    @Override
    public String visitBlock(BlockNode node) {
        if (node.getStatements().isEmpty()) {
            return getIndent() + "pass\n";
        }
        StringBuilder sb = new StringBuilder();
        for (StatementNode stmt : node.getStatements()) {
            sb.append(stmt.accept(this)).append("\n");
        }
        return sb.toString();
    }

    private String renderBlockOrStatement(StatementNode stmt) {
        if (stmt instanceof BlockNode) {
            return stmt.accept(this);
        } else {
            return stmt.accept(this) + "\n";
        }
    }

    @Override
    public String visitBinaryExpr(BinaryExprNode node) {
        String left = node.getLeft().accept(this);
        String right = node.getRight().accept(this);

        // Wrap composite sub-expressions in parentheses for safety
        if (node.getLeft() instanceof BinaryExprNode) {
            left = "(" + left + ")";
        }
        if (node.getRight() instanceof BinaryExprNode) {
            right = "(" + right + ")";
        }

        String op = switch (node.getOperator()) {
            case PLUS -> "+";
            case MINUS -> "-";
            case STAR -> "*";
            case SLASH -> "/";
            case PERCENT -> "%";
            case GT -> ">";
            case LT -> "<";
            case GTE -> ">=";
            case LTE -> "<=";
            case EQ -> "==";
            case NEQ -> "!=";
            case AND -> "and";
            case OR -> "or";
            default -> throw new IllegalArgumentException("Unknown operator: " + node.getOperator());
        };

        return left + " " + op + " " + right;
    }

    @Override
    public String visitUnaryExpr(UnaryExprNode node) {
        String operand = node.getOperand().accept(this);
        if (node.getOperator() == TokenType.NOT) {
            return "not " + operand;
        } else if (node.getOperator() == TokenType.MINUS) {
            return "-" + operand;
        }
        return operand;
    }

    @Override
    public String visitLiteral(LiteralNode node) {
        if (node.getType() == Type.BOOLEAN) {
            return (Boolean) node.getValue() ? "True" : "False";
        } else if (node.getType() == Type.STRING) {
            return "\"" + node.getValue() + "\"";
        }
        return String.valueOf(node.getValue());
    }

    @Override
    public String visitVariable(VariableNode node) {
        return node.getName();
    }
}
