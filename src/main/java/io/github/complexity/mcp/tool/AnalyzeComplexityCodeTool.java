package io.github.complexity.mcp.tool;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.github.complexity.calculator.*;
import io.github.complexity.exception.UnsupportedLanguageException;
import io.github.complexity.validation.InputValidator;

import java.util.Optional;

/**
 * Tool for analyzing complexity from inline source code
 */
public class AnalyzeComplexityCodeTool implements McpTool {

    private final CalculatorFactory calculatorFactory;

    public AnalyzeComplexityCodeTool(CalculatorFactory calculatorFactory) {
        this.calculatorFactory = calculatorFactory;
    }

    @Override
    public String getName() {
        return "analyze_complexity_code";
    }

    @Override
    public String getDescription() {
        return "Analyzes the cyclomatic complexity of source code provided as a string. " +
               "Supports Java, x86/x64 Assembler, and 6502 Assembler. Returns detailed complexity metrics " +
               "for each function/method.";
    }

    @Override
    public JsonObject getInputSchema() {
        JsonObject schema = new JsonObject();
        schema.addProperty("type", "object");

        JsonObject properties = new JsonObject();

        // source_code property
        JsonObject sourceCodeProp = new JsonObject();
        sourceCodeProp.addProperty("type", "string");
        sourceCodeProp.addProperty("description", "Source code to analyze");
        properties.add("source_code", sourceCodeProp);

        // language property (required)
        JsonObject langProp = new JsonObject();
        langProp.addProperty("type", "string");
        langProp.addProperty("description", "Language: 'java', 'asm', or '6502'");
        properties.add("language", langProp);

        // file_name property (optional)
        JsonObject fileNameProp = new JsonObject();
        fileNameProp.addProperty("type", "string");
        fileNameProp.addProperty("description", "File name for reporting (optional)");
        properties.add("file_name", fileNameProp);

        schema.add("properties", properties);

        JsonArray required = new JsonArray();
        required.add("source_code");
        required.add("language");
        schema.add("required", required);

        return schema;
    }

    @Override
    public String execute(JsonObject arguments) throws Exception {
        // Validate source code
        String sourceCode = InputValidator.requireNonEmpty(
            arguments.get("source_code").getAsString(),
            "source_code"
        );
        InputValidator.validateSourceCode(sourceCode, "source_code");

        // Validate and parse language
        String languageKey = InputValidator.requireNonEmpty(
            arguments.get("language").getAsString(),
            "language"
        );
        Language language = parseLanguage(languageKey);

        // Get file name (optional)
        String fileName = "inline_code";
        if (arguments.has("file_name") && !arguments.get("file_name").isJsonNull()) {
            fileName = arguments.get("file_name").getAsString().trim();
            if (fileName.isEmpty()) {
                fileName = "inline_code";
            }
        }

        // Get calculator and analyze
        ComplexityCalculator calculator = calculatorFactory.createCalculator(language);
        ComplexityResult result = calculator.calculate(sourceCode, fileName);

        return result.getSummary();
    }

    private Language parseLanguage(String languageKey) throws UnsupportedLanguageException {
        Optional<Language> lang = Language.fromKey(languageKey);
        if (lang.isEmpty()) {
            throw new UnsupportedLanguageException(
                languageKey,
                java.util.Set.of(Language.getAllKeys())
            );
        }
        return lang.get();
    }
}
