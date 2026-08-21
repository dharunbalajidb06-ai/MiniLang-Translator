package com.minilang.ir;

import com.minilang.symbol.Type;

import java.util.Objects;

/**
 * Represents a single Three-Address Code (TAC) quad/instruction:
 * format: result = arg1 op arg2
 */
public class TACInstruction {
    private final TACOp op;
    private final String arg1;
    private final String arg2;
    private final String result;
    private final Type type;

    public TACInstruction(TACOp op, String arg1, String arg2, String result, Type type) {
        this.op = op;
        this.arg1 = arg1;
        this.arg2 = arg2;
        this.result = result;
        this.type = type != null ? type : Type.UNKNOWN;
    }

    public TACInstruction(TACOp op, String arg1, String arg2, String result) {
        this(op, arg1, arg2, result, Type.UNKNOWN);
    }

    public TACOp getOp() {
        return op;
    }

    public String getArg1() {
        return arg1;
    }

    public String getArg2() {
        return arg2;
    }

    public String getResult() {
        return result;
    }

    public Type getType() {
        return type;
    }

    @Override
    public String toString() {
        return switch (op) {
            case LABEL -> result + ":";
            case GOTO -> "goto " + result;
            case IF_FALSE_GOTO -> "if False " + arg1 + " goto " + result;
            case IF_GOTO -> "if " + arg1 + " goto " + result;
            case PRINT -> "print " + arg1;
            case INPUT -> "input " + result;
            case VAR_DECL -> "decl " + type + " " + result;
            case ASSIGN -> result + " = " + arg1;
            case NEG -> result + " = -" + arg1;
            case NOT -> result + " = !" + arg1;
            case ADD -> result + " = " + arg1 + " + " + arg2;
            case SUB -> result + " = " + arg1 + " - " + arg2;
            case MUL -> result + " = " + arg1 + " * " + arg2;
            case DIV -> result + " = " + arg1 + " / " + arg2;
            case MOD -> result + " = " + arg1 + " % " + arg2;
            case GT -> result + " = " + arg1 + " > " + arg2;
            case LT -> result + " = " + arg1 + " < " + arg2;
            case GTE -> result + " = " + arg1 + " >= " + arg2;
            case LTE -> result + " = " + arg1 + " <= " + arg2;
            case EQ -> result + " = " + arg1 + " == " + arg2;
            case NEQ -> result + " = " + arg1 + " != " + arg2;
            case AND -> result + " = " + arg1 + " && " + arg2;
            case OR -> result + " = " + arg1 + " || " + arg2;
        };
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TACInstruction that)) return false;
        return op == that.op &&
                Objects.equals(arg1, that.arg1) &&
                Objects.equals(arg2, that.arg2) &&
                Objects.equals(result, that.result);
    }

    @Override
    public int hashCode() {
        return Objects.hash(op, arg1, arg2, result);
    }
}
