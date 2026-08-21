package com.minilang;

import com.minilang.ast.ProgramNode;
import com.minilang.errors.SemanticException;
import com.minilang.lexer.Lexer;
import com.minilang.lexer.Token;
import com.minilang.parser.Parser;
import com.minilang.semantic.SemanticAnalyzer;
import com.minilang.symbol.Symbol;
import com.minilang.symbol.Type;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class SemanticAnalyzerTest {

    private SemanticAnalyzer analyze(String code) {
        Lexer lexer = new Lexer(code);
        List<Token> tokens = lexer.tokenize();
        Parser parser = new Parser(tokens);
        ProgramNode program = parser.parse();
        SemanticAnalyzer analyzer = new SemanticAnalyzer();
        analyzer.analyze(program);
        return analyzer;
    }

    @Test
    @DisplayName("Test valid variable declarations and assignments")
    void testValidProgram() {
        String code = """
                int a = 10;
                int b = 20;
                int result;
                result = a + b;
                print(result);
                """;
        SemanticAnalyzer analyzer = analyze(code);
        Optional<Symbol> resultSym = analyzer.getSymbolTable().lookup("result");
        assertTrue(resultSym.isPresent());
        assertEquals(Type.INT, resultSym.get().getType());
        assertTrue(resultSym.get().isInitialized());
    }

    @Test
    @DisplayName("Test undeclared variable error: a = 10;")
    void testUndeclaredVariable() {
        String code = "a = 10;";
        SemanticException ex = assertThrows(SemanticException.class, () -> analyze(code));
        assertTrue(ex.getMessage().contains("Variable 'a' has not been declared"));
    }

    @Test
    @DisplayName("Test duplicate declaration in same scope")
    void testDuplicateDeclaration() {
        String code = """
                int a = 10;
                int a = 20;
                """;
        SemanticException ex = assertThrows(SemanticException.class, () -> analyze(code));
        assertTrue(ex.getMessage().contains("Duplicate declaration"));
    }

    @Test
    @DisplayName("Test type mismatch: int a = 'hello';")
    void testTypeMismatchInit() {
        String code = "int a = \"hello\";";
        SemanticException ex = assertThrows(SemanticException.class, () -> analyze(code));
        assertTrue(ex.getMessage().contains("Cannot assign string to int"));
    }

    @Test
    @DisplayName("Test type mismatch on assignment: int a = 10; a = true;")
    void testTypeMismatchAssign() {
        String code = """
                int a = 10;
                a = true;
                """;
        SemanticException ex = assertThrows(SemanticException.class, () -> analyze(code));
        assertTrue(ex.getMessage().contains("Cannot assign boolean to int"));
    }

    @Test
    @DisplayName("Test non-boolean condition in if statement: if (10) { ... }")
    void testNonBooleanIfCondition() {
        String code = """
                if (10) {
                    print(1);
                }
                """;
        SemanticException ex = assertThrows(SemanticException.class, () -> analyze(code));
        assertTrue(ex.getMessage().contains("Condition in 'if' statement must be of boolean type"));
    }

    @Test
    @DisplayName("Test implicit promotion: int can be assigned to float")
    void testIntToFloatPromotion() {
        String code = """
                float f = 10;
                """;
        assertDoesNotThrow(() -> analyze(code));
    }

    @Test
    @DisplayName("Test nested block scoping and variable shadowing")
    void testNestedScope() {
        String code = """
                int x = 10;
                {
                    int x = 20;
                    print(x);
                }
                print(x);
                """;
        assertDoesNotThrow(() -> analyze(code));
    }
}
