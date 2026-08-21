package com.minilang;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class CodeGeneratorTest {

    private final MiniLangTranslator translator = new MiniLangTranslator();

    @Test
    @DisplayName("Test Multi-Target Generation for Arithmetic Program")
    void testArithmeticTranslation() {
        String code = """
                int a = 10;
                int b = 20;
                int result;
                result = a + b;
                print(result);
                """;

        TranslationResult result = translator.translate(code);

        // Verify Python output
        String py = result.getPythonCode();
        assertTrue(py.contains("a = 10"));
        assertTrue(py.contains("b = 20"));
        assertTrue(py.contains("result = a + b"));
        assertTrue(py.contains("print(result)"));

        // Verify Java output
        String java = result.getJavaCode();
        assertTrue(java.contains("public class GeneratedProgram"));
        assertTrue(java.contains("int a = 10;"));
        assertTrue(java.contains("int b = 20;"));
        assertTrue(java.contains("result = a + b;"));
        assertTrue(java.contains("System.out.println(result);"));

        // Verify C output
        String c = result.getCCode();
        assertTrue(c.contains("#include <stdio.h>"));
        assertTrue(c.contains("int main()"));
        assertTrue(c.contains("int a = 10;"));
        assertTrue(c.contains("int b = 20;"));
        assertTrue(c.contains("result = a + b;"));
        assertTrue(c.contains("printf(\"%d\\n\", result);"));
    }

    @Test
    @DisplayName("Test Multi-Target Generation for If-Else Statement")
    void testIfElseTranslation() {
        String code = """
                int a = 10;
                if (a > 5) {
                    print(a);
                } else {
                    print(0);
                }
                """;

        TranslationResult result = translator.translate(code);

        // Python
        String py = result.getPythonCode();
        assertTrue(py.contains("if a > 5:"));
        assertTrue(py.contains("    print(a)"));
        assertTrue(py.contains("else:"));
        assertTrue(py.contains("    print(0)"));

        // Java
        String java = result.getJavaCode();
        assertTrue(java.contains("if (a > 5) {"));
        assertTrue(java.contains("    System.out.println(a);"));
        assertTrue(java.contains("} else {"));
        assertTrue(java.contains("    System.out.println(0);"));

        // C
        String c = result.getCCode();
        assertTrue(c.contains("if (a > 5) {"));
        assertTrue(c.contains("    printf(\"%d\\n\", a);"));
        assertTrue(c.contains("} else {"));
        assertTrue(c.contains("    printf(\"%d\\n\", 0);"));
    }

    @Test
    @DisplayName("Test Multi-Target Generation for While Loop")
    void testWhileLoopTranslation() {
        String code = """
                int count = 0;
                while (count < 5) {
                    print(count);
                    count = count + 1;
                }
                """;

        TranslationResult result = translator.translate(code);

        // Python
        String py = result.getPythonCode();
        assertTrue(py.contains("while count < 5:"));
        assertTrue(py.contains("    print(count)"));
        assertTrue(py.contains("    count = count + 1"));

        // Java
        String java = result.getJavaCode();
        assertTrue(java.contains("while (count < 5) {"));
        assertTrue(java.contains("    System.out.println(count);"));
        assertTrue(java.contains("    count = count + 1;"));

        // C
        String c = result.getCCode();
        assertTrue(c.contains("while (count < 5) {"));
        assertTrue(c.contains("    printf(\"%d\\n\", count);"));
        assertTrue(c.contains("    count = count + 1;"));
    }

    @Test
    @DisplayName("Test Multi-Target Generation for Input Statement")
    void testInputStatementTranslation() {
        String code = """
                int age;
                input(age);
                print(age);
                """;

        TranslationResult result = translator.translate(code);

        // Python: age = int(input())
        assertTrue(result.getPythonCode().contains("age = int(input())"));

        // Java: Scanner import & scanner.nextInt()
        assertTrue(result.getJavaCode().contains("import java.util.Scanner;"));
        assertTrue(result.getJavaCode().contains("age = scanner.nextInt();"));

        // C: scanf("%d", &age)
        assertTrue(result.getCCode().contains("scanf(\"%d\", &age);"));
    }

    @Test
    @DisplayName("Test Performance Timing Measurements")
    void testPerformanceTimings() {
        String code = "int a = 10;\nint b = 20;\nprint(a + b);";
        TranslationResult result = translator.translate(code);

        assertNotNull(result.getPhaseTimingsMs());
        assertTrue(result.getPhaseTimingsMs().containsKey("Lexical Analysis"));
        assertTrue(result.getPhaseTimingsMs().containsKey("Syntax Analysis (AST)"));
        assertTrue(result.getPhaseTimingsMs().containsKey("Semantic Analysis"));
        assertTrue(result.getPhaseTimingsMs().containsKey("TAC Generation"));
        assertTrue(result.getPhaseTimingsMs().containsKey("Optimization"));
        assertTrue(result.getPhaseTimingsMs().containsKey("Code Generation"));
        assertTrue(result.getPhaseTimingsMs().containsKey("Total Translation Time"));
    }
}
