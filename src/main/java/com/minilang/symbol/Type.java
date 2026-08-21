package com.minilang.symbol;

/**
 * Represents the data types supported in MiniLang.
 */
public enum Type {
    INT,
    FLOAT,
    STRING,
    BOOLEAN,
    VOID,
    UNKNOWN;

    public boolean isNumeric() {
        return this == INT || this == FLOAT;
    }

    public static Type fromString(String typeStr) {
        if (typeStr == null) return UNKNOWN;
        return switch (typeStr.toLowerCase()) {
            case "int" -> INT;
            case "float" -> FLOAT;
            case "string" -> STRING;
            case "boolean" -> BOOLEAN;
            case "void" -> VOID;
            default -> UNKNOWN;
        };
    }

    @Override
    public String toString() {
        return name().toLowerCase();
    }
}
