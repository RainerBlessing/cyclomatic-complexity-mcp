package io.github.complexity.calculator;

import java.util.Arrays;
import java.util.Optional;

/**
 * Enumeration of supported programming languages
 * Provides type-safe language identification and file extension mapping
 */
public enum Language {
    JAVA("java", "Java", ".java"),
    X86_ASSEMBLER("asm", "Assembler", ".asm", ".s"),
    MOS6502_ASSEMBLER("6502", "6502 Assembler", ".a65", ".s65", ".asm65", ".a");

    private final String key;
    private final String displayName;
    private final String[] fileExtensions;

    Language(String key, String displayName, String... fileExtensions) {
        this.key = key;
        this.displayName = displayName;
        this.fileExtensions = fileExtensions;
    }

    /**
     * Get the language key used for lookups
     */
    public String getKey() {
        return key;
    }

    /**
     * Get the display name for this language
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Get all file extensions for this language
     */
    public String[] getFileExtensions() {
        return fileExtensions.clone();
    }

    /**
     * Check if a file path has an extension matching this language
     */
    public boolean matchesExtension(String filePath) {
        String lowerPath = filePath.toLowerCase();
        return Arrays.stream(fileExtensions)
            .anyMatch(lowerPath::endsWith);
    }

    /**
     * Detect language from file path
     *
     * @param filePath The file path to analyze
     * @return Optional containing the detected language, or empty if no match
     */
    public static Optional<Language> fromFilePath(String filePath) {
        return Arrays.stream(values())
            .filter(lang -> lang.matchesExtension(filePath))
            .findFirst();
    }

    /**
     * Get language by key (case-insensitive)
     *
     * @param key The language key
     * @return Optional containing the language, or empty if not found
     */
    public static Optional<Language> fromKey(String key) {
        if (key == null) {
            return Optional.empty();
        }
        String lowerKey = key.toLowerCase();
        return Arrays.stream(values())
            .filter(lang -> lang.key.equals(lowerKey))
            .findFirst();
    }

    /**
     * Get all supported language keys
     */
    public static String[] getAllKeys() {
        return Arrays.stream(values())
            .map(Language::getKey)
            .toArray(String[]::new);
    }

    @Override
    public String toString() {
        return displayName;
    }
}
