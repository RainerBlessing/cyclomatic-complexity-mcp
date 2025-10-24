package io.github.complexity.calculator;

import io.github.complexity.exception.ComplexityCalculationException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

/**
 * Base class for assembler complexity calculators
 * Provides common functionality for parsing assembler code
 */
public abstract class AssemblerComplexityCalculatorBase implements ComplexityCalculator {

    /**
     * Calculate complexity for the given source code
     */
    @Override
    public ComplexityResult calculate(String sourceCode, String fileName) throws ComplexityCalculationException {
        try {
            Map<String, Integer> complexities = new HashMap<>();
            parseSourceCode(sourceCode, complexities);

            // If no functions found, treat whole file as one function
            if (complexities.isEmpty()) {
                complexities.put("_global_", calculateGlobalComplexity(sourceCode));
            }

            return new ComplexityResult(fileName, getLanguage(), complexities);
        } catch (IOException e) {
            throw new ComplexityCalculationException(fileName, e.getMessage(), e);
        }
    }

    /**
     * Parse source code and extract function complexities
     * Must be implemented by subclasses for language-specific parsing
     *
     * @param sourceCode The source code to parse
     * @param complexities Map to store function name -> complexity
     * @throws IOException if reading fails
     */
    protected abstract void parseSourceCode(String sourceCode, Map<String, Integer> complexities) throws IOException;

    /**
     * Count decision points in a single instruction
     * Must be implemented by subclasses for language-specific instructions
     *
     * @param instruction The instruction to analyze
     * @return Number of decision points (0 or more)
     */
    protected abstract int countDecisionPoints(String instruction);

    /**
     * Calculates complexity for files without explicit function declarations
     */
    protected int calculateGlobalComplexity(String sourceCode) throws IOException {
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
     * Preprocesses a line by trimming and removing comments
     *
     * @param line The line to preprocess
     * @return the preprocessed line, or null if the line should be skipped
     */
    protected String preprocessLine(String line) {
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

    /**
     * Helper class to manage function parsing state
     */
    protected static class FunctionState {
        protected String currentFunction;
        protected int currentComplexity;

        public void startFunction(String name) {
            this.currentFunction = name;
            this.currentComplexity = 1;
        }

        public void endFunction() {
            this.currentFunction = null;
            this.currentComplexity = 0;
        }

        public void addComplexity(int points) {
            this.currentComplexity += points;
        }

        public boolean hasFunction() {
            return currentFunction != null;
        }

        public String getCurrentFunction() {
            return currentFunction;
        }

        public int getCurrentComplexity() {
            return currentComplexity;
        }
    }
}
