package io.github.complexity.calculator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AssemblerComplexityCalculatorTest {

    private AssemblerComplexityCalculator calculator;

    @BeforeEach
    void setUp() {
        calculator = new AssemblerComplexityCalculator();
    }

    @Test
    void testSimpleProcedure() throws Exception {
        String code = """
            MyProc PROC
                mov eax, 1
                ret
            MyProc ENDP
            """;
        ComplexityResult result = calculator.calculate(code, "test.asm");

        assertEquals("Assembler", result.getLanguage());
        assertEquals(1, result.getFunctionComplexities().size());
        assertEquals(1, result.getFunctionComplexities().get("MyProc"));
    }

    @Test
    void testProcWithConditionalJumps() throws Exception {
        String code = """
            CheckValue PROC
                cmp eax, 0
                je zero_case
                jg positive_case
                jmp end
            zero_case:
                mov ebx, 0
                jmp end
            positive_case:
                mov ebx, 1
            end:
                ret
            CheckValue ENDP
            """;
        ComplexityResult result = calculator.calculate(code, "test.asm");

        // Complexity: 1 (base) + 1 (je) + 1 (jg) + 1 (jmp) = 4
        // Note: jmp is not conditional, so it doesn't count
        // Actual: 1 (base) + 1 (je) + 1 (jg) = 3
        assertEquals(3, result.getFunctionComplexities().get("CheckValue"));
    }

    @Test
    void testLoopInstructions() throws Exception {
        String code = """
            LoopProc PROC
                mov ecx, 10
            loop_start:
                loop loop_start
                ret
            LoopProc ENDP
            """;
        ComplexityResult result = calculator.calculate(code, "test.asm");

        // Complexity: 1 (base) + 1 (loop) = 2
        assertEquals(2, result.getFunctionComplexities().get("LoopProc"));
    }

    @Test
    void testConditionalMoves() throws Exception {
        String code = """
            ConditionalProc PROC
                cmp eax, ebx
                cmove ecx, edx
                cmovg esi, edi
                ret
            ConditionalProc ENDP
            """;
        ComplexityResult result = calculator.calculate(code, "test.asm");

        // Complexity: 1 (base) + 1 (cmove) + 1 (cmovg) = 3
        assertEquals(3, result.getFunctionComplexities().get("ConditionalProc"));
    }

    @Test
    void testLabelBasedFunction() throws Exception {
        String code = """
            my_function:
                cmp eax, 0
                je done
                inc eax
            done:
                ret
            """;
        ComplexityResult result = calculator.calculate(code, "test.asm");

        // Complexity: 1 (base) + 1 (je) = 2
        assertEquals(2, result.getFunctionComplexities().get("my_function"));
    }

    @Test
    void testMultipleFunctions() throws Exception {
        String code = """
            Func1 PROC
                mov eax, 1
                ret
            Func1 ENDP

            Func2 PROC
                cmp eax, 0
                je label1
                jne label2
            label1:
                ret
            label2:
                ret
            Func2 ENDP
            """;
        ComplexityResult result = calculator.calculate(code, "test.asm");

        assertEquals(2, result.getFunctionComplexities().size());
        assertEquals(1, result.getFunctionComplexities().get("Func1"));
        assertEquals(3, result.getFunctionComplexities().get("Func2")); // 1 + je + jne
    }

    @Test
    void testGlobalComplexity() throws Exception {
        String code = """
            ; Code without explicit functions
            mov eax, 1
            cmp eax, 0
            je label1
            jne label2
            label1:
                ret
            label2:
                ret
            """;
        ComplexityResult result = calculator.calculate(code, "test.asm");

        // Should have _global_ entry
        assertTrue(result.getFunctionComplexities().containsKey("_global_"));
        assertEquals(3, result.getFunctionComplexities().get("_global_")); // 1 + je + jne
    }

    @Test
    void testCommentsAreIgnored() throws Exception {
        String code = """
            MyProc PROC
                ; This is a comment with je in it
                mov eax, 1  ; je jne jg
                ret
            MyProc ENDP
            """;
        ComplexityResult result = calculator.calculate(code, "test.asm");

        assertEquals(1, result.getFunctionComplexities().get("MyProc"));
    }

    @Test
    void testCaseInsensitivity() throws Exception {
        String code = """
            myproc proc
                CMP eax, 0
                JE label
                JNE label2
            label:
                RET
            label2:
                RET
            myproc endp
            """;
        ComplexityResult result = calculator.calculate(code, "test.asm");

        assertEquals(3, result.getFunctionComplexities().get("myproc"));
    }

    @Test
    void testAllConditionalJumps() throws Exception {
        String code = """
            AllJumps PROC
                je l1
                jne l2
                jz l3
                jnz l4
                jg l5
                jl l6
                jge l7
                jle l8
                ja l9
                jb l10
                jae l11
                jbe l12
            l1: l2: l3: l4: l5: l6: l7: l8: l9: l10: l11: l12:
                ret
            AllJumps ENDP
            """;
        ComplexityResult result = calculator.calculate(code, "test.asm");

        // 1 (base) + 12 (conditional jumps) = 13
        assertEquals(13, result.getFunctionComplexities().get("AllJumps"));
    }

    @Test
    void testGetLanguage() {
        assertEquals("Assembler", calculator.getLanguage());
    }
}
