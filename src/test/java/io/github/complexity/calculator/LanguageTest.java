package io.github.complexity.calculator;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class LanguageTest {

    @Test
    void testFromKey() {
        assertEquals(Optional.of(Language.JAVA), Language.fromKey("java"));
        assertEquals(Optional.of(Language.X86_ASSEMBLER), Language.fromKey("asm"));
        assertEquals(Optional.of(Language.MOS6502_ASSEMBLER), Language.fromKey("6502"));
    }

    @Test
    void testFromKeyCaseInsensitive() {
        assertEquals(Optional.of(Language.JAVA), Language.fromKey("JAVA"));
        assertEquals(Optional.of(Language.JAVA), Language.fromKey("Java"));
        assertEquals(Optional.of(Language.X86_ASSEMBLER), Language.fromKey("ASM"));
    }

    @Test
    void testFromKeyInvalid() {
        assertEquals(Optional.empty(), Language.fromKey("python"));
        assertEquals(Optional.empty(), Language.fromKey("invalid"));
        assertEquals(Optional.empty(), Language.fromKey(null));
    }

    @Test
    void testFromFilePath() {
        assertEquals(Optional.of(Language.JAVA), Language.fromFilePath("Test.java"));
        assertEquals(Optional.of(Language.JAVA), Language.fromFilePath("/path/to/Test.java"));
        assertEquals(Optional.of(Language.X86_ASSEMBLER), Language.fromFilePath("file.asm"));
        assertEquals(Optional.of(Language.X86_ASSEMBLER), Language.fromFilePath("file.s"));
        assertEquals(Optional.of(Language.MOS6502_ASSEMBLER), Language.fromFilePath("code.a65"));
        assertEquals(Optional.of(Language.MOS6502_ASSEMBLER), Language.fromFilePath("code.s65"));
    }

    @Test
    void testFromFilePathCaseInsensitive() {
        assertEquals(Optional.of(Language.JAVA), Language.fromFilePath("Test.JAVA"));
        assertEquals(Optional.of(Language.X86_ASSEMBLER), Language.fromFilePath("FILE.ASM"));
    }

    @Test
    void testFromFilePathInvalid() {
        assertEquals(Optional.empty(), Language.fromFilePath("test.py"));
        assertEquals(Optional.empty(), Language.fromFilePath("test.txt"));
        assertEquals(Optional.empty(), Language.fromFilePath("noextension"));
    }

    @Test
    void testMatchesExtension() {
        assertTrue(Language.JAVA.matchesExtension("Test.java"));
        assertTrue(Language.X86_ASSEMBLER.matchesExtension("file.asm"));
        assertTrue(Language.X86_ASSEMBLER.matchesExtension("file.s"));
        assertTrue(Language.MOS6502_ASSEMBLER.matchesExtension("code.a65"));

        assertFalse(Language.JAVA.matchesExtension("test.py"));
        assertFalse(Language.X86_ASSEMBLER.matchesExtension("test.java"));
    }

    @Test
    void testGetAllKeys() {
        String[] keys = Language.getAllKeys();
        assertEquals(3, keys.length);
        assertTrue(java.util.Arrays.asList(keys).contains("java"));
        assertTrue(java.util.Arrays.asList(keys).contains("asm"));
        assertTrue(java.util.Arrays.asList(keys).contains("6502"));
    }

    @Test
    void testGetDisplayName() {
        assertEquals("Java", Language.JAVA.getDisplayName());
        assertEquals("Assembler", Language.X86_ASSEMBLER.getDisplayName());
        assertEquals("6502 Assembler", Language.MOS6502_ASSEMBLER.getDisplayName());
    }

    @Test
    void testToString() {
        assertEquals("Java", Language.JAVA.toString());
        assertEquals("Assembler", Language.X86_ASSEMBLER.toString());
    }

    @Test
    void testGetFileExtensions() {
        assertArrayEquals(new String[]{".java"}, Language.JAVA.getFileExtensions());

        String[] asmExts = Language.X86_ASSEMBLER.getFileExtensions();
        assertEquals(2, asmExts.length);
        assertTrue(java.util.Arrays.asList(asmExts).contains(".asm"));
        assertTrue(java.util.Arrays.asList(asmExts).contains(".s"));
    }
}
