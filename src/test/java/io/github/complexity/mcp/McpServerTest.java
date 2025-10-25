package io.github.complexity.mcp;

import org.junit.jupiter.api.Test;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for McpServer, focusing on assembler type detection
 */
class McpServerTest {

    /**
     * Helper method to access private detectAssemblerType method via reflection
     */
    private String callDetectAssemblerType(String sourceCode) throws Exception {
        McpServer server = new McpServer();
        Method method = McpServer.class.getDeclaredMethod("detectAssemblerType", String.class);
        method.setAccessible(true);
        return (String) method.invoke(server, sourceCode);
    }

    @Test
    void detectAssemblerType_pure6502_returns6502() throws Exception {
        String code = """
            ; 6502 Assembly
            clear_screen:
                LDA #$20
                LDX #$00
            loop:
                STA $0400,X
                INX
                BNE loop
                RTS
            """;

        String result = callDetectAssemblerType(code);
        assertEquals("6502", result, "Pure 6502 code should be detected as 6502");
    }

    @Test
    void detectAssemblerType_pureX86_returnsAsm() throws Exception {
        String code = """
            section .text
            global main
            main:
                mov eax, 0
                push rbp
                mov rbp, rsp
                call printf
                ret
            """;

        String result = callDetectAssemblerType(code);
        assertEquals("asm", result, "Pure x86 code should be detected as asm");
    }

    @Test
    void detectAssemblerType_emptyFile_returnsAsm() throws Exception {
        String code = "";

        String result = callDetectAssemblerType(code);
        assertEquals("asm", result, "Empty file should default to asm");
    }

    @Test
    void detectAssemblerType_onlyComments_returnsAsm() throws Exception {
        String code = """
            ; This is a comment
            ; Another comment
            ; No actual code here
            """;

        String result = callDetectAssemblerType(code);
        assertEquals("asm", result, "File with only comments should default to asm");
    }

    @Test
    void detectAssemblerType_6502WithDirectives_returns6502() throws Exception {
        String code = """
            PROCESSOR 6502
            ORG $8000

            start:
                LDA #$00
                STA $D020
                RTS
            """;

        String result = callDetectAssemblerType(code);
        assertEquals("6502", result, "6502 code with PROCESSOR directive should be detected as 6502");
    }

    @Test
    void detectAssemblerType_ca65Syntax_returns6502() throws Exception {
        String code = """
            .proc init
                LDA #$00
                STA $D020
                RTS
            .endproc
            """;

        String result = callDetectAssemblerType(code);
        assertEquals("6502", result, "ca65 .proc/.endproc syntax should be detected as 6502");
    }

    @Test
    void detectAssemblerType_dasmSyntax_returns6502() throws Exception {
        String code = """
                SUBROUTINE
            wait_key:
                LDA $C5
                BEQ wait_key
                RTS
            """;

        String result = callDetectAssemblerType(code);
        assertEquals("6502", result, "DASM SUBROUTINE directive should be detected as 6502");
    }

    @Test
    void detectAssemblerType_x86WithRegisters_returnsAsm() throws Exception {
        String code = """
            mov rax, rbx
            add rcx, rdx
            push eax
            pop ebx
            """;

        String result = callDetectAssemblerType(code);
        assertEquals("asm", result, "x86 code with specific registers should be detected as asm");
    }

    @Test
    void detectAssemblerType_mixedInstructions_selectsHigherScore() throws Exception {
        // Code with predominantly 6502 instructions but one x86 instruction
        String code6502Heavy = """
            start:
                LDA #$00
                LDX #$01
                LDY #$02
                STA $1000
                STX $1001
                STY $1002
                BEQ done
                BNE start
                mov eax, 0
            done:
                RTS
            """;

        String result = callDetectAssemblerType(code6502Heavy);
        assertEquals("6502", result, "Code with predominantly 6502 instructions should be detected as 6502");
    }

    @Test
    void detectAssemblerType_ambiguousCode_defaultsToAsm() throws Exception {
        // Code with equal or nearly equal scores
        String code = """
            ; Generic assembly
            NOP
            NOP
            NOP
            """;

        String result = callDetectAssemblerType(code);
        assertEquals("asm", result, "Ambiguous code should default to asm");
    }

    @Test
    void detectAssemblerType_caseInsensitive_works() throws Exception {
        String codeLowercase = """
            lda #$20
            ldx #$00
            sta $0400,x
            rts
            """;

        String result = callDetectAssemblerType(codeLowercase);
        assertEquals("6502", result, "Detection should be case-insensitive");
    }

    @Test
    void detectAssemblerType_6502BranchInstructions_returns6502() throws Exception {
        String code = """
            check:
                BEQ equal
                BNE not_equal
                BCC carry_clear
                BCS carry_set
                BPL positive
                BMI negative
                BVC overflow_clear
                BVS overflow_set
            equal:
                RTS
            """;

        String result = callDetectAssemblerType(code);
        assertEquals("6502", result, "6502 branch instructions should be detected as 6502");
    }
}
