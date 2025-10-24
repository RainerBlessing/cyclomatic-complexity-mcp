package io.github.complexity.mcp;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.github.complexity.calculator.ComplexityCalculator;
import io.github.complexity.calculator.ComplexityResult;
import io.github.complexity.exception.ComplexityException;
import io.github.complexity.exception.UnsupportedLanguageException;

import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Registry for MCP tools
 * Manages tool definitions and execution
 */
public class ToolRegistry {

    private final CalculatorRegistry calculatorRegistry;
    private final LanguageDetector languageDetector;

    public ToolRegistry(CalculatorRegistry calculatorRegistry, LanguageDetector languageDetector) {
        this.calculatorRegistry = calculatorRegistry;
        this.languageDetector = languageDetector;
    }

    /**
     * Get list of available tools with their schemas
     */
    public JsonObject getToolsList(JsonElement id) {
        JsonObject response = new JsonObject();
        response.add("jsonrpc", new com.google.gson.JsonPrimitive("2.0"));
        response.add("id", id);

        JsonArray tools = new JsonArray();
        tools.add(createAnalyzeComplexityTool());
        tools.add(createAnalyzeComplexityCodeTool());

        JsonObject result = new JsonObject();
        result.add("tools", tools);

        response.add("result", result);
        return response;
    }

    /**
     * Execute a tool by name
     */
    public String executeTool(String toolName, JsonObject arguments) throws Exception {
        switch (toolName) {
            case "analyze_complexity":
                return executeAnalyzeFile(arguments);
            case "analyze_complexity_code":
                return executeAnalyzeCode(arguments);
            default:
                throw new IllegalArgumentException("Unknown tool: " + toolName);
        }
    }

    private String executeAnalyzeFile(JsonObject arguments) throws Exception {
        String filePath = arguments.get("file_path").getAsString();
        String language = null;

        if (arguments.has("language") && !arguments.get("language").isJsonNull()) {
            language = arguments.get("language").getAsString().toLowerCase();
        }

        // Auto-detect language from extension if not provided
        if (language == null || language.isEmpty()) {
            language = languageDetector.detectLanguage(filePath);
        }

        ComplexityCalculator calculator = calculatorRegistry.getCalculator(language);
        String sourceCode = new String(Files.readAllBytes(Paths.get(filePath)));
        ComplexityResult result = calculator.calculate(sourceCode, filePath);

        return result.getSummary();
    }

    private String executeAnalyzeCode(JsonObject arguments) throws ComplexityException {
        String sourceCode = arguments.get("source_code").getAsString();
        String language = arguments.get("language").getAsString().toLowerCase();
        String fileName = arguments.has("file_name") ?
            arguments.get("file_name").getAsString() : "inline_code";

        ComplexityCalculator calculator = calculatorRegistry.getCalculator(language);
        ComplexityResult result = calculator.calculate(sourceCode, fileName);
        return result.getSummary();
    }

    private JsonObject createAnalyzeComplexityTool() {
        JsonObject tool = new JsonObject();
        tool.addProperty("name", "analyze_complexity");
        tool.addProperty("description",
            "Analyzes the cyclomatic complexity of source code from a file. " +
            "Supports Java, x86/x64 Assembler, and 6502 Assembler. Returns detailed complexity metrics " +
            "for each function/method including total complexity and most complex functions.");

        JsonObject schema = new JsonObject();
        schema.addProperty("type", "object");

        JsonObject properties = new JsonObject();

        JsonObject filePathProp = new JsonObject();
        filePathProp.addProperty("type", "string");
        filePathProp.addProperty("description", "Path to the source code file to analyze");
        properties.add("file_path", filePathProp);

        JsonObject langProp = new JsonObject();
        langProp.addProperty("type", "string");
        langProp.addProperty("description", "Language: 'java', 'asm', or '6502' (auto-detected from extension if not provided)");
        properties.add("language", langProp);

        schema.add("properties", properties);

        JsonArray required = new JsonArray();
        required.add("file_path");
        schema.add("required", required);

        tool.add("inputSchema", schema);
        return tool;
    }

    private JsonObject createAnalyzeComplexityCodeTool() {
        JsonObject tool = new JsonObject();
        tool.addProperty("name", "analyze_complexity_code");
        tool.addProperty("description",
            "Analyzes the cyclomatic complexity of source code provided as a string. " +
            "Supports Java, x86/x64 Assembler, and 6502 Assembler. Returns detailed complexity metrics " +
            "for each function/method.");

        JsonObject schema = new JsonObject();
        schema.addProperty("type", "object");

        JsonObject properties = new JsonObject();

        JsonObject sourceCodeProp = new JsonObject();
        sourceCodeProp.addProperty("type", "string");
        sourceCodeProp.addProperty("description", "Source code to analyze");
        properties.add("source_code", sourceCodeProp);

        JsonObject codeLangProp = new JsonObject();
        codeLangProp.addProperty("type", "string");
        codeLangProp.addProperty("description", "Language: 'java', 'asm', or '6502'");
        properties.add("language", codeLangProp);

        JsonObject fileNameProp = new JsonObject();
        fileNameProp.addProperty("type", "string");
        fileNameProp.addProperty("description", "File name for reporting (optional)");
        properties.add("file_name", fileNameProp);

        schema.add("properties", properties);

        JsonArray codeRequired = new JsonArray();
        codeRequired.add("source_code");
        codeRequired.add("language");
        schema.add("required", codeRequired);

        tool.add("inputSchema", schema);
        return tool;
    }
}
