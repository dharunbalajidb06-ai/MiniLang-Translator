package com.minilang.codegen.java;

import com.minilang.ast.*;
import com.minilang.codegen.CodeGenerator;
import com.minilang.lexer.TokenType;
import com.minilang.symbol.Symbol;
import com.minilang.symbol.SymbolTable;
import com.minilang.symbol.Type;

/**
 * Generates clean, standard Java 17 source code from the MiniLang AST.
 */
public class JavaCodeGenerator implements CodeGenerator, ASTVisitor<String> {
    private SymbolTable symbolTable;
    private int indentLevel = 2; // Inside main method
    private boolean usesScanner = false;

    @Override
    public String getTargetLanguageName() {
        return "Java";
    }

    @Override
    public String getFileExtension() {
        return ".java";
    }

    @Override
    public String generate(ProgramNode ast, SymbolTable symbolTable) {
        this.symbolTable = symbolTable;
        this.indentLevel = 2;
        this.usesScanner = checkIfUsesScanner(ast);

        StringBuilder body = new StringBuilder();
        if (usesScanner) {
            body.append(getIndent()).append("Scanner scanner = new Scanner(System.in);\n");
        }

        for (StatementNode stmt : ast.getStatements()) {
            body.append(stmt.accept(this)).append("\n");
        }

        StringBuilder fullClass = new StringBuilder();
        fullClass.append("// Generated Java code from MiniLang\n");
        if (usesScanner) {
            fullClass.append("import java.util.Scanner;\n\n");
        } else {
            fullClass.append("\n");
        }

        fullClass.append("public class GeneratedProgram {\n");
        fullClass.append("    public static void main(String[] args) {\n");
        fullClass.append(body);
        if (usesScanner) {
            fullClass.append("        scanner.close();\n");
        }
        fullClass.append("    }\n");
        fullClass.append("}\n");

        return fullClass.toString();
    }

    private boolean checkIfUsesScanner(ASTNode node) {
        if (node instanceof ProgramNode prog) {
            return prog.getStatements().stream().anyMatch(this::checkIfUsesScanner);
        } else if (node instanceof BlockNode block) {
            return block.getStatements().stream().anyMatch(this::checkIfUsesScanner);
        } else if (node instanceof IfNode ifNode) {
            return checkIfUsesScanner(ifNode.getThenBranch()) || (ifNode.hasElseBranch() && checkIfUsesScanner(ifNode.getElseBranch()));
        } else if (node instanceof WhileNode whileNode) {
            return checkIfUsesScanner(whileNode.getBody());
        }
        return node instanceof InputNode;
    }

    private String getIndent() {
        return "    ".repeat(indentLevel);
    }

    private String mapType(Type type) {
        return switch (type) {
            case INT -> "int";
            case FLOAT -> "double";
            case STRING -> "String";
            case BOOLEAN -> "boolean";
            default -> "Object";
        };
    }

    @Override
    public String visitProgram(ProgramNode node) {
        return "";
    }

    @Override
    public String visitVarDecl(VarDeclNode node) {
        String typeStr = mapType(node.getType());
        if (node.hasInitializer()) {
            return getIndent() + typeStr + " " + node.getVarName() + " = " + node.getInitializer().accept(this) + ";";
        } else {
            return getIndent() + typeStr + " " + node.getVarName() + ";";
        }
    }

    @Override
    public String visitAssign(AssignNode node) {
        return getIndent() + node.getVarName() + " = " + node.getExpression().accept(this) + ";";
    }

    @Override
    public String visitPrint(PrintNode node) {
        return getIndent() + "System.out.println(" + node.getExpression().accept(this) + ");";
    }

    @Override
    public String visitInput(InputNode node) {
        Type varType = symbolTable.lookup(node.getVarName())
                .map(Symbol::getType)
                .orElse(Type.STRING);

        String readExpr = switch (varType) {
            case INT -> "scanner.nextInt()";
            case FLOAT -> "scanner.nextDouble()";
            case BOOLEAN -> "scanner.nextBoolean()";
            default -> "scanner.nextLine()";
        };

        return getIndent() + node.getVarName() + " = " + readExpr + ";";
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
