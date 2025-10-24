package io.github.complexity.validation;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Utility class for input validation
 * Provides validation methods for common input types
 */
public class InputValidator {

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10 MB
    private static final int MAX_SOURCE_CODE_LENGTH = 1024 * 1024; // 1 MB for inline code

    /**
     * Validate a file path
     *
     * @param filePath The file path to validate
     * @param fieldName The name of the field (for error messages)
     * @throws ValidationException if validation fails
     */
    public static Path validateFilePath(String filePath, String fieldName) throws ValidationException {
        // Check null or empty
        if (filePath == null || filePath.trim().isEmpty()) {
            throw new ValidationException(fieldName, "File path cannot be null or empty");
        }

        // Try to parse as Path
        Path path;
        try {
            path = Paths.get(filePath);
        } catch (InvalidPathException e) {
            throw new ValidationException(fieldName, "Invalid file path format: " + e.getMessage(), e);
        }

        // Check if file exists
        if (!Files.exists(path)) {
            throw new ValidationException(fieldName, "File does not exist: " + filePath);
        }

        // Check if it's a regular file (not a directory)
        if (!Files.isRegularFile(path)) {
            throw new ValidationException(fieldName, "Path is not a regular file: " + filePath);
        }

        // Check if file is readable
        if (!Files.isReadable(path)) {
            throw new ValidationException(fieldName, "File is not readable: " + filePath);
        }

        // Check file size
        try {
            long size = Files.size(path);
            if (size > MAX_FILE_SIZE) {
                throw new ValidationException(fieldName,
                    String.format("File size (%d bytes) exceeds maximum allowed size (%d bytes)",
                        size, MAX_FILE_SIZE));
            }
            if (size == 0) {
                throw new ValidationException(fieldName, "File is empty: " + filePath);
            }
        } catch (IOException e) {
            throw new ValidationException(fieldName, "Cannot read file size: " + e.getMessage(), e);
        }

        return path;
    }

    /**
     * Validate source code string
     *
     * @param sourceCode The source code to validate
     * @param fieldName The name of the field (for error messages)
     * @throws ValidationException if validation fails
     */
    public static void validateSourceCode(String sourceCode, String fieldName) throws ValidationException {
        if (sourceCode == null) {
            throw new ValidationException(fieldName, "Source code cannot be null");
        }

        if (sourceCode.trim().isEmpty()) {
            throw new ValidationException(fieldName, "Source code cannot be empty");
        }

        if (sourceCode.length() > MAX_SOURCE_CODE_LENGTH) {
            throw new ValidationException(fieldName,
                String.format("Source code length (%d characters) exceeds maximum allowed (%d characters)",
                    sourceCode.length(), MAX_SOURCE_CODE_LENGTH));
        }
    }

    /**
     * Validate a string parameter
     *
     * @param value The string to validate
     * @param fieldName The name of the field (for error messages)
     * @param required Whether the field is required
     * @throws ValidationException if validation fails
     */
    public static void validateString(String value, String fieldName, boolean required) throws ValidationException {
        if (value == null || value.trim().isEmpty()) {
            if (required) {
                throw new ValidationException(fieldName, "Field is required but was null or empty");
            }
        }
    }

    /**
     * Validate that a string is not null or empty
     *
     * @param value The string to validate
     * @param fieldName The name of the field (for error messages)
     * @return The trimmed string
     * @throws ValidationException if validation fails
     */
    public static String requireNonEmpty(String value, String fieldName) throws ValidationException {
        if (value == null || value.trim().isEmpty()) {
            throw new ValidationException(fieldName, "Field is required but was null or empty");
        }
        return value.trim();
    }

    /**
     * Get the maximum allowed file size
     */
    public static long getMaxFileSize() {
        return MAX_FILE_SIZE;
    }

    /**
     * Get the maximum allowed source code length
     */
    public static int getMaxSourceCodeLength() {
        return MAX_SOURCE_CODE_LENGTH;
    }
}
