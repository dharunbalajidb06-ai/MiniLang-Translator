package com.minilang.symbol;

import com.minilang.errors.SemanticException;

import java.util.*;

/**
 * Scoped Symbol Table maintaining variable definitions across nested block scopes.
 */
public class SymbolTable {
    private final Deque<Map<String, Symbol>> scopes = new ArrayDeque<>();
    private final List<Symbol> allDeclaredSymbols = new ArrayList<>();
    private int currentScopeLevel = 0;

    public SymbolTable() {
        // Initialize global scope (Scope 0)
        enterScope();
    }

    public void enterScope() {
        scopes.push(new LinkedHashMap<>());
        currentScopeLevel++;
    }

    public void exitScope() {
        if (scopes.size() > 1) {
            scopes.pop();
            currentScopeLevel--;
        }
    }

    public int getCurrentScopeLevel() {
        return currentScopeLevel - 1;
    }

    /**
     * Defines a new variable in the current scope.
     * Throws SemanticException if already defined in the current scope.
     */
    public void define(Symbol symbol) {
        Map<String, Symbol> currentScope = scopes.peek();
        if (currentScope == null) return;

        if (currentScope.containsKey(symbol.getName())) {
            throw new SemanticException(
                    "Duplicate declaration: Variable '" + symbol.getName() + "' is already declared in this scope",
                    symbol.getLine(),
                    symbol.getColumn()
            );
        }

        currentScope.put(symbol.getName(), symbol);
        allDeclaredSymbols.add(symbol);
    }

    /**
     * Looks up a symbol starting from innermost scope up to global scope.
     */
    public Optional<Symbol> lookup(String name) {
        for (Map<String, Symbol> scope : scopes) {
            if (scope.containsKey(name)) {
                return Optional.of(scope.get(name));
            }
        }
        return Optional.empty();
    }

    /**
     * Returns all symbols declared throughout the program (useful for GUI table view).
     */
    public List<Symbol> getAllDeclaredSymbols() {
        return Collections.unmodifiableList(allDeclaredSymbols);
    }

    public void clear() {
        scopes.clear();
        allDeclaredSymbols.clear();
        currentScopeLevel = 0;
        enterScope();
    }
}
