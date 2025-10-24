package io.github.complexity.calculator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CalculatorFactoryTest {

    private CalculatorFactory factory;

    @BeforeEach
    void setUp() {
        factory = new CalculatorFactory();
    }

    @Test
    void testCreateJavaCalculator() {
        ComplexityCalculator calculator = factory.createCalculator(Language.JAVA);
        assertNotNull(calculator);
        assertTrue(calculator instanceof JavaComplexityCalculator);
    }

    @Test
    void testCreateX86AssemblerCalculator() {
        ComplexityCalculator calculator = factory.createCalculator(Language.X86_ASSEMBLER);
        assertNotNull(calculator);
        assertTrue(calculator instanceof AssemblerComplexityCalculator);
    }

    @Test
    void testCreate6502Calculator() {
        ComplexityCalculator calculator = factory.createCalculator(Language.MOS6502_ASSEMBLER);
        assertNotNull(calculator);
        assertTrue(calculator instanceof Mos6502ComplexityCalculator);
    }

    @Test
    void testHasCalculator() {
        assertTrue(factory.hasCalculator(Language.JAVA));
        assertTrue(factory.hasCalculator(Language.X86_ASSEMBLER));
        assertTrue(factory.hasCalculator(Language.MOS6502_ASSEMBLER));
    }

    @Test
    void testCreateCalculatorReturnsNewInstance() {
        ComplexityCalculator calc1 = factory.createCalculator(Language.JAVA);
        ComplexityCalculator calc2 = factory.createCalculator(Language.JAVA);

        // Should return new instances each time
        assertNotSame(calc1, calc2);
    }

    @Test
    void testCustomCalculatorRegistration() {
        // Create a custom calculator supplier
        ComplexityCalculator customCalculator = new JavaComplexityCalculator();
        factory.register(Language.JAVA, () -> customCalculator);

        ComplexityCalculator result = factory.createCalculator(Language.JAVA);
        assertSame(customCalculator, result);
    }
}
