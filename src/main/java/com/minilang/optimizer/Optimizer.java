package com.minilang.optimizer;

import com.minilang.ir.TACInstruction;
import com.minilang.ir.TACOp;
import com.minilang.ir.TACProgram;

import java.util.*;

/**
 * Code Optimizer performing safe compile-time optimizations:
 * 1. Constant Folding (e.g. 10 + 20 -> 30)
 * 2. Algebraic Simplification (e.g. x + 0 -> x, x * 0 -> 0)
 * 3. Constant Propagation (substituting known literal values)
 * 4. Dead Code & Unused Temporary Elimination
 */
public class Optimizer {

    public TACProgram optimize(TACProgram input) {
        TACProgram current = input;
        boolean changed;

        // Iterate optimization passes until fixed point is reached
        int maxPasses = 10;
        int pass = 0;
        do {
            changed = false;
            TACProgram next = optimizePass(current);
            if (!next.getInstructions().equals(current.getInstructions())) {
                changed = true;
                current = next;
            }
            pass++;
        } while (changed && pass < maxPasses);

        // Final pass: Eliminate unused temporaries
        return eliminateDeadTemporaries(current);
    }

    private TACProgram optimizePass(TACProgram input) {
        List<TACInstruction> optimized = new ArrayList<>();
        Map<String, String> constantMap = new HashMap<>();

        for (TACInstruction inst : input.getInstructions()) {
            TACOp op = inst.getOp();
            String arg1 = inst.getArg1();
            String arg2 = inst.getArg2();
            String result = inst.getResult();

            // Clear constant tracking across labels or jumps for safety
            if (op == TACOp.LABEL || op == TACOp.GOTO || op == TACOp.IF_FALSE_GOTO || op == TACOp.IF_GOTO) {
                constantMap.clear();
                optimized.add(inst);
                continue;
            }

            // Constant Propagation: substitute known constant arguments
            if (arg1 != null && constantMap.containsKey(arg1)) {
                arg1 = constantMap.get(arg1);
            }
            if (arg2 != null && constantMap.containsKey(arg2)) {
                arg2 = constantMap.get(arg2);
            }

            // Constant Folding for Binary Operations
            if (isBinaryArithOrLogic(op) && isLiteral(arg1) && isLiteral(arg2)) {
                String foldedVal = evaluateConstantBinary(op, arg1, arg2);
                if (foldedVal != null) {
                    optimized.add(new TACInstruction(TACOp.ASSIGN, foldedVal, null, result, inst.getType()));
                    constantMap.put(result, foldedVal);
                    continue;
                }
            }

            // Constant Folding for Unary Operations
            if ((op == TACOp.NEG || op == TACOp.NOT) && isLiteral(arg1)) {
                String foldedVal = evaluateConstantUnary(op, arg1);
                if (foldedVal != null) {
                    optimized.add(new TACInstruction(TACOp.ASSIGN, foldedVal, null, result, inst.getType()));
                    constantMap.put(result, foldedVal);
                    continue;
                }
            }

            // Algebraic Simplification
            TACInstruction simplified = applyAlgebraicSimplification(op, arg1, arg2, result, inst);
            if (simplified != null) {
                optimized.add(simplified);
                if (simplified.getOp() == TACOp.ASSIGN && isLiteral(simplified.getArg1())) {
                    constantMap.put(simplified.getResult(), simplified.getArg1());
                }
                continue;
            }

            // Track standard assignments of literals: x = 10
            if (op == TACOp.ASSIGN && isLiteral(arg1)) {
                constantMap.put(result, arg1);
            } else if (result != null) {
                constantMap.remove(result);
            }

            optimized.add(new TACInstruction(op, arg1, arg2, result, inst.getType()));
        }

        return new TACProgram(optimized);
    }

    private TACInstruction applyAlgebraicSimplification(TACOp op, String arg1, String arg2, String result, TACInstruction original) {
        if (op == TACOp.ADD) {
            // x + 0 -> x
            if ("0".equals(arg2) || "0.0".equals(arg2)) return new TACInstruction(TACOp.ASSIGN, arg1, null, result, original.getType());
            // 0 + x -> x
            if ("0".equals(arg1) || "0.0".equals(arg1)) return new TACInstruction(TACOp.ASSIGN, arg2, null, result, original.getType());
        } else if (op == TACOp.SUB) {
            // x - 0 -> x
            if ("0".equals(arg2) || "0.0".equals(arg2)) return new TACInstruction(TACOp.ASSIGN, arg1, null, result, original.getType());
        } else if (op == TACOp.MUL) {
            // x * 1 -> x
            if ("1".equals(arg2) || "1.0".equals(arg2)) return new TACInstruction(TACOp.ASSIGN, arg1, null, result, original.getType());
            // 1 * x -> x
            if ("1".equals(arg1) || "1.0".equals(arg1)) return new TACInstruction(TACOp.ASSIGN, arg2, null, result, original.getType());
            // x * 0 -> 0
            if ("0".equals(arg2) || "0.0".equals(arg2)) return new TACInstruction(TACOp.ASSIGN, "0", null, result, original.getType());
            // 0 * x -> 0
            if ("0".equals(arg1) || "0.0".equals(arg1)) return new TACInstruction(TACOp.ASSIGN, "0", null, result, original.getType());
        } else if (op == TACOp.DIV) {
            // x / 1 -> x
            if ("1".equals(arg2) || "1.0".equals(arg2)) return new TACInstruction(TACOp.ASSIGN, arg1, null, result, original.getType());
        } else if (op == TACOp.AND) {
            // x && true -> x
            if ("true".equals(arg2)) return new TACInstruction(TACOp.ASSIGN, arg1, null, result, original.getType());
            // true && x -> x
            if ("true".equals(arg1)) return new TACInstruction(TACOp.ASSIGN, arg2, null, result, original.getType());
            // x && false -> false
            if ("false".equals(arg2) || "false".equals(arg1)) return new TACInstruction(TACOp.ASSIGN, "false", null, result, original.getType());
        } else if (op == TACOp.OR) {
            // x || false -> x
            if ("false".equals(arg2)) return new TACInstruction(TACOp.ASSIGN, arg1, null, result, original.getType());
            // false || x -> x
            if ("false".equals(arg1)) return new TACInstruction(TACOp.ASSIGN, arg2, null, result, original.getType());
            // x || true -> true
            if ("true".equals(arg2) || "true".equals(arg1)) return new TACInstruction(TACOp.ASSIGN, "true", null, result, original.getType());
        }
        return null;
    }

    private String evaluateConstantBinary(TACOp op, String arg1, String arg2) {
        try {
            boolean isFloat = arg1.contains(".") || arg2.contains(".");
            if (isFloat) {
                double d1 = Double.parseDouble(arg1);
                double d2 = Double.parseDouble(arg2);
                return switch (op) {
                    case ADD -> String.valueOf(d1 + d2);
                    case SUB -> String.valueOf(d1 - d2);
                    case MUL -> String.valueOf(d1 * d2);
                    case DIV -> d2 != 0 ? String.valueOf(d1 / d2) : null;
                    case GT -> String.valueOf(d1 > d2);
                    case GTE -> String.valueOf(d1 >= d2);
                    case LT -> String.valueOf(d1 < d2);
                    case LTE -> String.valueOf(d1 <= d2);
                    case EQ -> String.valueOf(d1 == d2);
                    case NEQ -> String.valueOf(d1 != d2);
                    default -> null;
                };
            } else if (isInteger(arg1) && isInteger(arg2)) {
                int i1 = Integer.parseInt(arg1);
                int i2 = Integer.parseInt(arg2);
                return switch (op) {
                    case ADD -> String.valueOf(i1 + i2);
                    case SUB -> String.valueOf(i1 - i2);
                    case MUL -> String.valueOf(i1 * i2);
                    case DIV -> i2 != 0 ? String.valueOf(i1 / i2) : null;
                    case MOD -> i2 != 0 ? String.valueOf(i1 % i2) : null;
                    case GT -> String.valueOf(i1 > i2);
                    case GTE -> String.valueOf(i1 >= i2);
                    case LT -> String.valueOf(i1 < i2);
                    case LTE -> String.valueOf(i1 <= i2);
                    case EQ -> String.valueOf(i1 == i2);
                    case NEQ -> String.valueOf(i1 != i2);
                    default -> null;
                };
            } else if (isBoolean(arg1) && isBoolean(arg2)) {
                boolean b1 = Boolean.parseBoolean(arg1);
                boolean b2 = Boolean.parseBoolean(arg2);
                return switch (op) {
                    case AND -> String.valueOf(b1 && b2);
                    case OR -> String.valueOf(b1 || b2);
                    case EQ -> String.valueOf(b1 == b2);
                    case NEQ -> String.valueOf(b1 != b2);
                    default -> null;
                };
            }
        } catch (Exception ignored) {}
        return null;
    }

    private String evaluateConstantUnary(TACOp op, String arg1) {
        try {
            if (op == TACOp.NEG) {
                if (arg1.contains(".")) {
                    return String.valueOf(-Double.parseDouble(arg1));
                } else if (isInteger(arg1)) {
                    return String.valueOf(-Integer.parseInt(arg1));
                }
            } else if (op == TACOp.NOT && isBoolean(arg1)) {
                return String.valueOf(!Boolean.parseBoolean(arg1));
            }
        } catch (Exception ignored) {}
        return null;
    }

    private TACProgram eliminateDeadTemporaries(TACProgram input) {
        // Collect all used variables / temporaries in arg1 and arg2
        Set<String> usedVars = new HashSet<>();
        for (TACInstruction inst : input.getInstructions()) {
            if (inst.getArg1() != null) usedVars.add(inst.getArg1());
            if (inst.getArg2() != null) usedVars.add(inst.getArg2());
        }

        List<TACInstruction> filtered = new ArrayList<>();
        for (TACInstruction inst : input.getInstructions()) {
            String res = inst.getResult();
            // If result is a temporary (starts with "t") and never used in subsequent instructions
            if (res != null && res.startsWith("t") && !usedVars.contains(res) &&
                    inst.getOp() != TACOp.PRINT && inst.getOp() != TACOp.INPUT) {
                continue; // Skip dead temporary
            }
            filtered.add(inst);
        }
        return new TACProgram(filtered);
    }

    private boolean isLiteral(String s) {
        if (s == null) return false;
        return isInteger(s) || isFloat(s) || isBoolean(s) || isString(s);
    }

    private boolean isInteger(String s) {
        if (s == null || s.isEmpty()) return false;
        try {
            Integer.parseInt(s);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private boolean isFloat(String s) {
        if (s == null || !s.contains(".")) return false;
        try {
            Double.parseDouble(s);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private boolean isBoolean(String s) {
        return "true".equals(s) || "false".equals(s);
    }

    private boolean isString(String s) {
        return s != null && s.startsWith("\"") && s.endsWith("\"");
    }

    private boolean isBinaryArithOrLogic(TACOp op) {
        return switch (op) {
            case ADD, SUB, MUL, DIV, MOD, GT, GTE, LT, LTE, EQ, NEQ, AND, OR -> true;
            default -> false;
        };
    }
}
