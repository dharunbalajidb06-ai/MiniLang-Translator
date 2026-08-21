package com.minilang.ir;

/**
 * Three-Address Code (TAC) operation codes.
 */
public enum TACOp {
    // Arithmetic
    ADD,
    SUB,
    MUL,
    DIV,
    MOD,

    // Relational
    GT,
    LT,
    GTE,
    LTE,
    EQ,
    NEQ,

    // Logical
    AND,
    OR,
    NOT,

    // Unary
    NEG,

    // Assignment & Declarations
    ASSIGN,
    VAR_DECL,

    // Control Flow
    LABEL,
    GOTO,
    IF_FALSE_GOTO,
    IF_GOTO,

    // I/O
    PRINT,
    INPUT
}
