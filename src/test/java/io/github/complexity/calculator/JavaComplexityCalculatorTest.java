package io.github.complexity.calculator;

import io.github.complexity.exception.ParsingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JavaComplexityCalculatorTest {

    private JavaComplexityCalculator calculator;

    @BeforeEach
    void setUp() {
        calculator = new JavaComplexityCalculator();
    }

    @Test
    void testSimpleMethod() throws Exception {
        String code = "class Test { void simple() { System.out.println(\"hi\"); } }";
        ComplexityResult result = calculator.calculate(code, "Test.java");

        assertEquals("Java", result.getLanguage());
        assertEquals(1, result.getFunctionComplexities().size());
        assertTrue(result.getFunctionComplexities().containsKey("simple()"));
        assertEquals(1, result.getFunctionComplexities().get("simple()"));
    }

    @Test
    void testMethodWithIfStatement() throws Exception {
        String code = "class Test { void method(int x) { if (x > 0) { return; } } }";
        ComplexityResult result = calculator.calculate(code, "Test.java");

        assertEquals(2, result.getFunctionComplexities().get("method(int)"));
    }

    @Test
    void testMethodWithMultipleDecisionPoints() throws Exception {
        String code = """
            class Test {
                int complex(int x) {
                    if (x > 0) {
                        for (int i = 0; i < x; i++) {
                            if (i % 2 == 0) {
                                System.out.println(i);
                            }
                        }
                    } else if (x < 0) {
                        while (x < 0) {
                            x++;
                        }
                    }
                    return x;
                }
            }
            """;
        ComplexityResult result = calculator.calculate(code, "Test.java");

        // Complexity: 1 (base) + 1 (if) + 1 (for) + 1 (if) + 1 (else if) + 1 (while) = 6
        assertEquals(6, result.getFunctionComplexities().get("complex(int)"));
    }

    @Test
    void testMethodWithTernaryOperator() throws Exception {
        String code = "class Test { int method(int x) { return x > 0 ? 1 : 0; } }";
        ComplexityResult result = calculator.calculate(code, "Test.java");

        assertEquals(2, result.getFunctionComplexities().get("method(int)"));
    }

    @Test
    void testMethodWithLogicalOperators() throws Exception {
        String code = "class Test { boolean method(int x, int y) { return x > 0 && y > 0 || x < 0; } }";
        ComplexityResult result = calculator.calculate(code, "Test.java");

        // Complexity: 1 (base) + 1 (&&) + 1 (||) = 3
        assertEquals(3, result.getFunctionComplexities().get("method(int, int)"));
    }

    @Test
    void testMethodWithSwitchStatement() throws Exception {
        String code = """
            class Test {
                void method(int x) {
                    switch(x) {
                        case 1: break;
                        case 2: break;
                        case 3: break;
                        default: break;
                    }
                }
            }
            """;
        ComplexityResult result = calculator.calculate(code, "Test.java");

        // Complexity: 1 (base) + 3 (cases) + 1 (default) = 5
        assertEquals(5, result.getFunctionComplexities().get("method(int)"));
    }

    @Test
    void testMethodWithTryCatch() throws Exception {
        String code = """
            class Test {
                void method() {
                    try {
                        System.out.println("test");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            """;
        ComplexityResult result = calculator.calculate(code, "Test.java");

        // Complexity: 1 (base) + 1 (catch) = 2
        assertEquals(2, result.getFunctionComplexities().get("method()"));
    }

    @Test
    void testInvalidJavaCode() {
        String code = "this is not valid java code {{{";

        assertThrows(ParsingException.class, () -> {
            calculator.calculate(code, "Invalid.java");
        });
    }

    @Test
    void testMultipleMethods() throws Exception {
        String code = """
            class Test {
                void method1() { }
                void method2() { if (true) { } }
                void method3() {
                    if (true) { }
                    if (false) { }
                }
            }
            """;
        ComplexityResult result = calculator.calculate(code, "Test.java");

        assertEquals(3, result.getFunctionComplexities().size());
        assertEquals(1, result.getFunctionComplexities().get("method1()"));
        assertEquals(2, result.getFunctionComplexities().get("method2()"));
        assertEquals(3, result.getFunctionComplexities().get("method3()"));
    }

    @Test
    void testGetLanguage() {
        assertEquals("Java", calculator.getLanguage());
    }

    @Test
    void testResultMetrics() throws Exception {
        String code = """
            class Test {
                void simple() { }
                void complex() {
                    if (true) { }
                    if (false) { }
                    if (true) { }
                }
            }
            """;
        ComplexityResult result = calculator.calculate(code, "Test.java");

        assertEquals(5, result.getTotalComplexity()); // 1 + 4
        assertEquals(4, result.getMaxComplexity());
        assertEquals("complex()", result.getMostComplexFunction());
    }
}
