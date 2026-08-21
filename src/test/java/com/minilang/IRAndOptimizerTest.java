package com.minilang;

import com.minilang.ast.ProgramNode;
import com.minilang.ir.TACGenerator;
import com.minilang.ir.TACInstruction;
import com.minilang.ir.TACOp;
import com.minilang.ir.TACProgram;
import com.minilang.lexer.Lexer;
import com.minilang.lexer.Token;
import com.minilang.optimizer.Optimizer;
import com.minilang.parser.Parser;
import com.minilang.semantic.SemanticAnalyzer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class IRAndOptimizerTest {

    private TACProgram generateTAC(String code) {
        Lexer lexer = new Lexer(code);
        List<Token> tokens = lexer.tokenize();
        Parser parser = new Parser(tokens);
        ProgramNode program = parser.parse();
        SemanticAnalyzer analyzer = new SemanticAnalyzer();
        analyzer.analyze(program);
        TACGenerator generator = new TACGenerator();
        return generator.generate(program);
    }

    @Test
    @DisplayName("Test TAC generation for arithmetic expression: c = a + b * 10;")
    void testExpressionTAC() {
        String code = """
                int a = 2;
                int b = 3;
                int c;
                c = a + b * 10;
                """;
        TACProgram tac = generateTAC(code);
        String formatted = tac.toFormattedString();

        assertTrue(formatted.contains("decl int a"));
        assertTrue(formatted.contains("decl int b"));
        assertTrue(formatted.contains("decl int c"));
        assertTrue(formatted.contains("t1 = b * 10"));
        assertTrue(formatted.contains("t2 = a + t1"));
        assertTrue(formatted.contains("c = t2"));
    }

    @Test
    @DisplayName("Test TAC generation for If-Else with labels and jumps")
    void testIfElseTAC() {
        String code = """
                int a = 10;
                if (a > 5) {
                    print(a);
                } else {
                    print(0);
                }
                """;
        TACProgram tac = generateTAC(code);
        String formatted = tac.toFormattedString();

        assertTrue(formatted.contains("if False"));
        assertTrue(formatted.contains("goto"));
        assertTrue(formatted.contains("L1:"));
        assertTrue(formatted.contains("L2:"));
    }

    @Test
    @DisplayName("Test Optimizer Constant Folding: int a = 10 + 20;")
    void testConstantFolding() {
        String code = "int a = 10 + 20;";
        TACProgram tac = generateTAC(code);
        Optimizer optimizer = new Optimizer();
        TACProgram optTAC = optimizer.optimize(tac);

        String formatted = optTAC.toFormattedString();
        assertTrue(formatted.contains("a = 30"));
        assertFalse(formatted.contains("10 + 20"));
    }

    @Test
    @DisplayName("Test Optimizer Algebraic Simplification: a = b + 0; c = b * 0;")
    void testAlgebraicSimplification() {
        String code = """
                int b = 5;
                int a = b + 0;
                int c = b * 0;
                """;
        TACProgram tac = generateTAC(code);
        Optimizer optimizer = new Optimizer();
        TACProgram optTAC = optimizer.optimize(tac);

        String formatted = optTAC.toFormattedString();
        assertTrue(formatted.contains("c = 0"));
    }

    @Test
    @DisplayName("Test Optimizer Constant Propagation: a = 10; b = a + 5;")
    void testConstantPropagation() {
        String code = """
                int a = 10;
                int b = a + 5;
                """;
        TACProgram tac = generateTAC(code);
        Optimizer optimizer = new Optimizer();
        TACProgram optTAC = optimizer.optimize(tac);

        String formatted = optTAC.toFormattedString();
        assertTrue(formatted.contains("b = 15"));
    }
}
