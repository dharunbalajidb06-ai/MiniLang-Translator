package com.minilang;

import com.minilang.ast.ProgramNode;
import com.minilang.ir.TACProgram;
import com.minilang.lexer.Token;
import com.minilang.symbol.SymbolTable;

import java.util.List;
import java.util.Map;

/**
 * Encapsulates the complete end-to-end output of all compiler/translator phases.
 */
public class TranslationResult {
    private final List<Token> tokens;
    private final ProgramNode ast;
    private final String astTreeString;
    private final SymbolTable symbolTable;
    private final TACProgram rawTAC;
    private final TACProgram optimizedTAC;
    private final String pythonCode;
    private final String javaCode;
    private final String cCode;
    private final Map<String, Double> phaseTimingsMs;

    public TranslationResult(List<Token> tokens, ProgramNode ast, String astTreeString,
                             SymbolTable symbolTable, TACProgram rawTAC, TACProgram optimizedTAC,
                             String pythonCode, String javaCode, String cCode,
                             Map<String, Double> phaseTimingsMs) {
        this.tokens = tokens;
        this.ast = ast;
        this.astTreeString = astTreeString;
        this.symbolTable = symbolTable;
        this.rawTAC = rawTAC;
        this.optimizedTAC = optimizedTAC;
        this.pythonCode = pythonCode;
        this.javaCode = javaCode;
        this.cCode = cCode;
        this.phaseTimingsMs = phaseTimingsMs;
    }

    public List<Token> getTokens() { return tokens; }
    public ProgramNode getAst() { return ast; }
    public String getAstTreeString() { return astTreeString; }
    public SymbolTable getSymbolTable() { return symbolTable; }
    public TACProgram getRawTAC() { return rawTAC; }
    public TACProgram getOptimizedTAC() { return optimizedTAC; }
    public String getPythonCode() { return pythonCode; }
    public String getJavaCode() { return javaCode; }
    public String getCCode() { return cCode; }
    public Map<String, Double> getPhaseTimingsMs() { return phaseTimingsMs; }
}
