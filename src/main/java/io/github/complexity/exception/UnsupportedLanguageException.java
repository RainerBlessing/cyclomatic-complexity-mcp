package io.github.complexity.exception;

import java.util.Set;

/**
 * Exception thrown when an unsupported language is requested
 */
public class UnsupportedLanguageException extends ComplexityException {

    private final String requestedLanguage;
    private final Set<String> supportedLanguages;

    public UnsupportedLanguageException(String requestedLanguage, Set<String> supportedLanguages) {
        super(String.format("Unsupported language: '%s'. Supported languages: %s",
            requestedLanguage, supportedLanguages));
        this.requestedLanguage = requestedLanguage;
        this.supportedLanguages = supportedLanguages;
    }

    public String getRequestedLanguage() {
        return requestedLanguage;
    }

    public Set<String> getSupportedLanguages() {
        return supportedLanguages;
    }
}
