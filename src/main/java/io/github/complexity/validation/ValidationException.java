package io.github.complexity.validation;

/**
 * Exception thrown when input validation fails
 */
public class ValidationException extends Exception {

    private final String fieldName;

    public ValidationException(String fieldName, String message) {
        super(String.format("Validation failed for '%s': %s", fieldName, message));
        this.fieldName = fieldName;
    }

    public ValidationException(String fieldName, String message, Throwable cause) {
        super(String.format("Validation failed for '%s': %s", fieldName, message), cause);
        this.fieldName = fieldName;
    }

    public String getFieldName() {
        return fieldName;
    }
}
