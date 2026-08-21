package com.minilang.errors;

/**
 * Thrown when the Parser encounters unexpected tokens or violates MiniLang grammar.
 */
public class SyntaxException extends CompilerException {
    public SyntaxException(String message, int line, int column) {
        super("Syntax", message, line, column);
    }
}
