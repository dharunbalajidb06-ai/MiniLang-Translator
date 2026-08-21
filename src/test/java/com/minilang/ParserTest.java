package com.minilang;

import com.minilang.ast.*;
import com.minilang.errors.SyntaxException;
import com.minilang.lexer.Lexer;
import com.minilang.lexer.Token;
import com.minilang.lexer.TokenType;
import com.minilang.parser.Parser;
import com.minilang.symbol.Type;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ParserTest {

    private ProgramNode parseCode(String code) {
        Lexer lexer = new Lexer(code);
        List<Token> tokens = lexer.tokenize();
        Parser parser = new Parser(tokens);
        return parser.parse();
    }

    @Test
    @DisplayName("Test parsing variable declarations and assignments")
    void testVarDeclAndAssign() {
        String code = "int a = 10;\nfloat b;\nb = 20.5;";
        ProgramNode program = parseCode(code);

        assertEquals(3, program.getStatements().size());

        // 1: int a = 10;
        assertTrue(program.getStatements().get(0) instanceof VarDeclNode);
        VarDeclNode decl1 = (VarDeclNode) program.getStatements().get(0);
        assertEquals(Type.INT, decl1.getType());
        assertEquals("a", decl1.getVarName());
        assertTrue(decl1.hasInitializer());

        // 2: float b;
        assertTrue(program.getStatements().get(1) instanceof VarDeclNode);
        VarDeclNode decl2 = (VarDeclNode) program.getStatements().get(1);
        assertEquals(Type.FLOAT, decl2.getType());
        assertEquals("b", decl2.getVarName());
        assertFalse(decl2.hasInitializer());

        // 3: b = 20.5;
        assertTrue(program.getStatements().get(2) instanceof AssignNode);
        AssignNode assign = (AssignNode) program.getStatements().get(2);
        assertEquals("b", assign.getVarName());
        assertTrue(assign.getExpression() instanceof LiteralNode);
    }

    @Test
    @DisplayName("Test operator precedence: c = a + b * 10;")
    void testOperatorPrecedence() {
        String code = "c = a + b * 10;";
        ProgramNode program = parseCode(code);

        AssignNode assign = (AssignNode) program.getStatements().get(0);
        assertTrue(assign.getExpression() instanceof BinaryExprNode);

        // Root binary op must be '+' because '*' has higher precedence and binds tighter to b and 10
        BinaryExprNode plusNode = (BinaryExprNode) assign.getExpression();
        assertEquals(TokenType.PLUS, plusNode.getOperator());
        assertTrue(plusNode.getLeft() instanceof VariableNode);
        assertEquals("a", ((VariableNode) plusNode.getLeft()).getName());

        assertTrue(plusNode.getRight() instanceof BinaryExprNode);
        BinaryExprNode starNode = (BinaryExprNode) plusNode.getRight();
        assertEquals(TokenType.STAR, starNode.getOperator());
        assertEquals("b", ((VariableNode) starNode.getLeft()).getName());
        assertEquals(10, ((LiteralNode) starNode.getRight()).getValue());
    }

    @Test
    @DisplayName("Test parentheses override precedence: c = (a + b) * 10;")
    void testParenthesesPrecedence() {
        String code = "c = (a + b) * 10;";
        ProgramNode program = parseCode(code);

        AssignNode assign = (AssignNode) program.getStatements().get(0);
        assertTrue(assign.getExpression() instanceof BinaryExprNode);

        // Root binary op must be '*'
        BinaryExprNode starNode = (BinaryExprNode) assign.getExpression();
        assertEquals(TokenType.STAR, starNode.getOperator());
        assertTrue(starNode.getLeft() instanceof BinaryExprNode);
        assertEquals(TokenType.PLUS, ((BinaryExprNode) starNode.getLeft()).getOperator());
    }

    @Test
    @DisplayName("Test If-Else statement parsing")
    void testIfElseStatement() {
        String code = """
                if (a > 5) {
                    print(a);
                } else {
                    print(0);
                }
                """;
        ProgramNode program = parseCode(code);
        assertEquals(1, program.getStatements().size());
        assertTrue(program.getStatements().get(0) instanceof IfNode);

        IfNode ifNode = (IfNode) program.getStatements().get(0);
        assertTrue(ifNode.hasElseBranch());
        assertTrue(ifNode.getCondition() instanceof BinaryExprNode);
        assertTrue(ifNode.getThenBranch() instanceof BlockNode);
        assertTrue(ifNode.getElseBranch() instanceof BlockNode);
    }

    @Test
    @DisplayName("Test While loop statement parsing")
    void testWhileStatement() {
        String code = """
                while (count < 10) {
                    count = count + 1;
                    print(count);
                }
                """;
        ProgramNode program = parseCode(code);
        assertEquals(1, program.getStatements().size());
        assertTrue(program.getStatements().get(0) instanceof WhileNode);

        WhileNode whileNode = (WhileNode) program.getStatements().get(0);
        assertTrue(whileNode.getCondition() instanceof BinaryExprNode);
        assertTrue(whileNode.getBody() instanceof BlockNode);
        assertEquals(2, ((BlockNode) whileNode.getBody()).getStatements().size());
    }

    @Test
    @DisplayName("Test AST Pretty Printer")
    void testASTPrinter() {
        String code = "int a = 10 + 20;";
        ProgramNode program = parseCode(code);
        ASTPrinter printer = new ASTPrinter();
        String tree = printer.print(program);

        assertNotNull(tree);
        assertTrue(tree.contains("Program"));
        assertTrue(tree.contains("VarDecl (int a)"));
        assertTrue(tree.contains("BinaryOp (PLUS)"));
    }

    @Test
    @DisplayName("Test syntax error on missing expression after '='")
    void testMissingExpressionError() {
        String code = "int a = ;";
        SyntaxException ex = assertThrows(SyntaxException.class, () -> parseCode(code));
        assertTrue(ex.getMessage().contains("Expected expression"));
    }

    @Test
    @DisplayName("Test syntax error on missing semicolon")
    void testMissingSemicolonError() {
        String code = "int a = 10\nint b = 20;";
        SyntaxException ex = assertThrows(SyntaxException.class, () -> parseCode(code));
        assertTrue(ex.getMessage().contains("Expected ';'"));
    }

    @Test
    @DisplayName("Test syntax error on unclosed if parenthesis")
    void testUnclosedIfCondition() {
        String code = "if (a > 5 { print(a); }";
        SyntaxException ex = assertThrows(SyntaxException.class, () -> parseCode(code));
        assertTrue(ex.getMessage().contains("Expected ')'"));
    }
}
