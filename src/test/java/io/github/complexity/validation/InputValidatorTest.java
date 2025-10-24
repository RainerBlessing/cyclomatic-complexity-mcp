package io.github.complexity.validation;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class InputValidatorTest {

    @TempDir
    Path tempDir;

    @Test
    void testValidateFilePathSuccess() throws Exception {
        // Create a valid test file
        Path testFile = tempDir.resolve("test.java");
        Files.writeString(testFile, "public class Test {}");

        Path result = InputValidator.validateFilePath(testFile.toString(), "testFile");
        assertEquals(testFile, result);
    }

    @Test
    void testValidateFilePathNullOrEmpty() {
        assertThrows(ValidationException.class, () ->
            InputValidator.validateFilePath(null, "test"));

        assertThrows(ValidationException.class, () ->
            InputValidator.validateFilePath("", "test"));

        assertThrows(ValidationException.class, () ->
            InputValidator.validateFilePath("   ", "test"));
    }

    @Test
    void testValidateFilePathNotExists() {
        assertThrows(ValidationException.class, () ->
            InputValidator.validateFilePath("/nonexistent/file.java", "test"));
    }

    @Test
    void testValidateFilePathDirectory() throws IOException {
        Path dir = tempDir.resolve("testdir");
        Files.createDirectory(dir);

        assertThrows(ValidationException.class, () ->
            InputValidator.validateFilePath(dir.toString(), "test"));
    }

    @Test
    void testValidateFilePathEmptyFile() throws IOException {
        Path emptyFile = tempDir.resolve("empty.java");
        Files.createFile(emptyFile);

        assertThrows(ValidationException.class, () ->
            InputValidator.validateFilePath(emptyFile.toString(), "test"));
    }

    @Test
    void testValidateFilePathTooLarge() throws IOException {
        Path largeFile = tempDir.resolve("large.java");
        // Create a file larger than MAX_FILE_SIZE (10 MB)
        byte[] data = new byte[11 * 1024 * 1024]; // 11 MB
        Files.write(largeFile, data);

        assertThrows(ValidationException.class, () ->
            InputValidator.validateFilePath(largeFile.toString(), "test"));
    }

    @Test
    void testValidateSourceCodeSuccess() throws ValidationException {
        InputValidator.validateSourceCode("public class Test {}", "code");
        // Should not throw
    }

    @Test
    void testValidateSourceCodeNull() {
        assertThrows(ValidationException.class, () ->
            InputValidator.validateSourceCode(null, "code"));
    }

    @Test
    void testValidateSourceCodeEmpty() {
        assertThrows(ValidationException.class, () ->
            InputValidator.validateSourceCode("", "code"));

        assertThrows(ValidationException.class, () ->
            InputValidator.validateSourceCode("   ", "code"));
    }

    @Test
    void testValidateSourceCodeTooLong() {
        // Create a string longer than MAX_SOURCE_CODE_LENGTH (1 MB)
        String longCode = "x".repeat(2 * 1024 * 1024); // 2 MB

        assertThrows(ValidationException.class, () ->
            InputValidator.validateSourceCode(longCode, "code"));
    }

    @Test
    void testRequireNonEmptySuccess() throws ValidationException {
        assertEquals("test", InputValidator.requireNonEmpty("test", "field"));
        assertEquals("test", InputValidator.requireNonEmpty("  test  ", "field"));
    }

    @Test
    void testRequireNonEmptyFails() {
        assertThrows(ValidationException.class, () ->
            InputValidator.requireNonEmpty(null, "field"));

        assertThrows(ValidationException.class, () ->
            InputValidator.requireNonEmpty("", "field"));

        assertThrows(ValidationException.class, () ->
            InputValidator.requireNonEmpty("   ", "field"));
    }

    @Test
    void testValidateStringOptional() throws ValidationException {
        // Should not throw for optional field
        InputValidator.validateString(null, "field", false);
        InputValidator.validateString("", "field", false);
    }

    @Test
    void testValidateStringRequired() {
        // Should throw for required field
        assertThrows(ValidationException.class, () ->
            InputValidator.validateString(null, "field", true));

        assertThrows(ValidationException.class, () ->
            InputValidator.validateString("", "field", true));
    }
}
