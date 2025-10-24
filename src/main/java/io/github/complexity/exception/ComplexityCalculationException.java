package io.github.complexity.exception;

/**
 * Exception thrown when complexity calculation fails
 */
public class ComplexityCalculationException extends ComplexityException {

    private final String fileName;

    public ComplexityCalculationException(String fileName, String message) {
        super(String.format("Failed to calculate complexity for '%s': %s", fileName, message));
        this.fileName = fileName;
    }

    public ComplexityCalculationException(String fileName, String message, Throwable cause) {
        super(String.format("Failed to calculate complexity for '%s': %s", fileName, message), cause);
        this.fileName = fileName;
    }

    public String getFileName() {
        return fileName;
    }
}
