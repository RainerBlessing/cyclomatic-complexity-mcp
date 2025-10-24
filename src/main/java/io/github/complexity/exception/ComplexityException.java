package io.github.complexity.exception;

/**
 * Base exception for all complexity calculation related errors
 */
public class ComplexityException extends Exception {

    public ComplexityException(String message) {
        super(message);
    }

    public ComplexityException(String message, Throwable cause) {
        super(message, cause);
    }
}
