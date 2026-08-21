package com.minilang.lexer;

/**
 * Enumeration of all Token Types recognized in MiniLang.
 */
public enum TokenType {
    // Keywords - Data Types
    KW_INT,         // "int"
    KW_FLOAT,       // "float"
    KW_STRING,      // "string"
    KW_BOOLEAN,     // "boolean"

    // Keywords - Control Flow & I/O
    KW_IF,          // "if"
    KW_ELSE,        // "else"
    KW_WHILE,       // "while"
    KW_PRINT,       // "print"
    KW_INPUT,       // "input"

    // Literals
    INT_LITERAL,    // e.g. 10, 42
    FLOAT_LITERAL,  // e.g. 25.5, 3.14
    STRING_LITERAL, // e.g. "hello world"
    BOOLEAN_LITERAL,// "true", "false"

    // Identifiers
    IDENTIFIER,     // e.g. a, price, totalSum

    // Arithmetic Operators
    PLUS,           // "+"
    MINUS,          // "-"
    STAR,           // "*"
    SLASH,          // "/"
    PERCENT,        // "%"

    // Relational Operators
    GT,             // ">"
    LT,             // "<"
    GTE,            // ">="
    LTE,            // "<="
    EQ,             // "=="
    NEQ,            // "!="

    // Logical Operators
    AND,            // "&&"
    OR,             // "||"
    NOT,            // "!"

    // Assignment Operator
    ASSIGN,         // "="

    // Delimiters and Separators
    SEMICOLON,      // ";"
    COMMA,          // ","
    LPAREN,         // "("
    RPAREN,         // ")"
    LBRACE,         // "{"
    RBRACE,         // "}"

    // End Of File
    EOF
}
