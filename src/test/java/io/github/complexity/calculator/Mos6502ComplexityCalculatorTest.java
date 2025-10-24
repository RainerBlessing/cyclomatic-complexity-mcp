package io.github.complexity.calculator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class Mos6502ComplexityCalculatorTest {

    private Mos6502ComplexityCalculator calculator;

    @BeforeEach
    void setUp() {
        calculator = new Mos6502ComplexityCalculator();
    }

    @Test
    void testSimpleProcedure() throws Exception {
        String code = """
            .proc init
                LDA #$00
                STA $0200
                RTS
            .endproc
            """;
        ComplexityResult result = calculator.calculate(code, "test.asm");

        assertEquals("6502 Assembler", result.getLanguage());
        assertEquals(1, result.getFunctionComplexities().size());
        assertEquals(1, result.getFunctionComplexities().get("init"));
    }

    @Test
    void testProcWithBranches() throws Exception {
        String code = """
            .proc check_value
                LDA $0200
                BEQ zero_branch
                BNE nonzero_branch
            zero_branch:
                LDA #$01
                RTS
            nonzero_branch:
                LDA #$02
                RTS
            .endproc
            """;
        ComplexityResult result = calculator.calculate(code, "test.asm");

        // Complexity: 1 (base) + 1 (BEQ) + 1 (BNE) = 3
        assertEquals(3, result.getFunctionComplexities().get("check_value"));
    }

    @Test
    void testLabelWithRTS() throws Exception {
        String code = """
            my_subroutine:
                LDA #$FF
                BCC carry_clear
                LDA #$00
            carry_clear:
                RTS
            """;
        ComplexityResult result = calculator.calculate(code, "test.asm");

        // Complexity: 1 (base) + 1 (BCC) = 2
        assertEquals(2, result.getFunctionComplexities().get("my_subroutine"));
    }

    @Test
    void testDasmSubroutine() throws Exception {
        String code = """
            init_screen:
                LDA #$00
                SUBROUTINE
                BEQ done
                LDA #$01
            done:
                RTS
            """;
        ComplexityResult result = calculator.calculate(code, "test.asm");

        // Complexity: 1 (base) + 1 (BEQ) = 2
        assertEquals(2, result.getFunctionComplexities().get("init_screen"));
    }

    @Test
    void testAllConditionalBranches() throws Exception {
        String code = """
            .proc all_branches
                BEQ l1
                BNE l2
                BCC l3
                BCS l4
                BPL l5
                BMI l6
                BVC l7
                BVS l8
            l1: l2: l3: l4: l5: l6: l7: l8:
                RTS
            .endproc
            """;
        ComplexityResult result = calculator.calculate(code, "test.asm");

        // 1 (base) + 8 (branches) = 9
        assertEquals(9, result.getFunctionComplexities().get("all_branches"));
    }

    @Test
    void test65C02BitBranches() throws Exception {
        String code = """
            .proc bit_test
                BBR0 $00, label1
                BBS7 $01, label2
                BBR3 $02, label3
            label1:
            label2:
            label3:
                RTS
            .endproc
            """;
        ComplexityResult result = calculator.calculate(code, "test.asm");

        // 1 (base) + 3 (bit branches) = 4
        assertEquals(4, result.getFunctionComplexities().get("bit_test"));
    }

    @Test
    void testMultipleProcedures() throws Exception {
        String code = """
            .proc proc1
                LDA #$00
                RTS
            .endproc

            .proc proc2
                LDA #$00
                BEQ label
                BNE label
            label:
                RTS
            .endproc
            """;
        ComplexityResult result = calculator.calculate(code, "test.asm");

        assertEquals(2, result.getFunctionComplexities().size());
        assertEquals(1, result.getFunctionComplexities().get("proc1"));
        assertEquals(3, result.getFunctionComplexities().get("proc2"));
    }

    @Test
    void testLabelWithoutRTS() throws Exception {
        String code = """
            data_label:
                .byte $00, $01, $02

            real_subroutine:
                LDA data_label
                RTS
            """;
        ComplexityResult result = calculator.calculate(code, "test.asm");

        // data_label should not be counted as it has no RTS
        assertEquals(1, result.getFunctionComplexities().size());
        assertEquals(1, result.getFunctionComplexities().get("real_subroutine"));
    }

    @Test
    void testCommentsIgnored() throws Exception {
        String code = """
            .proc test
                ; BEQ BNE in comment
                LDA #$00  ; BCC inline comment
                RTS
            .endproc
            """;
        ComplexityResult result = calculator.calculate(code, "test.asm");

        assertEquals(1, result.getFunctionComplexities().get("test"));
    }

    @Test
    void testCaseInsensitivity() throws Exception {
        String code = """
            .proc TEST
                lda #$00
                beq label
                bne label
            label:
                rts
            .endproc
            """;
        ComplexityResult result = calculator.calculate(code, "test.asm");

        assertEquals(3, result.getFunctionComplexities().get("TEST"));
    }

    @Test
    void testNestedLabelsInProc() throws Exception {
        String code = """
            .proc outer
                LDA #$00
            inner_label:
                BEQ inner_label
                RTS
            .endproc
            """;
        ComplexityResult result = calculator.calculate(code, "test.asm");

        // inner_label should not start a new subroutine
        assertEquals(1, result.getFunctionComplexities().size());
        assertEquals(2, result.getFunctionComplexities().get("outer"));
    }

    @Test
    void testGlobalComplexity() throws Exception {
        String code = """
            ; Code without explicit functions
            LDA #$00
            BEQ label
            BNE label
            label:
                NOP
            """;
        ComplexityResult result = calculator.calculate(code, "test.asm");

        assertTrue(result.getFunctionComplexities().containsKey("_global_"));
        assertEquals(3, result.getFunctionComplexities().get("_global_"));
    }

    @Test
    void testGetLanguage() {
        assertEquals("6502 Assembler", calculator.getLanguage());
    }
}
