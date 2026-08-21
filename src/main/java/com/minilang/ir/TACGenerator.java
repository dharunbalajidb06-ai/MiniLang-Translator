package com.minilang.ir;

import com.minilang.ast.*;
import com.minilang.lexer.TokenType;
import com.minilang.symbol.Type;

/**
 * Generates Three-Address Code (TAC) Intermediate Representation from the MiniLang AST.
 */
public class TACGenerator implements ASTVisitor<String> {
    private final TACProgram program = new TACProgram();
    private int tempCount = 1;
    private int labelCount = 1;

    public TACProgram generate(ASTNode root) {
        tempCount = 1;
        labelCount = 1;
        if (root != null) {
            root.accept(this);
        }
        return program;
    }

    public String newTemp() {
        return "t" + (tempCount++);
    }

    public String newLabel() {
        return "L" + (labelCount++);
    }

    @Override
    public String visitProgram(ProgramNode node) {
        for (StatementNode stmt : node.getStatements()) {
            stmt.accept(this);
        }
        return null;
    }

    @Override
    public String visitVarDecl(VarDeclNode node) {
        program.add(new TACInstruction(TACOp.VAR_DECL, null, null, node.getVarName(), node.getType()));
        if (node.hasInitializer()) {
            String initRes = node.getInitializer().accept(this);
            program.add(new TACInstruction(TACOp.ASSIGN, initRes, null, node.getVarName(), node.getType()));
        }
        return null;
    }

    @Override
    public String visitAssign(AssignNode node) {
        String val = node.getExpression().accept(this);
        program.add(new TACInstruction(TACOp.ASSIGN, val, null, node.getVarName(), node.getExpression().getEvaluatedType()));
        return null;
    }

    @Override
    public String visitPrint(PrintNode node) {
        String res = node.getExpression().accept(this);
        program.add(new TACInstruction(TACOp.PRINT, res, null, null, node.getExpression().getEvaluatedType()));
        return null;
    }

    @Override
    public String visitInput(InputNode node) {
        program.add(new TACInstruction(TACOp.INPUT, null, null, node.getVarName()));
        return null;
    }

    @Override
    public String visitIf(IfNode node) {
        String cond = node.getCondition().accept(this);
        String labelEnd = newLabel();

        if (node.hasElseBranch()) {
            String labelElse = newLabel();
            program.add(new TACInstruction(TACOp.IF_FALSE_GOTO, cond, null, labelElse));
            node.getThenBranch().accept(this);
            program.add(new TACInstruction(TACOp.GOTO, null, null, labelEnd));
            program.add(new TACInstruction(TACOp.LABEL, null, null, labelElse));
            node.getElseBranch().accept(this);
            program.add(new TACInstruction(TACOp.LABEL, null, null, labelEnd));
        } else {
            program.add(new TACInstruction(TACOp.IF_FALSE_GOTO, cond, null, labelEnd));
            node.getThenBranch().accept(this);
            program.add(new TACInstruction(TACOp.LABEL, null, null, labelEnd));
        }
        return null;
    }

    @Override
    public String visitWhile(WhileNode node) {
        String labelStart = newLabel();
        String labelEnd = newLabel();

        program.add(new TACInstruction(TACOp.LABEL, null, null, labelStart));
        String cond = node.getCondition().accept(this);
        program.add(new TACInstruction(TACOp.IF_FALSE_GOTO, cond, null, labelEnd));
        node.getBody().accept(this);
        program.add(new TACInstruction(TACOp.GOTO, null, null, labelStart));
        program.add(new TACInstruction(TACOp.LABEL, null, null, labelEnd));
        return null;
    }

    @Override
    public String visitBlock(BlockNode node) {
        for (StatementNode stmt : node.getStatements()) {
            stmt.accept(this);
        }
        return null;
    }

    @Override
    public String visitBinaryExpr(BinaryExprNode node) {
        String left = node.getLeft().accept(this);
        String right = node.getRight().accept(this);
        String temp = newTemp();
        TACOp op = mapBinaryOp(node.getOperator());

        program.add(new TACInstruction(op, left, right, temp, node.getEvaluatedType()));
        return temp;
    }

    @Override
    public String visitUnaryExpr(UnaryExprNode node) {
        String operand = node.getOperand().accept(this);
        String temp = newTemp();
        TACOp op = (node.getOperator() == TokenType.MINUS) ? TACOp.NEG : TACOp.NOT;

        program.add(new TACInstruction(op, operand, null, temp, node.getEvaluatedType()));
        return temp;
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

    private TACOp mapBinaryOp(TokenType tokenType) {
        return switch (tokenType) {
            case PLUS -> TACOp.ADD;
            case MINUS -> TACOp.SUB;
            case STAR -> TACOp.MUL;
            case SLASH -> TACOp.DIV;
            case PERCENT -> TACOp.MOD;
            case GT -> TACOp.GT;
            case GTE -> TACOp.GTE;
            case LT -> TACOp.LT;
            case LTE -> TACOp.LTE;
            case EQ -> TACOp.EQ;
            case NEQ -> TACOp.NEQ;
            case AND -> TACOp.AND;
            case OR -> TACOp.OR;
            default -> throw new IllegalArgumentException("Unsupported binary operator for TAC: " + tokenType);
        };
    }
}
