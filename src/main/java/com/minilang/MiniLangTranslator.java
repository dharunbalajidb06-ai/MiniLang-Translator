package com.minilang;

import com.minilang.ast.ASTPrinter;
import com.minilang.ast.ProgramNode;
import com.minilang.codegen.c.CCodeGenerator;
import com.minilang.codegen.java.JavaCodeGenerator;
import com.minilang.codegen.python.PythonCodeGenerator;
import com.minilang.ir.TACGenerator;
import com.minilang.ir.TACProgram;
import com.minilang.lexer.Lexer;
import com.minilang.lexer.Token;
import com.minilang.optimizer.Optimizer;
import com.minilang.parser.Parser;
import com.minilang.semantic.SemanticAnalyzer;
import com.minilang.symbol.SymbolTable;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Main coordinator pipeline for the MiniLang Translator.
 * Orchestrates Lexer -> Parser -> AST -> Semantic Analysis -> TAC IR -> Optimizer -> Code Generators.
 */
public class MiniLangTranslator {
    private final PythonCodeGenerator pythonGen = new PythonCodeGenerator();
    private final JavaCodeGenerator javaGen = new JavaCodeGenerator();
    private final CCodeGenerator cGen = new CCodeGenerator();
    private final Optimizer optimizer = new Optimizer();
    private final ASTPrinter astPrinter = new ASTPrinter();

    /**
     * Executes all compiler/translator phases on the given MiniLang source code.
     */
    public TranslationResult translate(String sourceCode) {
        Map<String, Double> timings = new LinkedHashMap<>();

        // Phase 1: Lexical Analysis
        long t0 = System.nanoTime();
        Lexer lexer = new Lexer(sourceCode);
        List<Token> tokens = lexer.tokenize();
        long t1 = System.nanoTime();
        timings.put("Lexical Analysis", (t1 - t0) / 1_000_000.0);

        // Phase 2: Syntax Analysis (Parser & AST)
        Parser parser = new Parser(tokens);
        ProgramNode ast = parser.parse();
        long t2 = System.nanoTime();
        timings.put("Syntax Analysis (AST)", (t2 - t1) / 1_000_000.0);

        // Phase 3: Semantic Analysis & Symbol Table
        SemanticAnalyzer semanticAnalyzer = new SemanticAnalyzer();
        semanticAnalyzer.analyze(ast);
        SymbolTable symbolTable = semanticAnalyzer.getSymbolTable();
        long t3 = System.nanoTime();
        timings.put("Semantic Analysis", (t3 - t2) / 1_000_000.0);

        // Phase 4: Intermediate Code Generation (TAC)
        TACGenerator tacGen = new TACGenerator();
        TACProgram rawTAC = tacGen.generate(ast);
        long t4 = System.nanoTime();
        timings.put("TAC Generation", (t4 - t3) / 1_000_000.0);

        // Phase 5: Code Optimization
        TACProgram optimizedTAC = optimizer.optimize(rawTAC);
        long t5 = System.nanoTime();
        timings.put("Optimization", (t5 - t4) / 1_000_000.0);

        // Phase 6: Target Code Generation (Python, Java, C)
        String pythonCode = pythonGen.generate(ast, symbolTable);
        String javaCode = javaGen.generate(ast, symbolTable);
        String cCode = cGen.generate(ast, symbolTable);
        long t6 = System.nanoTime();
        timings.put("Code Generation", (t6 - t5) / 1_000_000.0);
        timings.put("Total Translation Time", (t6 - t0) / 1_000_000.0);

        String astTreeString = astPrinter.print(ast);

        return new TranslationResult(
                tokens,
                ast,
                astTreeString,
                symbolTable,
                rawTAC,
                optimizedTAC,
                pythonCode,
                javaCode,
                cCode,
                timings
        );
    }
}
