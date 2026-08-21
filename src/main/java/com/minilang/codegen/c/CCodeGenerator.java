package com.minilang.codegen.c;

import com.minilang.ast.*;
import com.minilang.codegen.CodeGenerator;
import com.minilang.lexer.TokenType;
import com.minilang.symbol.Symbol;
import com.minilang.symbol.SymbolTable;
import com.minilang.symbol.Type;

/**
 * Generates standard ISO C99 / C11 source code from the MiniLang AST.
 */
public class CCodeGenerator implements CodeGenerator, ASTVisitor<String> {
    private SymbolTable symbolTable;
    private int indentLevel = 1;

    @Override
    public String getTargetLanguageName() {
        return "C";
    }

    @Override
    public String getFileExtension() {
        return ".c";
    }

    @Override
    public String generate(ProgramNode ast, SymbolTable symbolTable) {
        this.symbolTable = symbolTable;
        this.indentLevel = 1;

        StringBuilder body = new StringBuilder();
        for (StatementNode stmt : ast.getStatements()) {
            body.append(stmt.accept(this)).append("\n");
        }

        StringBuilder fullCode = new StringBuilder();
        fullCode.append("// Generated C code from MiniLang\n");
        fullCode.append("#include <stdio.h>\n");
        fullCode.append("#include <stdbool.h>\n");
        fullCode.append("#include <string.h>\n\n");
        fullCode.append("int main() {\n");
        fullCode.append(body);
        fullCode.append("    return 0;\n");
        fullCode.append("}\n");

        return fullCode.toString();
    }

    private String getIndent() {
        return "    ".repeat(indentLevel);
    }

    private String mapType(Type type) {
        return switch (type) {
            case INT -> "int";
            case FLOAT -> "double";
            case STRING -> "char";
            case BOOLEAN -> "bool";
            default -> "void";
        };
    }

    @Override
    public String visitProgram(ProgramNode node) {
        return "";
    }

    @Override
    public String visitVarDecl(VarDeclNode node) {
        if (node.getType() == Type.STRING) {
            if (node.hasInitializer()) {
                return getIndent() + "char " + node.getVarName() + "[256] = " + node.getInitializer().accept(this) + ";";
            } else {
                return getIndent() + "char " + node.getVarName() + "[256] = \"\";";
            }
        }

        String typeStr = mapType(node.getType());
        if (node.hasInitializer()) {
            return getIndent() + typeStr + " " + node.getVarName() + " = " + node.getInitializer().accept(this) + ";";
        } else {
            return getIndent() + typeStr + " " + node.getVarName() + ";";
        }
    }

    @Override
    public String visitAssign(AssignNode node) {
        Type varType = symbolTable.lookup(node.getVarName())
                .map(Symbol::getType)
                .orElse(Type.UNKNOWN);

        if (varType == Type.STRING) {
            return getIndent() + "snprintf(" + node.getVarName() + ", sizeof(" + node.getVarName() + "), \"%s\", " + node.getExpression().accept(this) + ");";
        }
        return getIndent() + node.getVarName() + " = " + node.getExpression().accept(this) + ";";
    }

    @Override
    public String visitPrint(PrintNode node) {
        Type exprType = node.getExpression().getEvaluatedType();
        String exprStr = node.getExpression().accept(this);

        return switch (exprType) {
            case INT -> getIndent() + "printf(\"%d\\n\", " + exprStr + ");";
            case FLOAT -> getIndent() + "printf(\"%f\\n\", " + exprStr + ");";
            case STRING -> getIndent() + "printf(\"%s\\n\", " + exprStr + ");";
            case BOOLEAN -> getIndent() + "printf(\"%s\\n\", (" + exprStr + ") ? \"true\" : \"false\");";
            default -> getIndent() + "printf(\"%d\\n\", " + exprStr + ");";
        };
    }

    @Override
    public String visitInput(InputNode node) {
        Type varType = symbolTable.lookup(node.getVarName())
                .map(Symbol::getType)
                .orElse(Type.STRING);

        return switch (varType) {
            case INT -> getIndent() + "scanf(\"%d\", &" + node.getVarName() + ");";
            case FLOAT -> getIndent() + "scanf(\"%lf\", &" + node.getVarName() + ");";
            case BOOLEAN -> getIndent() + "scanf(\"%d\", &" + node.getVarName() + ");";
            default -> getIndent() + "scanf(\"%255s\", " + node.getVarName() + ");";
        };
    }

    @Override
    public String visitIf(IfNode node) {
        StringBuilder sb = new StringBuilder();
        sb.append(getIndent()).append("if (").append(node.getCondition().accept(this)).append(") {\n");

        indentLevel++;
        sb.append(renderBlockContent(node.getThenBranch()));
        indentLevel--;

        if (node.hasElseBranch()) {
            sb.append(getIndent()).append("} else {\n");
            indentLevel++;
            sb.append(renderBlockContent(node.getElseBranch()));
            indentLevel--;
        }
        sb.append(getIndent()).append("}");
        return sb.toString();
    }

    @Override
    public String visitWhile(WhileNode node) {
        StringBuilder sb = new StringBuilder();
        sb.append(getIndent()).append("while (").append(node.getCondition().accept(this)).append(") {\n");

        indentLevel++;
        sb.append(renderBlockContent(node.getBody()));
        indentLevel--;

        sb.append(getIndent()).append("}");
        return sb.toString();
    }

    @Override
    public String visitBlock(BlockNode node) {
        StringBuilder sb = new StringBuilder();
        sb.append(getIndent()).append("{\n");
        indentLevel++;
        for (StatementNode stmt : node.getStatements()) {
            sb.append(stmt.accept(this)).append("\n");
        }
        indentLevel--;
        sb.append(getIndent()).append("}");
        return sb.toString();
    }

    private String renderBlockContent(StatementNode stmt) {
        if (stmt instanceof BlockNode block) {
            StringBuilder sb = new StringBuilder();
            for (StatementNode s : block.getStatements()) {
                sb.append(s.accept(this)).append("\n");
            }
            return sb.toString();
        } else {
            return stmt.accept(this) + "\n";
        }
    }

    @Override
    public String visitBinaryExpr(BinaryExprNode node) {
        String left = node.getLeft().accept(this);
        String right = node.getRight().accept(this);

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
            case AND -> "&&";
            case OR -> "||";
            default -> throw new IllegalArgumentException("Unknown operator: " + node.getOperator());
        };

        return left + " " + op + " " + right;
    }

    @Override
    public String visitUnaryExpr(UnaryExprNode node) {
        String operand = node.getOperand().accept(this);
        if (node.getOperator() == TokenType.NOT) {
            return "!" + operand;
        } else if (node.getOperator() == TokenType.MINUS) {
            return "-" + operand;
        }
        return operand;
    }

    @Override
    public String visitLiteral(LiteralNode node) {
        if (node.getType() == Type.STRING) {
            return "\"" + node.getValue() + "\"";
        }
        return String.valueOf(node.getValue());
    }

    @Override
    public String visitVariable(VariableNode node) {
        return node.getName();
    }
}
