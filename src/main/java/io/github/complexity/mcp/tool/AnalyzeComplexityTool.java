package io.github.complexity.mcp.tool;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.github.complexity.calculator.*;
import io.github.complexity.exception.UnsupportedLanguageException;
import io.github.complexity.validation.InputValidator;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

/**
 * Tool for analyzing complexity from a file path
 */
public class AnalyzeComplexityTool implements McpTool {

    private final CalculatorFactory calculatorFactory;

    public AnalyzeComplexityTool(CalculatorFactory calculatorFactory) {
        this.calculatorFactory = calculatorFactory;
    }

    @Override
    public String getName() {
        return "analyze_complexity";
    }

    @Override
    public String getDescription() {
        return "Analyzes the cyclomatic complexity of source code from a file. " +
               "Supports Java, x86/x64 Assembler, and 6502 Assembler. Returns detailed complexity metrics " +
               "for each function/method including total complexity and most complex functions.";
    }

    @Override
    public JsonObject getInputSchema() {
        JsonObject schema = new JsonObject();
        schema.addProperty("type", "object");

        JsonObject properties = new JsonObject();

        // file_path property
        JsonObject filePathProp = new JsonObject();
        filePathProp.addProperty("type", "string");
        filePathProp.addProperty("description", "Path to the source code file to analyze");
        properties.add("file_path", filePathProp);

        // language property (optional)
        JsonObject langProp = new JsonObject();
        langProp.addProperty("type", "string");
        langProp.addProperty("description",
            "Language: 'java', 'asm', or '6502' (auto-detected from extension if not provided)");
        properties.add("language", langProp);

        schema.add("properties", properties);

        JsonArray required = new JsonArray();
        required.add("file_path");
        schema.add("required", required);

        return schema;
    }

    @Override
    public String execute(JsonObject arguments) throws Exception {
        // Validate and get file path
        String filePathString = InputValidator.requireNonEmpty(
            arguments.get("file_path").getAsString(),
            "file_path"
        );
        Path filePath = InputValidator.validateFilePath(filePathString, "file_path");

        // Determine language
        Language language = determineLanguage(arguments, filePathString);

        // Get calculator and analyze
        ComplexityCalculator calculator = calculatorFactory.createCalculator(language);
        String sourceCode = Files.readString(filePath);
        ComplexityResult result = calculator.calculate(sourceCode, filePathString);

        return result.getSummary();
    }

    private Language determineLanguage(JsonObject arguments, String filePath) throws UnsupportedLanguageException {
        // Check if language was explicitly provided
        if (arguments.has("language") && !arguments.get("language").isJsonNull()) {
            String languageKey = arguments.get("language").getAsString().toLowerCase();
            Optional<Language> lang = Language.fromKey(languageKey);
            if (lang.isEmpty()) {
                throw new UnsupportedLanguageException(
                    languageKey,
                    java.util.Set.of(Language.getAllKeys())
                );
            }
            return lang.get();
        }

        // Auto-detect from file path
        Optional<Language> detected = Language.fromFilePath(filePath);
        if (detected.isEmpty()) {
            throw new UnsupportedLanguageException(
                "file with unknown extension",
                java.util.Set.of(Language.getAllKeys())
            );
        }

        return detected.get();
    }
}
