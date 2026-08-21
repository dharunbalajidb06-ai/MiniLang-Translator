package com.minilang.semantic;

import com.minilang.ast.*;
import com.minilang.errors.SemanticException;
import com.minilang.lexer.TokenType;
import com.minilang.symbol.Symbol;
import com.minilang.symbol.SymbolTable;
import com.minilang.symbol.Type;

/**
 * Semantic Analyzer responsible for:
 * 1. Type verification & type compatibility checking
 * 2. Scope management and variable lifetime
 * 3. Detecting undeclared variables & duplicate declarations
 * 4. Resolving expression types and setting them on AST nodes
 */
public class SemanticAnalyzer implements ASTVisitor<Type> {
    private final SymbolTable symbolTable;

    public SemanticAnalyzer(SymbolTable symbolTable) {
        this.symbolTable = symbolTable != null ? symbolTable : new SymbolTable();
    }

    public SemanticAnalyzer() {
        this(new SymbolTable());
    }

    public SymbolTable getSymbolTable() {
        return symbolTable;
    }

    public void analyze(ASTNode root) {
        if (root != null) {
            root.accept(this);
        }
    }

    @Override
    public Type visitProgram(ProgramNode node) {
        for (StatementNode stmt : node.getStatements()) {
            stmt.accept(this);
        }
        return Type.VOID;
    }

    @Override
    public Type visitVarDecl(VarDeclNode node) {
        Type declType = node.getType();
        boolean hasInit = node.hasInitializer();

        if (hasInit) {
            Type initType = node.getInitializer().accept(this);
            if (!isAssignable(declType, initType)) {
                throw new SemanticException(
                        "Cannot assign " + initType + " to " + declType + " variable '" + node.getVarName() + "'",
                        node.getLine(),
                        node.getColumn()
                );
            }
        }

        Symbol symbol = new Symbol(
                node.getVarName(),
                declType,
                symbolTable.getCurrentScopeLevel(),
                node.getLine(),
                node.getColumn(),
                hasInit
        );
        symbolTable.define(symbol);
        return Type.VOID;
    }

    @Override
    public Type visitAssign(AssignNode node) {
        Symbol symbol = symbolTable.lookup(node.getVarName()).orElseThrow(() ->
                new SemanticException("Variable '" + node.getVarName() + "' has not been declared",
                        node.getLine(), node.getColumn())
        );

        Type exprType = node.getExpression().accept(this);
        if (!isAssignable(symbol.getType(), exprType)) {
            throw new SemanticException(
                    "Cannot assign " + exprType + " to " + symbol.getType() + " variable '" + node.getVarName() + "'",
                    node.getLine(),
                    node.getColumn()
            );
        }

        symbol.setInitialized(true);
        return Type.VOID;
    }

    @Override
    public Type visitPrint(PrintNode node) {
        node.getExpression().accept(this);
        return Type.VOID;
    }

    @Override
    public Type visitInput(InputNode node) {
        Symbol symbol = symbolTable.lookup(node.getVarName()).orElseThrow(() ->
                new SemanticException("Variable '" + node.getVarName() + "' has not been declared",
                        node.getLine(), node.getColumn())
        );
        symbol.setInitialized(true);
        return Type.VOID;
    }

    @Override
    public Type visitIf(IfNode node) {
        Type condType = node.getCondition().accept(this);
        if (condType != Type.BOOLEAN) {
            throw new SemanticException(
                    "Condition in 'if' statement must be of boolean type, found: " + condType,
                    node.getCondition().getLine(),
                    node.getCondition().getColumn()
            );
        }

        node.getThenBranch().accept(this);
        if (node.hasElseBranch()) {
            node.getElseBranch().accept(this);
        }
        return Type.VOID;
    }

    @Override
    public Type visitWhile(WhileNode node) {
        Type condType = node.getCondition().accept(this);
        if (condType != Type.BOOLEAN) {
            throw new SemanticException(
                    "Condition in 'while' statement must be of boolean type, found: " + condType,
                    node.getCondition().getLine(),
                    node.getCondition().getColumn()
            );
        }

        node.getBody().accept(this);
        return Type.VOID;
    }

    @Override
    public Type visitBlock(BlockNode node) {
        symbolTable.enterScope();
        for (StatementNode stmt : node.getStatements()) {
            stmt.accept(this);
        }
        symbolTable.exitScope();
        return Type.VOID;
    }

    @Override
    public Type visitBinaryExpr(BinaryExprNode node) {
        Type leftType = node.getLeft().accept(this);
        Type rightType = node.getRight().accept(this);
        TokenType op = node.getOperator();
        Type resultType;

        switch (op) {
            case PLUS:
                if (leftType == Type.STRING || rightType == Type.STRING) {
                    resultType = Type.STRING; // String concatenation
                } else if (leftType.isNumeric() && rightType.isNumeric()) {
                    resultType = (leftType == Type.FLOAT || rightType == Type.FLOAT) ? Type.FLOAT : Type.INT;
                } else {
                    throw new SemanticException("Operator '+' cannot be applied to " + leftType + " and " + rightType,
                            node.getLine(), node.getColumn());
                }
                break;

            case MINUS:
            case STAR:
            case SLASH:
                if (leftType.isNumeric() && rightType.isNumeric()) {
                    resultType = (leftType == Type.FLOAT || rightType == Type.FLOAT) ? Type.FLOAT : Type.INT;
                } else {
                    throw new SemanticException("Operator '" + op + "' cannot be applied to " + leftType + " and " + rightType,
                            node.getLine(), node.getColumn());
                }
                break;

            case PERCENT:
                if (leftType == Type.INT && rightType == Type.INT) {
                    resultType = Type.INT;
                } else {
                    throw new SemanticException("Operator '%' requires integer operands, found " + leftType + " and " + rightType,
                            node.getLine(), node.getColumn());
                }
                break;

            case GT:
            case GTE:
            case LT:
            case LTE:
                if (leftType.isNumeric() && rightType.isNumeric()) {
                    resultType = Type.BOOLEAN;
                } else {
                    throw new SemanticException("Comparison operator '" + op + "' requires numeric operands, found " + leftType + " and " + rightType,
                            node.getLine(), node.getColumn());
                }
                break;

            case EQ:
            case NEQ:
                if (isComparable(leftType, rightType)) {
                    resultType = Type.BOOLEAN;
                } else {
                    throw new SemanticException("Cannot compare incompatible types " + leftType + " and " + rightType,
                            node.getLine(), node.getColumn());
                }
                break;

            case AND:
            case OR:
                if (leftType == Type.BOOLEAN && rightType == Type.BOOLEAN) {
                    resultType = Type.BOOLEAN;
                } else {
                    throw new SemanticException("Logical operator '" + op + "' requires boolean operands, found " + leftType + " and " + rightType,
                            node.getLine(), node.getColumn());
                }
                break;

            default:
                throw new SemanticException("Unknown binary operator: " + op, node.getLine(), node.getColumn());
        }

        node.setEvaluatedType(resultType);
        return resultType;
    }

    @Override
    public Type visitUnaryExpr(UnaryExprNode node) {
        Type operandType = node.getOperand().accept(this);
        TokenType op = node.getOperator();
        Type resultType;

        if (op == TokenType.MINUS) {
            if (operandType.isNumeric()) {
                resultType = operandType;
            } else {
                throw new SemanticException("Unary '-' requires numeric operand, found " + operandType,
                        node.getLine(), node.getColumn());
            }
        } else if (op == TokenType.NOT) {
            if (operandType == Type.BOOLEAN) {
                resultType = Type.BOOLEAN;
            } else {
                throw new SemanticException("Logical '!' requires boolean operand, found " + operandType,
                        node.getLine(), node.getColumn());
            }
        } else {
            throw new SemanticException("Unknown unary operator: " + op, node.getLine(), node.getColumn());
        }

        node.setEvaluatedType(resultType);
        return resultType;
    }

    @Override
    public Type visitLiteral(LiteralNode node) {
        return node.getType();
    }

    @Override
    public Type visitVariable(VariableNode node) {
        Symbol symbol = symbolTable.lookup(node.getName()).orElseThrow(() ->
                new SemanticException("Variable '" + node.getName() + "' has not been declared",
                        node.getLine(), node.getColumn())
        );

        node.setEvaluatedType(symbol.getType());
        return symbol.getType();
    }

    private boolean isAssignable(Type target, Type source) {
        if (target == source) return true;
        // Implicit promotion: int can be assigned to float
        return target == Type.FLOAT && source == Type.INT;
    }

    private boolean isComparable(Type t1, Type t2) {
        if (t1 == t2) return true;
        return t1.isNumeric() && t2.isNumeric();
    }
}
