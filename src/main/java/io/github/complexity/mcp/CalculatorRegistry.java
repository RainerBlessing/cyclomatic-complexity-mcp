package io.github.complexity.mcp;

import io.github.complexity.calculator.*;
import io.github.complexity.exception.UnsupportedLanguageException;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Registry for complexity calculators
 * Manages available calculators and provides lookup by language
 */
public class CalculatorRegistry {

    private final Map<String, ComplexityCalculator> calculators = new HashMap<>();

    public CalculatorRegistry() {
        // Register default calculators
        registerCalculator("java", new JavaComplexityCalculator());
        registerCalculator("asm", new AssemblerComplexityCalculator());
        registerCalculator("s", new AssemblerComplexityCalculator());
        registerCalculator("6502", new Mos6502ComplexityCalculator());
    }

    /**
     * Register a calculator for a specific language
     */
    public void registerCalculator(String language, ComplexityCalculator calculator) {
        calculators.put(language.toLowerCase(), calculator);
    }

    /**
     * Get calculator for a specific language
     *
     * @throws UnsupportedLanguageException if language is not supported
     */
    public ComplexityCalculator getCalculator(String language) throws UnsupportedLanguageException {
        ComplexityCalculator calculator = calculators.get(language.toLowerCase());
        if (calculator == null) {
            throw new UnsupportedLanguageException(language, getSupportedLanguages());
        }
        return calculator;
    }

    /**
     * Get all supported languages
     */
    public Set<String> getSupportedLanguages() {
        return calculators.keySet();
    }

    /**
     * Check if a language is supported
     */
    public boolean isLanguageSupported(String language) {
        return calculators.containsKey(language.toLowerCase());
    }
}
