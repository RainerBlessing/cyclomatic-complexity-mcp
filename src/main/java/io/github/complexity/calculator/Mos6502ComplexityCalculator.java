package io.github.complexity.calculator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Calculates cyclomatic complexity for MOS 6502 assembler code
 *
 * Supports multiple assembler syntaxes:
 * - Generic 6502 (label + RTS pattern)
 * - ca65 (.proc/.endproc blocks)
 * - DASM (SUBROUTINE directive)
 * - ACME (!zone)
 *
 * Complexity is calculated by counting decision points:
 * - Conditional branches (BEQ, BNE, BCC, BCS, BPL, BMI, BVC, BVS)
 * - Bit branches (BBR0-BBR7, BBS0-BBS7 for 65C02)
 * - Each subroutine starts at complexity 1
 */
public class Mos6502ComplexityCalculator extends AssemblerComplexityCalculatorBase {

    // Patterns for different assembler constructs
    private static final Pattern PROC_PATTERN = Pattern.compile(
        "^\\s*\\.proc\\s+([a-zA-Z_][a-zA-Z0-9_]*)");
    private static final Pattern ENDPROC_PATTERN = Pattern.compile(
        "^\\s*\\.endproc");
    private static final Pattern SUBROUTINE_PATTERN = Pattern.compile(
        "^\\s*SUBROUTINE");
    private static final Pattern LABEL_PATTERN = Pattern.compile(
        "^([a-zA-Z_][a-zA-Z0-9_]*):");
    private static final Pattern RTS_PATTERN = Pattern.compile(
        "^\\s*(RTS|rts)\\b");

    /**
     * Helper class to manage subroutine parsing state.
     */
    private static class SubroutineState extends FunctionState {
        String labelCandidate; // Potential subroutine label waiting for RTS
        int candidateComplexity;

        void setLabelCandidate(String name) {
            this.labelCandidate = name;
            this.candidateComplexity = 1; // Base complexity
        }

        void promoteLabelCandidate() {
            if (labelCandidate != null) {
                this.currentFunction = labelCandidate;
                this.currentComplexity = candidateComplexity;
                this.labelCandidate = null;
                this.candidateComplexity = 0;
            }
        }

        void clearLabelCandidate() {
            this.labelCandidate = null;
            this.candidateComplexity = 0;
        }

        @Override
        public void addComplexity(int points) {
            if (currentFunction != null) {
                this.currentComplexity += points;
            } else if (labelCandidate != null) {
                this.candidateComplexity += points;
            }
        }

        boolean hasCandidate() {
            return labelCandidate != null;
        }
    }

    // 6502 conditional branch instructions
    private static final Set<String> CONDITIONAL_BRANCHES = new HashSet<>(Arrays.asList(
        "BEQ", "BNE",  // Branch on Equal / Not Equal
        "BCC", "BCS",  // Branch on Carry Clear / Set
        "BPL", "BMI",  // Branch on Plus / Minus
        "BVC", "BVS"   // Branch on Overflow Clear / Set
    ));

    // 65C02 bit branch instructions
    private static final Set<String> BIT_BRANCHES = new HashSet<>(Arrays.asList(
        "BBR0", "BBR1", "BBR2", "BBR3", "BBR4", "BBR5", "BBR6", "BBR7",
        "BBS0", "BBS1", "BBS2", "BBS3", "BBS4", "BBS5", "BBS6", "BBS7"
    ));

    @Override
    protected void parseSourceCode(String sourceCode, Map<String, Integer> complexities) throws IOException {
        SubroutineState state = new SubroutineState();

        BufferedReader reader = new BufferedReader(new StringReader(sourceCode));
        String line;
        while ((line = reader.readLine()) != null) {
            String trimmed = preprocessLine(line);
            if (trimmed == null) {
                continue;
            }

            // Check for ca65 .proc/.endproc
            if (processProcStart(trimmed, state, complexities)) continue;
            if (processProcEnd(trimmed, state, complexities)) continue;

            // Check for DASM SUBROUTINE directive
            if (processSubroutineDirective(trimmed, state)) continue;

            // Check for RTS (return from subroutine)
            if (processRts(trimmed, state, complexities)) continue;

            // Check for label (potential subroutine start)
            if (processLabel(trimmed, state)) continue;

            // Count decision points if we're in a subroutine or have a candidate
            if (state.hasFunction() || state.hasCandidate()) {
                state.addComplexity(countDecisionPoints(trimmed));
            }
        }

        // Save last subroutine if exists
        saveSubroutine(state, complexities);
    }

    /**
     * Saves current subroutine state to the complexities map if a subroutine exists.
     */
    private void saveSubroutine(SubroutineState state, Map<String, Integer> complexities) {
        if (state.hasFunction()) {
            complexities.put(state.getCurrentFunction(), state.getCurrentComplexity());
        }
    }

    /**
     * Processes ca65 .proc declaration. Returns true if line was processed.
     */
    private boolean processProcStart(String line, SubroutineState state, Map<String, Integer> complexities) {
        Matcher matcher = PROC_PATTERN.matcher(line);
        if (matcher.find()) {
            saveSubroutine(state, complexities);
            state.clearLabelCandidate();
            state.startFunction(matcher.group(1));
            return true;
        }
        return false;
    }

    /**
     * Processes ca65 .endproc declaration. Returns true if line was processed.
     */
    private boolean processProcEnd(String line, SubroutineState state, Map<String, Integer> complexities) {
        Matcher matcher = ENDPROC_PATTERN.matcher(line);
        if (matcher.find()) {
            saveSubroutine(state, complexities);
            state.endFunction();
            return true;
        }
        return false;
    }

    /**
     * Processes DASM SUBROUTINE directive. Returns true if line was processed.
     */
    private boolean processSubroutineDirective(String line, SubroutineState state) {
        Matcher matcher = SUBROUTINE_PATTERN.matcher(line);
        if (matcher.find()) {
            // DASM SUBROUTINE creates an anonymous boundary
            // Use the last label as the subroutine name
            if (state.hasCandidate()) {
                state.promoteLabelCandidate();
            }
            return true;
        }
        return false;
    }

    /**
     * Processes RTS (return from subroutine). Returns true if line was processed.
     */
    private boolean processRts(String line, SubroutineState state, Map<String, Integer> complexities) {
        Matcher matcher = RTS_PATTERN.matcher(line);
        if (matcher.find()) {
            // If we have a label candidate, promote it to a subroutine and end it
            if (state.hasCandidate()) {
                state.promoteLabelCandidate();
                saveSubroutine(state, complexities);
                state.endFunction();
                return true;
            }
            // If we're in a .proc block, RTS ends the subroutine
            else if (state.hasFunction()) {
                saveSubroutine(state, complexities);
                state.endFunction();
                return true;
            }
        }
        return false;
    }

    /**
     * Processes label declaration. Returns true if line was processed.
     */
    private boolean processLabel(String line, SubroutineState state) {
        Matcher matcher = LABEL_PATTERN.matcher(line);
        if (matcher.find()) {
            String labelName = matcher.group(1);

            // If we're not in a subroutine, this could be a new subroutine
            if (!state.hasFunction()) {
                // Save any previous candidate
                if (state.hasCandidate()) {
                    // Previous candidate didn't end with RTS, might be data label
                    state.clearLabelCandidate();
                }
                state.setLabelCandidate(labelName);
                return true;
            }
            // Labels inside subroutines are just local labels, not new subroutines
        }
        return false;
    }

    @Override
    protected int countDecisionPoints(String instruction) {
        int count = 0;

        // Extract the opcode (first word)
        String[] parts = instruction.split("\\s+");
        if (parts.length == 0) {
            return 0;
        }

        String opcode = parts[0].toUpperCase();

        // Check for conditional branches
        if (CONDITIONAL_BRANCHES.contains(opcode)) {
            count++;
        }

        // Check for bit branches (65C02)
        if (BIT_BRANCHES.contains(opcode)) {
            count++;
        }

        return count;
    }

    @Override
    public String getLanguage() {
        return "6502 Assembler";
    }
}
