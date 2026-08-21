package com.minilang.ast;

/**
 * Visitor interface for traversing and transforming the MiniLang AST.
 */
public interface ASTVisitor<R> {
    R visitProgram(ProgramNode node);
    R visitVarDecl(VarDeclNode node);
    R visitAssign(AssignNode node);
    R visitPrint(PrintNode node);
    R visitInput(InputNode node);
    R visitIf(IfNode node);
    R visitWhile(WhileNode node);
    R visitBlock(BlockNode node);

    R visitBinaryExpr(BinaryExprNode node);
    R visitUnaryExpr(UnaryExprNode node);
    R visitLiteral(LiteralNode node);
    R visitVariable(VariableNode node);
}
