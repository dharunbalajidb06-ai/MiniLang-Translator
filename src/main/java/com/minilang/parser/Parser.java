package com.minilang.parser;

import com.minilang.ast.*;
import com.minilang.errors.SyntaxException;
import com.minilang.lexer.Token;
import com.minilang.lexer.TokenType;
import com.minilang.symbol.Type;

import java.util.ArrayList;
import java.util.List;

/**
 * Recursive-descent parser for MiniLang that constructs an Abstract Syntax Tree (AST).
 * Handles operator precedence, associativity, and detailed syntax error diagnostics.
 */
public class Parser {
    private final List<Token> tokens;
    private int current = 0;

    public Parser(List<Token> tokens) {
        this.tokens = tokens != null ? tokens : new ArrayList<>();
    }

    /**
     * Parses the full token stream into a ProgramNode AST.
     */
    public ProgramNode parse() {
        List<StatementNode> statements = new ArrayList<>();
        int startLine = tokens.isEmpty() ? 1 : tokens.get(0).getLine();
        int startCol = tokens.isEmpty() ? 1 : tokens.get(0).getColumn();

        while (!isAtEnd()) {
            statements.add(statement());
        }

        return new ProgramNode(statements, startLine, startCol);
    }

    private StatementNode statement() {
        Token token = peek();

        if (match(TokenType.KW_INT, TokenType.KW_FLOAT, TokenType.KW_STRING, TokenType.KW_BOOLEAN)) {
            return varDeclaration(previous());
        }
        if (check(TokenType.IDENTIFIER)) {
            return assignment();
        }
        if (match(TokenType.KW_PRINT)) {
            return printStatement(previous());
        }
        if (match(TokenType.KW_INPUT)) {
            return inputStatement(previous());
        }
        if (match(TokenType.KW_IF)) {
            return ifStatement(previous());
        }
        if (match(TokenType.KW_WHILE)) {
            return whileStatement(previous());
        }
        if (match(TokenType.LBRACE)) {
            return blockStatement(previous());
        }

        throw new SyntaxException("Unexpected token '" + token.getLexeme() + "'", token.getLine(), token.getColumn());
    }

    private VarDeclNode varDeclaration(Token typeToken) {
        Type type = Type.fromString(typeToken.getLexeme());
        Token nameToken = consume(TokenType.IDENTIFIER, "Expected variable name after type '" + typeToken.getLexeme() + "'");
        ExpressionNode initializer = null;

        if (match(TokenType.ASSIGN)) {
            initializer = expression();
        }

        consume(TokenType.SEMICOLON, "Expected ';' after variable declaration");
        return new VarDeclNode(type, nameToken.getLexeme(), initializer, typeToken.getLine(), typeToken.getColumn());
    }

    private AssignNode assignment() {
        Token nameToken = advance(); // Consume IDENTIFIER
        consume(TokenType.ASSIGN, "Expected '=' after variable '" + nameToken.getLexeme() + "'");
        ExpressionNode value = expression();
        consume(TokenType.SEMICOLON, "Expected ';' after assignment");
        return new AssignNode(nameToken.getLexeme(), value, nameToken.getLine(), nameToken.getColumn());
    }

    private PrintNode printStatement(Token printToken) {
        consume(TokenType.LPAREN, "Expected '(' after 'print'");
        ExpressionNode expr = expression();
        consume(TokenType.RPAREN, "Expected ')' after print expression");
        consume(TokenType.SEMICOLON, "Expected ';' after print statement");
        return new PrintNode(expr, printToken.getLine(), printToken.getColumn());
    }

    private InputNode inputStatement(Token inputToken) {
        consume(TokenType.LPAREN, "Expected '(' after 'input'");
        Token varToken = consume(TokenType.IDENTIFIER, "Expected variable name inside 'input()'");
        consume(TokenType.RPAREN, "Expected ')' after variable in 'input()'");
        consume(TokenType.SEMICOLON, "Expected ';' after input statement");
        return new InputNode(varToken.getLexeme(), inputToken.getLine(), inputToken.getColumn());
    }

    private IfNode ifStatement(Token ifToken) {
        consume(TokenType.LPAREN, "Expected '(' after 'if'");
        ExpressionNode condition = expression();
        consume(TokenType.RPAREN, "Expected ')' after if condition");

        StatementNode thenBranch = statement();
        StatementNode elseBranch = null;

        if (match(TokenType.KW_ELSE)) {
            elseBranch = statement();
        }

        return new IfNode(condition, thenBranch, elseBranch, ifToken.getLine(), ifToken.getColumn());
    }

    private WhileNode whileStatement(Token whileToken) {
        consume(TokenType.LPAREN, "Expected '(' after 'while'");
        ExpressionNode condition = expression();
        consume(TokenType.RPAREN, "Expected ')' after while condition");

        StatementNode body = statement();
        return new WhileNode(condition, body, whileToken.getLine(), whileToken.getColumn());
    }

    private BlockNode blockStatement(Token lbraceToken) {
        List<StatementNode> statements = new ArrayList<>();
        while (!check(TokenType.RBRACE) && !isAtEnd()) {
            statements.add(statement());
        }
        consume(TokenType.RBRACE, "Expected '}' after block");
        return new BlockNode(statements, lbraceToken.getLine(), lbraceToken.getColumn());
    }

    // ==========================================
    // Expression Parsing with Precedence Climbing
    // ==========================================

    public ExpressionNode expression() {
        return logicalOr();
    }

    private ExpressionNode logicalOr() {
        ExpressionNode expr = logicalAnd();

        while (match(TokenType.OR)) {
            Token op = previous();
            ExpressionNode right = logicalAnd();
            expr = new BinaryExprNode(expr, op.getType(), right, op.getLine(), op.getColumn());
        }

        return expr;
    }

    private ExpressionNode logicalAnd() {
        ExpressionNode expr = equality();

        while (match(TokenType.AND)) {
            Token op = previous();
            ExpressionNode right = equality();
            expr = new BinaryExprNode(expr, op.getType(), right, op.getLine(), op.getColumn());
        }

        return expr;
    }

    private ExpressionNode equality() {
        ExpressionNode expr = comparison();

        while (match(TokenType.EQ, TokenType.NEQ)) {
            Token op = previous();
            ExpressionNode right = comparison();
            expr = new BinaryExprNode(expr, op.getType(), right, op.getLine(), op.getColumn());
        }

        return expr;
    }

    private ExpressionNode comparison() {
        ExpressionNode expr = term();

        while (match(TokenType.GT, TokenType.GTE, TokenType.LT, TokenType.LTE)) {
            Token op = previous();
            ExpressionNode right = term();
            expr = new BinaryExprNode(expr, op.getType(), right, op.getLine(), op.getColumn());
        }

        return expr;
    }

    private ExpressionNode term() {
        ExpressionNode expr = factor();

        while (match(TokenType.PLUS, TokenType.MINUS)) {
            Token op = previous();
            ExpressionNode right = factor();
            expr = new BinaryExprNode(expr, op.getType(), right, op.getLine(), op.getColumn());
        }

        return expr;
    }

    private ExpressionNode factor() {
        ExpressionNode expr = unary();

        while (match(TokenType.STAR, TokenType.SLASH, TokenType.PERCENT)) {
            Token op = previous();
            ExpressionNode right = unary();
            expr = new BinaryExprNode(expr, op.getType(), right, op.getLine(), op.getColumn());
        }

        return expr;
    }

    private ExpressionNode unary() {
        if (match(TokenType.NOT, TokenType.MINUS)) {
            Token op = previous();
            ExpressionNode right = unary();
            return new UnaryExprNode(op.getType(), right, op.getLine(), op.getColumn());
        }

        return primary();
    }

    private ExpressionNode primary() {
        Token token = peek();

        if (match(TokenType.INT_LITERAL)) {
            return new LiteralNode(previous().getLiteralValue(), Type.INT, previous().getLine(), previous().getColumn());
        }
        if (match(TokenType.FLOAT_LITERAL)) {
            return new LiteralNode(previous().getLiteralValue(), Type.FLOAT, previous().getLine(), previous().getColumn());
        }
        if (match(TokenType.STRING_LITERAL)) {
            return new LiteralNode(previous().getLiteralValue(), Type.STRING, previous().getLine(), previous().getColumn());
        }
        if (match(TokenType.BOOLEAN_LITERAL)) {
            return new LiteralNode(previous().getLiteralValue(), Type.BOOLEAN, previous().getLine(), previous().getColumn());
        }
        if (match(TokenType.IDENTIFIER)) {
            return new VariableNode(previous().getLexeme(), previous().getLine(), previous().getColumn());
        }
        if (match(TokenType.LPAREN)) {
            ExpressionNode expr = expression();
            consume(TokenType.RPAREN, "Expected ')' after expression");
            return expr;
        }

        throw new SyntaxException("Expected expression, found '" + token.getLexeme() + "'", token.getLine(), token.getColumn());
    }

    // ==========================================
    // Helper Methods
    // ==========================================

    private boolean match(TokenType... types) {
        for (TokenType type : types) {
            if (check(type)) {
                advance();
                return true;
            }
        }
        return false;
    }

    private Token consume(TokenType type, String message) {
        if (check(type)) return advance();
        throw new SyntaxException(message, peek().getLine(), peek().getColumn());
    }

    private boolean check(TokenType type) {
        if (isAtEnd()) return type == TokenType.EOF;
        return peek().getType() == type;
    }

    private Token advance() {
        if (!isAtEnd()) current++;
        return previous();
    }

    private boolean isAtEnd() {
        return peek().getType() == TokenType.EOF;
    }

    private Token peek() {
        return tokens.get(current);
    }

    private Token previous() {
        return tokens.get(current - 1);
    }
}
