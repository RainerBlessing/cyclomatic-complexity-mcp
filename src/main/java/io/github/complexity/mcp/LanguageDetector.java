package io.github.complexity.mcp;

import io.github.complexity.calculator.Language;
import io.github.complexity.exception.UnsupportedLanguageException;

import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Detects programming language from file extensions
 */
public class LanguageDetector {

    /**
     * Detect language from file path
     *
     * @param filePath The file path to analyze
     * @return The detected language
     * @throws UnsupportedLanguageException if language cannot be detected
     */
    public Language detectLanguage(String filePath) throws UnsupportedLanguageException {
        Optional<Language> detected = Language.fromFilePath(filePath);

        if (detected.isEmpty()) {
            // Extract extension for error message
            int lastDot = filePath.lastIndexOf('.');
            String extension = lastDot > 0 ? filePath.substring(lastDot) : "<no extension>";

            throw new UnsupportedLanguageException(
                "file with extension '" + extension + "'",
                getSupportedLanguageKeys()
            );
        }

        return detected.get();
    }

    /**
     * Get all supported language keys
     */
    public Set<String> getSupportedLanguageKeys() {
        return Arrays.stream(Language.values())
            .map(Language::getKey)
            .collect(Collectors.toSet());
    }
}
