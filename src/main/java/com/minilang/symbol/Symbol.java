package com.minilang.symbol;

import java.util.Objects;

/**
 * Represents a declared symbol (variable) in the Symbol Table.
 */
public class Symbol {
    private final String name;
    private final Type type;
    private final int scopeLevel;
    private final int line;
    private final int column;
    private boolean initialized;

    public Symbol(String name, Type type, int scopeLevel, int line, int column, boolean initialized) {
        this.name = name;
        this.type = type;
        this.scopeLevel = scopeLevel;
        this.line = line;
        this.column = column;
        this.initialized = initialized;
    }

    public String getName() {
        return name;
    }

    public Type getType() {
        return type;
    }

    public int getScopeLevel() {
        return scopeLevel;
    }

    public int getLine() {
        return line;
    }

    public int getColumn() {
        return column;
    }

    public boolean isInitialized() {
        return initialized;
    }

    public void setInitialized(boolean initialized) {
        this.initialized = initialized;
    }

    @Override
    public String toString() {
        return String.format("Symbol(name='%s', type=%s, scope=%d, line=%d, col=%d, init=%s)",
                name, type, scopeLevel, line, column, initialized);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Symbol symbol)) return false;
        return scopeLevel == symbol.scopeLevel && Objects.equals(name, symbol.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, scopeLevel);
    }
}
