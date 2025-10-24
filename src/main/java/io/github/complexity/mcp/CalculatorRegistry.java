package io.github.complexity.mcp;

import io.github.complexity.calculator.*;
import io.github.complexity.exception.UnsupportedLanguageException;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Registry for complexity calculators
 * Manages available calculators and provides lookup by language
 */
public class CalculatorRegistry {

    private final CalculatorFactory calculatorFactory;
    private final Map<Language, ComplexityCalculator> calculatorCache = new HashMap<>();

    public CalculatorRegistry(CalculatorFactory calculatorFactory) {
        this.calculatorFactory = calculatorFactory;
    }

    /**
     * Get calculator for a specific language (with caching)
     *
     * @throws UnsupportedLanguageException if language is not supported
     */
    public ComplexityCalculator getCalculator(Language language) throws UnsupportedLanguageException {
        // Check cache first
        if (calculatorCache.containsKey(language)) {
            return calculatorCache.get(language);
        }

        // Create new calculator
        try {
            ComplexityCalculator calculator = calculatorFactory.createCalculator(language);
            calculatorCache.put(language, calculator);
            return calculator;
        } catch (IllegalArgumentException e) {
            throw new UnsupportedLanguageException(
                language.getKey(),
                getSupportedLanguageKeys()
            );
        }
    }

    /**
     * Get all supported language keys
     */
    public Set<String> getSupportedLanguageKeys() {
        return Arrays.stream(Language.values())
            .map(Language::getKey)
            .collect(Collectors.toSet());
    }

    /**
     * Check if a language is supported
     */
    public boolean isLanguageSupported(Language language) {
        return calculatorFactory.hasCalculator(language);
    }
}
