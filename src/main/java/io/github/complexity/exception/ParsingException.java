package io.github.complexity.exception;

/**
 * Exception thrown when source code parsing fails
 */
public class ParsingException extends ComplexityException {

    private final String language;
    private final String fileName;

    public ParsingException(String language, String fileName, String message) {
        super(String.format("Failed to parse %s code in '%s': %s", language, fileName, message));
        this.language = language;
        this.fileName = fileName;
    }

    public ParsingException(String language, String fileName, String message, Throwable cause) {
        super(String.format("Failed to parse %s code in '%s': %s", language, fileName, message), cause);
        this.language = language;
        this.fileName = fileName;
    }

    public String getLanguage() {
        return language;
    }

    public String getFileName() {
        return fileName;
    }
}
