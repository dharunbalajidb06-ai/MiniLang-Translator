package com.minilang.lexer;

import java.util.Objects;

/**
 * Represents an individual Token produced by the Lexical Analyzer.
 */
public class Token {
    private final TokenType type;
    private final String lexeme;
    private final Object literalValue;
    private final int line;
    private final int column;

    public Token(TokenType type, String lexeme, Object literalValue, int line, int column) {
        this.type = type;
        this.lexeme = lexeme;
        this.literalValue = literalValue;
        this.line = line;
        this.column = column;
    }

    public Token(TokenType type, String lexeme, int line, int column) {
        this(type, lexeme, null, line, column);
    }

    public TokenType getType() {
        return type;
    }

    public String getLexeme() {
        return lexeme;
    }

    public Object getLiteralValue() {
        return literalValue;
    }

    public int getLine() {
        return line;
    }

    public int getColumn() {
        return column;
    }

    @Override
    public String toString() {
        if (literalValue != null) {
            return String.format("Token(%s, '%s', val=%s, Line:%d, Col:%d)", type, lexeme, literalValue, line, column);
        }
        return String.format("Token(%s, '%s', Line:%d, Col:%d)", type, lexeme, line, column);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Token token)) return false;
        return line == token.line && column == token.column && type == token.type && Objects.equals(lexeme, token.lexeme);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, lexeme, line, column);
    }
}
