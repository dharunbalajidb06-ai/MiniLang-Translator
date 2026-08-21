package com.minilang.errors;

/**
 * Thrown when the Lexer encounters an unrecognized character, unclosed string, or invalid number literal.
 */
public class LexicalException extends CompilerException {
    public LexicalException(String message, int line, int column) {
        super("Lexical", message, line, column);
    }
}
