package io.github.complexity.mcp;

import com.google.gson.*;
import io.github.complexity.calculator.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

/**
 * MCP Server for cyclomatic complexity analysis
 * Implements the Model Context Protocol for Claude integration
 */
public class McpServer {
    private static final Logger logger = LoggerFactory.getLogger(McpServer.class);
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    private final Map<String, ComplexityCalculator> calculators = new HashMap<>();

    public McpServer() {
        calculators.put("java", new JavaComplexityCalculator());
        calculators.put("asm", new AssemblerComplexityCalculator());
        calculators.put("s", new AssemblerComplexityCalculator());
        calculators.put("6502", new Mos6502ComplexityCalculator());
    }

    public static void main(String[] args) {
        McpServer server = new McpServer();
        server.run();
    }

    public void run() {
        logger.info("Starting Cyclomatic Complexity MCP Server");

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
             PrintWriter writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(System.out)), true)) {

            String line;
            while ((line = reader.readLine()) != null) {
                try {
                    JsonObject request = JsonParser.parseString(line).getAsJsonObject();
                    JsonObject response = handleRequest(request);

                    if (response != null) {
                        writer.println(gson.toJson(response));
                        writer.flush();
                    }
                } catch (Exception e) {
                    logger.error("Error processing request", e);
                    JsonObject error = createErrorResponse(null, -32603, "Internal error: " + e.getMessage());
                    writer.println(gson.toJson(error));
                    writer.flush();
                }
            }
        } catch (IOException e) {
            logger.error("IO Error in main loop", e);
        }

        logger.info("Server shutting down");
    }

    private JsonObject handleRequest(JsonObject request) {
        String method = request.has("method") ? request.get("method").getAsString() : "";
        JsonElement idElement = request.get("id");

        logger.debug("Received request: method={}", method);

        switch (method) {
            case "initialize":
                return handleInitialize(idElement);
            case "tools/list":
                return handleToolsList(idElement);
            case "tools/call":
                return handleToolsCall(request, idElement);
            default:
                return createErrorResponse(idElement, -32601, "Method not found: " + method);
        }
    }

    private JsonObject handleInitialize(JsonElement id) {
        JsonObject response = new JsonObject();
        response.add("jsonrpc", new JsonPrimitive("2.0"));
        response.add("id", id);

        JsonObject result = new JsonObject();
        result.addProperty("protocolVersion", "2024-11-05");
        result.addProperty("serverInfo", "Cyclomatic Complexity MCP Server v1.0.0");

        JsonObject capabilities = new JsonObject();
        capabilities.addProperty("tools", true);

        result.add("capabilities", capabilities);
        response.add("result", result);

        logger.info("Initialized MCP server");
        return response;
    }

    private JsonObject handleToolsList(JsonElement id) {
        JsonObject response = new JsonObject();
        response.add("jsonrpc", new JsonPrimitive("2.0"));
        response.add("id", id);

        JsonArray tools = new JsonArray();

        // Tool 1: analyze_file - Analyze a file by path
        JsonObject analyzeTool = new JsonObject();
        analyzeTool.addProperty("name", "analyze_complexity");
        analyzeTool.addProperty("description",
            "Analyzes the cyclomatic complexity of source code from a file. " +
            "Supports Java, x86/x64 Assembler, and 6502 Assembler. Returns detailed complexity metrics " +
            "for each function/method including total complexity and most complex functions.");

        JsonObject analyzeSchema = new JsonObject();
        analyzeSchema.addProperty("type", "object");

        JsonObject analyzeProps = new JsonObject();

        JsonObject filePathProp = new JsonObject();
        filePathProp.addProperty("type", "string");
        filePathProp.addProperty("description", "Path to the source code file to analyze");
        analyzeProps.add("file_path", filePathProp);

        JsonObject langProp = new JsonObject();
        langProp.addProperty("type", "string");
        langProp.addProperty("description", "Language: 'java', 'asm', or '6502' (auto-detected from extension if not provided)");
        analyzeProps.add("language", langProp);

        analyzeSchema.add("properties", analyzeProps);

        JsonArray required = new JsonArray();
        required.add("file_path");
        analyzeSchema.add("required", required);

        analyzeTool.add("inputSchema", analyzeSchema);
        tools.add(analyzeTool);

        // Tool 2: analyze_code - Analyze code directly
        JsonObject analyzeCodeTool = new JsonObject();
        analyzeCodeTool.addProperty("name", "analyze_complexity_code");
        analyzeCodeTool.addProperty("description",
            "Analyzes the cyclomatic complexity of source code provided as a string. " +
            "Supports Java, x86/x64 Assembler, and 6502 Assembler. Returns detailed complexity metrics " +
            "for each function/method.");

        JsonObject codeSchema = new JsonObject();
        codeSchema.addProperty("type", "object");

        JsonObject codeProps = new JsonObject();

        JsonObject sourceCodeProp = new JsonObject();
        sourceCodeProp.addProperty("type", "string");
        sourceCodeProp.addProperty("description", "Source code to analyze");
        codeProps.add("source_code", sourceCodeProp);

        JsonObject codeLangProp = new JsonObject();
        codeLangProp.addProperty("type", "string");
        codeLangProp.addProperty("description", "Language: 'java', 'asm', or '6502'");
        codeProps.add("language", codeLangProp);

        JsonObject fileNameProp = new JsonObject();
        fileNameProp.addProperty("type", "string");
        fileNameProp.addProperty("description", "File name for reporting (optional)");
        codeProps.add("file_name", fileNameProp);

        codeSchema.add("properties", codeProps);

        JsonArray codeRequired = new JsonArray();
        codeRequired.add("source_code");
        codeRequired.add("language");
        codeSchema.add("required", codeRequired);

        analyzeCodeTool.add("inputSchema", codeSchema);
        tools.add(analyzeCodeTool);

        JsonObject result = new JsonObject();
        result.add("tools", tools);

        response.add("result", result);
        return response;
    }

    private JsonObject handleToolsCall(JsonObject request, JsonElement id) {
        JsonObject params = request.getAsJsonObject("params");
        String toolName = params.get("name").getAsString();
        JsonObject arguments = params.getAsJsonObject("arguments");

        logger.debug("Tool call: {}", toolName);

        try {
            String resultText;
            if ("analyze_complexity".equals(toolName)) {
                resultText = handleAnalyzeFile(arguments);
            } else if ("analyze_complexity_code".equals(toolName)) {
                resultText = handleAnalyzeCode(arguments);
            } else {
                return createErrorResponse(id, -32602, "Unknown tool: " + toolName);
            }

            JsonObject response = new JsonObject();
            response.add("jsonrpc", new JsonPrimitive("2.0"));
            response.add("id", id);

            JsonObject result = new JsonObject();
            JsonArray content = new JsonArray();

            JsonObject textContent = new JsonObject();
            textContent.addProperty("type", "text");
            textContent.addProperty("text", resultText);

            content.add(textContent);
            result.add("content", content);

            response.add("result", result);
            return response;

        } catch (Exception e) {
            logger.error("Error executing tool", e);
            return createErrorResponse(id, -32603, "Tool execution error: " + e.getMessage());
        }
    }

    private String handleAnalyzeFile(JsonObject arguments) throws IOException {
        String filePath = arguments.get("file_path").getAsString();
        String language = null;

        if (arguments.has("language")) {
            language = arguments.get("language").getAsString().toLowerCase();
        }

        // Auto-detect language from extension if not provided
        if (language == null || language.isEmpty()) {
            language = detectLanguage(filePath);
        }

        ComplexityCalculator calculator = calculators.get(language);
        if (calculator == null) {
            throw new IllegalArgumentException("Unsupported language: " + language +
                ". Supported languages: " + calculators.keySet());
        }

        String sourceCode = new String(Files.readAllBytes(Paths.get(filePath)));
        ComplexityResult result = calculator.calculate(sourceCode, filePath);

        return result.getSummary();
    }

    private String handleAnalyzeCode(JsonObject arguments) throws IOException {
        String sourceCode = arguments.get("source_code").getAsString();
        String language = arguments.get("language").getAsString().toLowerCase();
        String fileName = arguments.has("file_name") ?
            arguments.get("file_name").getAsString() : "inline_code";

        ComplexityCalculator calculator = calculators.get(language);
        if (calculator == null) {
            throw new IllegalArgumentException("Unsupported language: " + language +
                ". Supported languages: " + calculators.keySet());
        }

        ComplexityResult result = calculator.calculate(sourceCode, fileName);
        return result.getSummary();
    }

    private String detectLanguage(String filePath) {
        String lower = filePath.toLowerCase();
        if (lower.endsWith(".java")) {
            return "java";
        } else if (lower.endsWith(".asm") || lower.endsWith(".s")) {
            return "asm";
        } else if (lower.endsWith(".a65") || lower.endsWith(".s65") ||
                   lower.endsWith(".asm65") || lower.endsWith(".a")) {
            return "6502";
        }
        throw new IllegalArgumentException("Cannot detect language from file extension: " + filePath);
    }

    private JsonObject createErrorResponse(JsonElement id, int code, String message) {
        JsonObject response = new JsonObject();
        response.add("jsonrpc", new JsonPrimitive("2.0"));
        response.add("id", id);

        JsonObject error = new JsonObject();
        error.addProperty("code", code);
        error.addProperty("message", message);

        response.add("error", error);
        return response;
    }
}
