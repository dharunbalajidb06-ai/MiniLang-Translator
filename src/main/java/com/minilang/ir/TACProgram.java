package com.minilang.ir;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents a sequence of Three-Address Code instructions making up an IR program.
 */
public class TACProgram {
    private final List<TACInstruction> instructions;

    public TACProgram(List<TACInstruction> instructions) {
        this.instructions = instructions != null ? new ArrayList<>(instructions) : new ArrayList<>();
    }

    public TACProgram() {
        this(new ArrayList<>());
    }

    public void add(TACInstruction instruction) {
        instructions.add(instruction);
    }

    public List<TACInstruction> getInstructions() {
        return Collections.unmodifiableList(instructions);
    }

    public int size() {
        return instructions.size();
    }

    public String toFormattedString() {
        StringBuilder sb = new StringBuilder();
        for (TACInstruction inst : instructions) {
            if (inst.getOp() == TACOp.LABEL) {
                sb.append(inst).append("\n");
            } else {
                sb.append("    ").append(inst).append("\n");
            }
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        return toFormattedString();
    }
}
