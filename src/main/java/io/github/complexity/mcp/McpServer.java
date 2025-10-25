package io.github.complexity.mcp;

import com.google.gson.*;
import io.github.complexity.calculator.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Pattern;

/**
 * MCP Server for cyclomatic complexity analysis
 * Implements the Model Context Protocol for Claude integration
 */
public class McpServer {
    private static final Logger logger = LoggerFactory.getLogger(McpServer.class);
    private static final Gson gson = new Gson(); // No pretty printing - JSON-RPC requires single-line responses

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
            case "notifications/initialized":
                // This is a notification, no response needed
                logger.debug("Received initialized notification");
                return null;
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
        result.addProperty("protocolVersion", "2025-06-18"); // Updated to match Claude Code

        JsonObject serverInfo = new JsonObject();
        serverInfo.addProperty("name", "cyclomatic-complexity-mcp");
        serverInfo.addProperty("version", "1.0.0");
        result.add("serverInfo", serverInfo);

        JsonObject capabilities = new JsonObject();
        JsonObject toolsCapability = new JsonObject();
        // tools capability can be empty object {} or contain listChanged: true
        capabilities.add("tools", toolsCapability);

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

    private String detectLanguage(String filePath) throws IOException {
        String lower = filePath.toLowerCase();
        if (lower.endsWith(".java")) {
            return "java";
        } else if (lower.endsWith(".asm") || lower.endsWith(".s")) {
            // Ambiguous extension - need to analyze content
            String sourceCode = new String(Files.readAllBytes(Paths.get(filePath)));
            return detectAssemblerType(sourceCode);
        } else if (lower.endsWith(".a65") || lower.endsWith(".s65") ||
                   lower.endsWith(".asm65") || lower.endsWith(".a")) {
            return "6502";
        }
        throw new IllegalArgumentException("Cannot detect language from file extension: " + filePath);
    }

    // Precompiled regex patterns for assembler detection (performance optimization)
    private static final int DIRECTIVE_WEIGHT = 2;

    // 6502-specific instructions (that don't exist in x86)
    private static final Pattern[] PATTERNS_6502_INSTR = {
        Pattern.compile("\\bLDA\\b"), Pattern.compile("\\bLDX\\b"), Pattern.compile("\\bLDY\\b"),
        Pattern.compile("\\bSTA\\b"), Pattern.compile("\\bSTX\\b"), Pattern.compile("\\bSTY\\b"),
        Pattern.compile("\\bINX\\b"), Pattern.compile("\\bINY\\b"), Pattern.compile("\\bDEX\\b"),
        Pattern.compile("\\bDEY\\b"), Pattern.compile("\\bBEQ\\b"), Pattern.compile("\\bBNE\\b"),
        Pattern.compile("\\bBCC\\b"), Pattern.compile("\\bBCS\\b"), Pattern.compile("\\bBPL\\b"),
        Pattern.compile("\\bBMI\\b"), Pattern.compile("\\bBVC\\b"), Pattern.compile("\\bBVS\\b"),
        Pattern.compile("\\bPHA\\b"), Pattern.compile("\\bPLA\\b"), Pattern.compile("\\bPHP\\b"),
        Pattern.compile("\\bPLP\\b"), Pattern.compile("\\bSEC\\b"), Pattern.compile("\\bCLC\\b"),
        Pattern.compile("\\bSED\\b"), Pattern.compile("\\bCLD\\b"), Pattern.compile("\\bSEI\\b"),
        Pattern.compile("\\bCLI\\b"), Pattern.compile("\\bCLV\\b"), Pattern.compile("\\bADC\\b"),
        Pattern.compile("\\bSBC\\b"), Pattern.compile("\\bCMP\\b"), Pattern.compile("\\bCPX\\b"),
        Pattern.compile("\\bCPY\\b"), Pattern.compile("\\bRTS\\b"), Pattern.compile("\\bRTI\\b"),
        Pattern.compile("\\bJSR\\b")
    };

    // 6502-specific directives
    private static final Pattern[] PATTERNS_6502_DIRECTIVES = {
        Pattern.compile("\\bPROCESSOR\\s+6502\\b"), Pattern.compile("\\.PROC\\b"),
        Pattern.compile("\\.ENDPROC\\b"), Pattern.compile("\\bSUBROUTINE\\b"),
        Pattern.compile("\\b!ZONE\\b"), Pattern.compile("\\b!ADDR\\b")
    };

    // x86-specific instructions (that don't exist in 6502)
    private static final Pattern[] PATTERNS_X86_INSTR = {
        Pattern.compile("\\bMOV\\b"), Pattern.compile("\\bPUSH\\b"), Pattern.compile("\\bPOP\\b"),
        Pattern.compile("\\bCALL\\b"), Pattern.compile("\\bRET\\b"), Pattern.compile("\\bADD\\b"),
        Pattern.compile("\\bSUB\\b"), Pattern.compile("\\bXOR\\b"), Pattern.compile("\\bAND\\b"),
        Pattern.compile("\\bOR\\b"), Pattern.compile("\\bLEA\\b"), Pattern.compile("\\bJMP\\b"),
        Pattern.compile("\\bJE\\b"), Pattern.compile("\\bJNE\\b"), Pattern.compile("\\bJZ\\b"),
        Pattern.compile("\\bJG\\b"), Pattern.compile("\\bJL\\b"), Pattern.compile("\\bINC\\b"),
        Pattern.compile("\\bDEC\\b"), Pattern.compile("\\bNOP\\b"), Pattern.compile("\\bINT\\b"),
        Pattern.compile("\\bCMOV\\b"), Pattern.compile("\\bSETCC\\b"), Pattern.compile("\\bLOOP\\b")
    };

    // x86-specific directives and registers
    private static final Pattern[] PATTERNS_X86_DIRECTIVES = {
        Pattern.compile("\\bSECTION\\b"), Pattern.compile("\\bSEGMENT\\b"),
        Pattern.compile("\\bGLOBAL\\b"), Pattern.compile("\\bEXTERN\\b"),
        Pattern.compile("\\b\\[RBP\\b"), Pattern.compile("\\b\\[RSP\\b"),
        Pattern.compile("\\b\\[ESP\\b"), Pattern.compile("\\b\\[EBP\\b"),
        Pattern.compile("\\bRAX\\b"), Pattern.compile("\\bRBX\\b"),
        Pattern.compile("\\bRCX\\b"), Pattern.compile("\\bRDX\\b"),
        Pattern.compile("\\bEAX\\b"), Pattern.compile("\\bEBX\\b"),
        Pattern.compile("\\bECX\\b"), Pattern.compile("\\bEDX\\b")
    };

    /**
     * Detects whether assembly code is 6502 or x86/x64 by analyzing content.
     * Looks for architecture-specific instructions and directives.
     */
    private String detectAssemblerType(String sourceCode) {
        String upperCode = sourceCode.toUpperCase();

        // Count 6502-specific indicators
        int score6502 = 0;
        int scoreX86 = 0;

        // Count matches for 6502 instructions
        for (Pattern pattern : PATTERNS_6502_INSTR) {
            if (pattern.matcher(upperCode).find()) {
                score6502++;
            }
        }

        // Count matches for 6502 directives (weighted higher)
        for (Pattern pattern : PATTERNS_6502_DIRECTIVES) {
            if (pattern.matcher(upperCode).find()) {
                score6502 += DIRECTIVE_WEIGHT;
            }
        }

        // Count matches for x86 instructions
        for (Pattern pattern : PATTERNS_X86_INSTR) {
            if (pattern.matcher(upperCode).find()) {
                scoreX86++;
            }
        }

        // Count matches for x86 directives (weighted higher)
        for (Pattern pattern : PATTERNS_X86_DIRECTIVES) {
            if (pattern.matcher(upperCode).find()) {
                scoreX86 += DIRECTIVE_WEIGHT;
            }
        }

        logger.info("Assembly detection scores - 6502: {}, x86: {}", score6502, scoreX86);

        // Decide based on scores
        if (score6502 > scoreX86) {
            return "6502";
        } else if (scoreX86 > score6502) {
            return "asm";
        } else {
            // Default to x86 if unclear (more common)
            logger.warn("Unable to confidently detect assembler type, defaulting to x86");
            return "asm";
        }
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
