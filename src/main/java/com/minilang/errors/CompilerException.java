package com.minilang.errors;

/**
 * Base class for all compilation and translation exceptions in MiniLang.
 * Tracks the exact line and column number where the error occurred in source code.
 */
public class CompilerException extends RuntimeException {
    private final String errorType;
    private final int line;
    private final int column;
    private final String rawMessage;

    public CompilerException(String errorType, String message, int line, int column) {
        super(String.format("[%s Error] Line %d, Column %d: %s", errorType, line, column, message));
        this.errorType = errorType;
        this.line = line;
        this.column = column;
        this.rawMessage = message;
    }

    public String getErrorType() {
        return errorType;
    }

    public int getLine() {
        return line;
    }

    public int getColumn() {
        return column;
    }

    public String getRawMessage() {
        return rawMessage;
    }
}
