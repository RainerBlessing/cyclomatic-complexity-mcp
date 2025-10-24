package io.github.complexity.calculator;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Factory for creating complexity calculator instances
 * Uses supplier pattern for lazy instantiation
 */
public class CalculatorFactory {

    private final Map<Language, Supplier<ComplexityCalculator>> calculatorSuppliers = new HashMap<>();

    public CalculatorFactory() {
        registerDefaultCalculators();
    }

    /**
     * Register default calculators for all supported languages
     */
    private void registerDefaultCalculators() {
        register(Language.JAVA, JavaComplexityCalculator::new);
        register(Language.X86_ASSEMBLER, AssemblerComplexityCalculator::new);
        register(Language.MOS6502_ASSEMBLER, Mos6502ComplexityCalculator::new);
    }

    /**
     * Register a calculator supplier for a specific language
     *
     * @param language The language to register for
     * @param supplier Supplier that creates calculator instances
     */
    public void register(Language language, Supplier<ComplexityCalculator> supplier) {
        calculatorSuppliers.put(language, supplier);
    }

    /**
     * Create a calculator instance for the specified language
     *
     * @param language The language to create a calculator for
     * @return A new calculator instance
     * @throws IllegalArgumentException if no calculator is registered for the language
     */
    public ComplexityCalculator createCalculator(Language language) {
        Supplier<ComplexityCalculator> supplier = calculatorSuppliers.get(language);
        if (supplier == null) {
            throw new IllegalArgumentException(
                "No calculator registered for language: " + language
            );
        }
        return supplier.get();
    }

    /**
     * Check if a calculator is available for the specified language
     */
    public boolean hasCalculator(Language language) {
        return calculatorSuppliers.containsKey(language);
    }
}
