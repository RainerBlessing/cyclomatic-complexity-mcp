package io.github.complexity.calculator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Calculates cyclomatic complexity for x86/x64 assembler code
 *
 * Complexity is calculated by counting decision points:
 * - Conditional jumps (JE, JNE, JZ, JNZ, JG, JL, etc.)
 * - Loop instructions (LOOP, LOOPE, LOOPNE, etc.)
 * - Conditional returns
 * - Each function/procedure starts at complexity 1
 */
public class AssemblerComplexityCalculator implements ComplexityCalculator {

    // Patterns for different assembler constructs
    private static final Pattern PROC_PATTERN = Pattern.compile(
        "^\\s*([a-zA-Z_][a-zA-Z0-9_]*)\\s+(PROC|proc)\\b");
    private static final Pattern ENDP_PATTERN = Pattern.compile(
        "^\\s*([a-zA-Z_][a-zA-Z0-9_]*)\\s+(ENDP|endp)\\b");
    private static final Pattern LABEL_PATTERN = Pattern.compile(
        "^\\s*([a-zA-Z_][a-zA-Z0-9_]*):");

    /**
     * Helper class to manage function parsing state.
     */
    private static class FunctionState {
        String currentFunction;
        int currentComplexity;

        void startFunction(String name) {
            this.currentFunction = name;
            this.currentComplexity = 1;
        }

        void endFunction() {
            this.currentFunction = null;
            this.currentComplexity = 0;
        }

        void addComplexity(int points) {
            this.currentComplexity += points;
        }

        boolean hasFunction() {
            return currentFunction != null;
        }
    }

    // Conditional jump instructions
    private static final Set<String> CONDITIONAL_JUMPS = new HashSet<>(Arrays.asList(
        "JE", "JZ", "JNE", "JNZ", "JG", "JNLE", "JGE", "JNL",
        "JL", "JNGE", "JLE", "JNG", "JA", "JNBE", "JAE", "JNB",
        "JB", "JNAE", "JBE", "JNA", "JP", "JPE", "JNP", "JPO",
        "JC", "JNC", "JO", "JNO", "JS", "JNS",
        "JECXZ", "JCXZ", "JRCXZ" // Conditional jumps based on CX/ECX/RCX
    ));

    // Loop instructions
    private static final Set<String> LOOP_INSTRUCTIONS = new HashSet<>(Arrays.asList(
        "LOOP", "LOOPE", "LOOPZ", "LOOPNE", "LOOPNZ"
    ));

    // Conditional move instructions (also add complexity)
    private static final Set<String> CONDITIONAL_MOVES = new HashSet<>(Arrays.asList(
        "CMOVE", "CMOVZ", "CMOVNE", "CMOVNZ", "CMOVG", "CMOVGE",
        "CMOVL", "CMOVLE", "CMOVA", "CMOVAE", "CMOVB", "CMOVBE",
        "CMOVP", "CMOVNP", "CMOVO", "CMOVNO", "CMOVS", "CMOVNS"
    ));

    @Override
    public ComplexityResult calculate(String sourceCode, String fileName) throws IOException {
        Map<String, Integer> complexities = new HashMap<>();
        FunctionState state = new FunctionState();

        BufferedReader reader = new BufferedReader(new StringReader(sourceCode));
        String line;
        while ((line = reader.readLine()) != null) {
            String trimmed = preprocessLine(line);
            if (trimmed == null) {
                continue;
            }

            if (processProcStart(trimmed, state, complexities)) continue;
            if (processProcEnd(trimmed, state, complexities)) continue;
            if (processLabel(trimmed, state)) continue;

            // Count decision points if we're in a function
            if (state.hasFunction()) {
                state.addComplexity(countDecisionPoints(trimmed));
            }
        }

        // Save last function if exists
        saveFunction(state, complexities);

        // If no functions found, treat whole file as one function
        if (complexities.isEmpty()) {
            complexities.put("_global_", calculateGlobalComplexity(sourceCode));
        }

        return new ComplexityResult(fileName, "Assembler", complexities);
    }

    /**
     * Saves current function state to the complexities map if a function exists.
     */
    private void saveFunction(FunctionState state, Map<String, Integer> complexities) {
        if (state.hasFunction()) {
            complexities.put(state.currentFunction, state.currentComplexity);
        }
    }

    /**
     * Processes PROC declaration. Returns true if line was processed.
     */
    private boolean processProcStart(String line, FunctionState state, Map<String, Integer> complexities) {
        Matcher matcher = PROC_PATTERN.matcher(line);
        if (matcher.find()) {
            saveFunction(state, complexities);
            state.startFunction(matcher.group(1));
            return true;
        }
        return false;
    }

    /**
     * Processes ENDP declaration. Returns true if line was processed.
     */
    private boolean processProcEnd(String line, FunctionState state, Map<String, Integer> complexities) {
        Matcher matcher = ENDP_PATTERN.matcher(line);
        if (matcher.find()) {
            saveFunction(state, complexities);
            state.endFunction();
            return true;
        }
        return false;
    }

    /**
     * Processes label declaration. Returns true if line was processed.
     */
    private boolean processLabel(String line, FunctionState state) {
        if (state.hasFunction()) {
            return false;
        }

        Matcher matcher = LABEL_PATTERN.matcher(line);
        if (matcher.find()) {
            state.startFunction(matcher.group(1));
            return true;
        }
        return false;
    }

    /**
     * Calculates complexity for files without explicit function declarations.
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

        // Check for conditional jumps
        if (CONDITIONAL_JUMPS.contains(opcode)) {
            count++;
        }

        // Check for loop instructions
        if (LOOP_INSTRUCTIONS.contains(opcode)) {
            count++;
        }

        // Check for conditional moves
        if (CONDITIONAL_MOVES.contains(opcode)) {
            count++;
        }

        return count;
    }

    @Override
    public String getLanguage() {
        return "Assembler";
    }
}
