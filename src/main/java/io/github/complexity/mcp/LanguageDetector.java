package io.github.complexity.mcp;

import io.github.complexity.exception.UnsupportedLanguageException;

import java.util.Set;

/**
 * Detects programming language from file extensions
 */
public class LanguageDetector {

    private final CalculatorRegistry calculatorRegistry;

    public LanguageDetector(CalculatorRegistry calculatorRegistry) {
        this.calculatorRegistry = calculatorRegistry;
    }

    /**
     * Detect language from file path
     *
     * @param filePath The file path to analyze
     * @return The detected language key
     * @throws UnsupportedLanguageException if language cannot be detected
     */
    public String detectLanguage(String filePath) throws UnsupportedLanguageException {
        String lower = filePath.toLowerCase();

        if (lower.endsWith(".java")) {
            return "java";
        } else if (lower.endsWith(".asm") || lower.endsWith(".s")) {
            return "asm";
        } else if (lower.endsWith(".a65") || lower.endsWith(".s65") ||
                   lower.endsWith(".asm65") || lower.endsWith(".a")) {
            return "6502";
        }

        // Extract extension for error message
        int lastDot = filePath.lastIndexOf('.');
        String extension = lastDot > 0 ? filePath.substring(lastDot) : "<no extension>";

        throw new UnsupportedLanguageException(
            "file with extension '" + extension + "'",
            calculatorRegistry.getSupportedLanguages()
        );
    }

    /**
     * Get supported languages
     */
    public Set<String> getSupportedLanguages() {
        return calculatorRegistry.getSupportedLanguages();
    }
}
