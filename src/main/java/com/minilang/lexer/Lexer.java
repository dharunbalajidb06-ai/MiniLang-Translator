package com.minilang.lexer;

import com.minilang.errors.LexicalException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The Lexical Analyzer (Lexer) scans MiniLang source code character by character
 * and converts it into a structured stream of Token objects.
 */
public class Lexer {
    private final String source;
    private final List<Token> tokens = new ArrayList<>();
    
    private int start = 0;
    private int current = 0;
    private int line = 1;
    private int column = 1;
    private int startColumn = 1;

    private static final Map<String, TokenType> KEYWORDS = new HashMap<>();

    static {
        KEYWORDS.put("int", TokenType.KW_INT);
        KEYWORDS.put("float", TokenType.KW_FLOAT);
        KEYWORDS.put("string", TokenType.KW_STRING);
        KEYWORDS.put("boolean", TokenType.KW_BOOLEAN);
        KEYWORDS.put("if", TokenType.KW_IF);
        KEYWORDS.put("else", TokenType.KW_ELSE);
        KEYWORDS.put("while", TokenType.KW_WHILE);
        KEYWORDS.put("print", TokenType.KW_PRINT);
        KEYWORDS.put("input", TokenType.KW_INPUT);
        KEYWORDS.put("true", TokenType.BOOLEAN_LITERAL);
        KEYWORDS.put("false", TokenType.BOOLEAN_LITERAL);
    }

    public Lexer(String source) {
        this.source = source != null ? source : "";
    }

    /**
     * Scans the entire source code and returns the full list of tokens.
     */
    public List<Token> tokenize() {
        tokens.clear();
        start = 0;
        current = 0;
        line = 1;
        column = 1;

        while (!isAtEnd()) {
            start = current;
            startColumn = column;
            scanToken();
        }

        tokens.add(new Token(TokenType.EOF, "", null, line, column));
        return tokens;
    }

    private void scanToken() {
        char c = advance();
        switch (c) {
            case '(': addToken(TokenType.LPAREN); break;
            case ')': addToken(TokenType.RPAREN); break;
            case '{': addToken(TokenType.LBRACE); break;
            case '}': addToken(TokenType.RBRACE); break;
            case ';': addToken(TokenType.SEMICOLON); break;
            case ',': addToken(TokenType.COMMA); break;
            case '+': addToken(TokenType.PLUS); break;
            case '-': addToken(TokenType.MINUS); break;
            case '*': addToken(TokenType.STAR); break;
            case '%': addToken(TokenType.PERCENT); break;

            case '!':
                addToken(match('=') ? TokenType.NEQ : TokenType.NOT);
                break;
            case '=':
                addToken(match('=') ? TokenType.EQ : TokenType.ASSIGN);
                break;
            case '<':
                addToken(match('=') ? TokenType.LTE : TokenType.LT);
                break;
            case '>':
                addToken(match('=') ? TokenType.GTE : TokenType.GT);
                break;

            case '&':
                if (match('&')) {
                    addToken(TokenType.AND);
                } else {
                    throw new LexicalException("Unexpected character '&'. Did you mean '&&'?", line, startColumn);
                }
                break;

            case '|':
                if (match('|')) {
                    addToken(TokenType.OR);
                } else {
                    throw new LexicalException("Unexpected character '|'. Did you mean '||'?", line, startColumn);
                }
                break;

            case '/':
                if (match('/')) {
                    // Single-line comment: ignore until end of line
                    while (peek() != '\n' && !isAtEnd()) {
                        advance();
                    }
                } else if (match('*')) {
                    // Multi-line block comment
                    scanBlockComment();
                } else {
                    addToken(TokenType.SLASH);
                }
                break;

            // Whitespace handling
            case ' ':
            case '\r':
            case '\t':
                // Ignore whitespace
                break;

            case '\n':
                line++;
                column = 1;
                break;

            case '"':
                scanString();
                break;

            default:
                if (isDigit(c)) {
                    scanNumber();
                } else if (isAlpha(c)) {
                    scanIdentifierOrKeyword();
                } else {
                    throw new LexicalException("Invalid character '" + c + "'", line, startColumn);
                }
                break;
        }
    }

    private void scanBlockComment() {
        int commentStartLine = line;
        int commentStartCol = startColumn;
        while (!isAtEnd()) {
            if (peek() == '*' && peekNext() == '/') {
                advance(); // consume '*'
                advance(); // consume '/'
                return;
            }
            if (peek() == '\n') {
                line++;
                column = 0; // advance will make it 1
            }
            advance();
        }
        throw new LexicalException("Unterminated block comment starting at line " + commentStartLine, commentStartLine, commentStartCol);
    }

    private void scanString() {
        StringBuilder sb = new StringBuilder();
        int strStartLine = line;
        int strStartCol = startColumn;

        while (peek() != '"' && !isAtEnd()) {
            if (peek() == '\n') {
                line++;
                column = 1;
            }
            if (peek() == '\\') {
                advance(); // consume escape '\'
                if (isAtEnd()) {
                    throw new LexicalException("Unterminated string literal escape sequence", line, column);
                }
                char escaped = advance();
                switch (escaped) {
                    case 'n': sb.append('\n'); break;
                    case 't': sb.append('\t'); break;
                    case 'r': sb.append('\r'); break;
                    case '"': sb.append('"'); break;
                    case '\\': sb.append('\\'); break;
                    default: sb.append('\\').append(escaped); break;
                }
            } else {
                sb.append(advance());
            }
        }

        if (isAtEnd()) {
            throw new LexicalException("Unterminated string literal", strStartLine, strStartCol);
        }

        advance(); // consume closing '"'
        String value = sb.toString();
        tokens.add(new Token(TokenType.STRING_LITERAL, source.substring(start, current), value, strStartLine, strStartCol));
    }

    private void scanNumber() {
        while (isDigit(peek())) {
            advance();
        }

        // Look for floating point part
        if (peek() == '.' && isDigit(peekNext())) {
            advance(); // consume '.'
            while (isDigit(peek())) {
                advance();
            }
            String numStr = source.substring(start, current);
            double val = Double.parseDouble(numStr);
            tokens.add(new Token(TokenType.FLOAT_LITERAL, numStr, val, line, startColumn));
            return;
        }

        String numStr = source.substring(start, current);
        try {
            int val = Integer.parseInt(numStr);
            tokens.add(new Token(TokenType.INT_LITERAL, numStr, val, line, startColumn));
        } catch (NumberFormatException e) {
            long val = Long.parseLong(numStr);
            tokens.add(new Token(TokenType.INT_LITERAL, numStr, val, line, startColumn));
        }
    }

    private void scanIdentifierOrKeyword() {
        while (isAlphaNumeric(peek())) {
            advance();
        }

        String text = source.substring(start, current);
        TokenType type = KEYWORDS.get(text);

        if (type == null) {
            tokens.add(new Token(TokenType.IDENTIFIER, text, null, line, startColumn));
        } else if (type == TokenType.BOOLEAN_LITERAL) {
            boolean val = Boolean.parseBoolean(text);
            tokens.add(new Token(type, text, val, line, startColumn));
        } else {
            tokens.add(new Token(type, text, null, line, startColumn));
        }
    }

    private boolean match(char expected) {
        if (isAtEnd()) return false;
        if (source.charAt(current) != expected) return false;
        current++;
        column++;
        return true;
    }

    private char advance() {
        char c = source.charAt(current++);
        column++;
        return c;
    }

    private char peek() {
        if (isAtEnd()) return '\0';
        return source.charAt(current);
    }

    private char peekNext() {
        if (current + 1 >= source.length()) return '\0';
        return source.charAt(current + 1);
    }

    private boolean isAtEnd() {
        return current >= source.length();
    }

    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    private boolean isAlpha(char c) {
        return (c >= 'a' && c <= 'z') ||
               (c >= 'A' && c <= 'Z') ||
               c == '_';
    }

    private boolean isAlphaNumeric(char c) {
        return isAlpha(c) || isDigit(c);
    }

    private void addToken(TokenType type) {
        String text = source.substring(start, current);
        tokens.add(new Token(type, text, null, line, startColumn));
    }
}
