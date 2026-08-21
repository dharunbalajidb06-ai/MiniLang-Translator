package com.minilang;

import com.minilang.errors.LexicalException;
import com.minilang.lexer.Lexer;
import com.minilang.lexer.Token;
import com.minilang.lexer.TokenType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class LexerTest {

    @Test
    @DisplayName("Test tokenizing integer declaration: int a = 10;")
    void testIntegerDeclaration() {
        String code = "int a = 10;";
        Lexer lexer = new Lexer(code);
        List<Token> tokens = lexer.tokenize();

        assertEquals(6, tokens.size()); // int, a, =, 10, ;, EOF -> total 6 including EOF
        assertEquals(TokenType.KW_INT, tokens.get(0).getType());
        assertEquals(TokenType.IDENTIFIER, tokens.get(1).getType());
        assertEquals("a", tokens.get(1).getLexeme());
        assertEquals(TokenType.ASSIGN, tokens.get(2).getType());
        assertEquals(TokenType.INT_LITERAL, tokens.get(3).getType());
        assertEquals(10, tokens.get(3).getLiteralValue());
        assertEquals(TokenType.SEMICOLON, tokens.get(4).getType());
        assertEquals(TokenType.EOF, tokens.get(5).getType());
    }

    @Test
    @DisplayName("Test tokenizing float, string, and boolean literals")
    void testLiterals() {
        String code = "float price = 25.5;\nstring name = \"Dharun\";\nboolean flag = true;";
        Lexer lexer = new Lexer(code);
        List<Token> tokens = lexer.tokenize();

        // Line 1: float, price, =, 25.5, ;
        assertEquals(TokenType.KW_FLOAT, tokens.get(0).getType());
        assertEquals(TokenType.FLOAT_LITERAL, tokens.get(3).getType());
        assertEquals(25.5, (Double) tokens.get(3).getLiteralValue(), 0.0001);

        // Line 2: string, name, =, "Dharun", ;
        assertEquals(TokenType.KW_STRING, tokens.get(5).getType());
        assertEquals(TokenType.STRING_LITERAL, tokens.get(8).getType());
        assertEquals("Dharun", tokens.get(8).getLiteralValue());
        assertEquals(2, tokens.get(8).getLine());

        // Line 3: boolean, flag, =, true, ;
        assertEquals(TokenType.KW_BOOLEAN, tokens.get(10).getType());
        assertEquals(TokenType.BOOLEAN_LITERAL, tokens.get(13).getType());
        assertEquals(true, tokens.get(13).getLiteralValue());
        assertEquals(3, tokens.get(13).getLine());
    }

    @Test
    @DisplayName("Test all operators and separators")
    void testOperators() {
        String code = "+ - * / % > < >= <= == != && || ! ( ) { } ,";
        Lexer lexer = new Lexer(code);
        List<Token> tokens = lexer.tokenize();

        TokenType[] expected = {
                TokenType.PLUS, TokenType.MINUS, TokenType.STAR, TokenType.SLASH, TokenType.PERCENT,
                TokenType.GT, TokenType.LT, TokenType.GTE, TokenType.LTE, TokenType.EQ, TokenType.NEQ,
                TokenType.AND, TokenType.OR, TokenType.NOT,
                TokenType.LPAREN, TokenType.RPAREN, TokenType.LBRACE, TokenType.RBRACE, TokenType.COMMA,
                TokenType.EOF
        };

        assertEquals(expected.length, tokens.size());
        for (int i = 0; i < expected.length; i++) {
            assertEquals(expected[i], tokens.get(i).getType());
        }
    }

    @Test
    @DisplayName("Test single line and block comments are ignored")
    void testComments() {
        String code = "// This is a comment\nint x = 5; /* block comment */ int y = 10;";
        Lexer lexer = new Lexer(code);
        List<Token> tokens = lexer.tokenize();

        assertEquals(TokenType.KW_INT, tokens.get(0).getType());
        assertEquals("x", tokens.get(1).getLexeme());
        assertEquals(TokenType.KW_INT, tokens.get(5).getType());
        assertEquals("y", tokens.get(6).getLexeme());
    }

    @Test
    @DisplayName("Test lexical error on invalid character @")
    void testInvalidCharacter() {
        String code = "int @a = 10;";
        Lexer lexer = new Lexer(code);
        LexicalException ex = assertThrows(LexicalException.class, lexer::tokenize);
        assertTrue(ex.getMessage().contains("Invalid character '@'"));
        assertEquals(1, ex.getLine());
        assertEquals(5, ex.getColumn());
    }

    @Test
    @DisplayName("Test lexical error on unterminated string")
    void testUnterminatedString() {
        String code = "string s = \"unclosed string;";
        Lexer lexer = new Lexer(code);
        LexicalException ex = assertThrows(LexicalException.class, lexer::tokenize);
        assertTrue(ex.getMessage().contains("Unterminated string literal"));
    }

    @Test
    @DisplayName("Test lexical error on single & character")
    void testLoneAmpersand() {
        String code = "boolean b = true & false;";
        Lexer lexer = new Lexer(code);
        LexicalException ex = assertThrows(LexicalException.class, lexer::tokenize);
        assertTrue(ex.getMessage().contains("Did you mean '&&'?"));
    }
}
