package io.github.complexity.calculator;

import java.io.IOException;

/**
 * Interface for cyclomatic complexity calculators
 */
public interface ComplexityCalculator {
    /**
     * Calculate cyclomatic complexity for the given source code
     *
     * @param sourceCode The source code to analyze
     * @param fileName The file name (for reporting)
     * @return ComplexityResult containing the analysis
     * @throws IOException if there's an error reading or parsing the code
     */
    ComplexityResult calculate(String sourceCode, String fileName) throws IOException;

    /**
     * Get the language this calculator supports
     */
    String getLanguage();
}
