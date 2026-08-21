package com.minilang.codegen;

import com.minilang.ast.ProgramNode;
import com.minilang.symbol.SymbolTable;

/**
 * Interface implemented by all target language code generators (Python, Java, C).
 */
public interface CodeGenerator {
    /**
     * Generates source code in the target programming language.
     *
     * @param ast The validated ProgramNode AST
     * @param symbolTable The symbol table populated during semantic analysis
     * @return Formatted target source code string
     */
    String generate(ProgramNode ast, SymbolTable symbolTable);

    /**
     * Returns the human-readable target language name (e.g. "Python", "Java", "C").
     */
    String getTargetLanguageName();

    /**
     * Returns the standard file extension for the target language (e.g. ".py", ".java", ".c").
     */
    String getFileExtension();
}
