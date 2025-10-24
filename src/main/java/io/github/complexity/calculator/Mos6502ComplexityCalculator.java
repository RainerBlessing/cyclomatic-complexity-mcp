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
public class Mos6502ComplexityCalculator implements ComplexityCalculator {

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
    private static class SubroutineState {
        String currentSubroutine;
        int currentComplexity;
        String labelCandidate; // Potential subroutine label waiting for RTS
        int candidateComplexity;

        void startSubroutine(String name) {
            this.currentSubroutine = name;
            this.currentComplexity = 1;
            this.labelCandidate = null;
            this.candidateComplexity = 0;
        }

        void endSubroutine() {
            this.currentSubroutine = null;
            this.currentComplexity = 0;
        }

        void setLabelCandidate(String name) {
            this.labelCandidate = name;
            this.candidateComplexity = 1; // Base complexity
        }

        void promoteLabelCandidate() {
            if (labelCandidate != null) {
                this.currentSubroutine = labelCandidate;
                this.currentComplexity = candidateComplexity;
                this.labelCandidate = null;
                this.candidateComplexity = 0;
            }
        }

        void clearLabelCandidate() {
            this.labelCandidate = null;
            this.candidateComplexity = 0;
        }

        void addComplexity(int points) {
            if (currentSubroutine != null) {
                this.currentComplexity += points;
            } else if (labelCandidate != null) {
                this.candidateComplexity += points;
            }
        }

        boolean hasSubroutine() {
            return currentSubroutine != null;
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
    public ComplexityResult calculate(String sourceCode, String fileName) throws IOException {
        Map<String, Integer> complexities = new HashMap<>();
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
            if (processSubroutineDirective(trimmed, state, complexities)) continue;

            // Check for RTS (return from subroutine)
            if (processRts(trimmed, state, complexities)) continue;

            // Check for label (potential subroutine start)
            if (processLabel(trimmed, state, complexities)) continue;

            // Count decision points if we're in a subroutine or have a candidate
            if (state.hasSubroutine() || state.hasCandidate()) {
                state.addComplexity(countDecisionPoints(trimmed));
            }
        }

        // Save last subroutine if exists
        saveSubroutine(state, complexities);

        // If no subroutines found, treat whole file as one function
        if (complexities.isEmpty()) {
            complexities.put("_global_", calculateGlobalComplexity(sourceCode));
        }

        return new ComplexityResult(fileName, "6502 Assembler", complexities);
    }

    /**
     * Saves current subroutine state to the complexities map if a subroutine exists.
     */
    private void saveSubroutine(SubroutineState state, Map<String, Integer> complexities) {
        if (state.hasSubroutine()) {
            complexities.put(state.currentSubroutine, state.currentComplexity);
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
            state.startSubroutine(matcher.group(1));
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
            state.endSubroutine();
            return true;
        }
        return false;
    }

    /**
     * Processes DASM SUBROUTINE directive. Returns true if line was processed.
     */
    private boolean processSubroutineDirective(String line, SubroutineState state, Map<String, Integer> complexities) {
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
                state.endSubroutine();
                return true;
            }
            // If we're in a .proc block, RTS ends the subroutine
            else if (state.hasSubroutine()) {
                saveSubroutine(state, complexities);
                state.endSubroutine();
                return true;
            }
        }
        return false;
    }

    /**
     * Processes label declaration. Returns true if line was processed.
     */
    private boolean processLabel(String line, SubroutineState state, Map<String, Integer> complexities) {
        Matcher matcher = LABEL_PATTERN.matcher(line);
        if (matcher.find()) {
            String labelName = matcher.group(1);

            // If we're not in a subroutine, this could be a new subroutine
            if (!state.hasSubroutine()) {
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

    /**
     * Calculates complexity for files without explicit subroutine declarations.
     */
    private int calculateGlobalComplexity(String sourceCode) throws IOException {
        int totalComplexity = 1;
        BufferedReader reader = new BufferedReader(new StringReader(sourceCode));
        String line;
        while ((line = reader.readLine()) != null) {
            String trimmed = preprocessLine(line);
            if (trimmed != null) {
                totalComplexity += countDecisionPoints(trimmed);
            }
        }
        return totalComplexity;
    }

    /**
     * Preprocesses a line by trimming and removing comments.
     * @return the preprocessed line, or null if the line should be skipped
     */
    private String preprocessLine(String line) {
        String trimmed = line.trim();

        // Skip empty lines and comments
        if (trimmed.isEmpty() || trimmed.startsWith(";")) {
            return null;
        }

        // Remove inline comments
        int commentPos = trimmed.indexOf(';');
        if (commentPos >= 0) {
            trimmed = trimmed.substring(0, commentPos).trim();
        }

        return trimmed;
    }

    private int countDecisionPoints(String instruction) {
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
