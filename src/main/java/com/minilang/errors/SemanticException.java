package com.minilang.errors;

/**
 * Thrown when the Semantic Analyzer encounters type mismatches, undeclared variables, duplicate variables, or scope violations.
 */
public class SemanticException extends CompilerException {
    public SemanticException(String message, int line, int column) {
        super("Semantic", message, line, column);
    }
}
