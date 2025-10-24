package io.github.complexity.mcp;

import com.google.gson.*;
import io.github.complexity.calculator.CalculatorFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

/**
 * MCP Server for cyclomatic complexity analysis
 * Implements the Model Context Protocol for Claude integration
 *
 * Responsibilities:
 * - JSON-RPC message handling (stdin/stdout)
 * - Request routing
 * - Error handling and logging
 */
public class McpServer {
    private static final Logger logger = LoggerFactory.getLogger(McpServer.class);
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    private final McpProtocolHandler protocolHandler;
    private final ToolRegistry toolRegistry;

    public McpServer() {
        CalculatorFactory calculatorFactory = new CalculatorFactory();
        this.protocolHandler = new McpProtocolHandler();
        this.toolRegistry = new ToolRegistry(calculatorFactory);
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
                    JsonObject error = protocolHandler.createErrorResponse(
                        null,
                        McpProtocolHandler.ErrorCodes.INTERNAL_ERROR,
                        "Internal error: " + e.getMessage()
                    );
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
                return protocolHandler.handleInitialize(idElement);
            case "tools/list":
                return toolRegistry.getToolsList(idElement);
            case "tools/call":
                return handleToolsCall(request, idElement);
            default:
                return protocolHandler.createErrorResponse(
                    idElement,
                    McpProtocolHandler.ErrorCodes.METHOD_NOT_FOUND,
                    "Method not found: " + method
                );
        }
    }

    private JsonObject handleToolsCall(JsonObject request, JsonElement id) {
        JsonObject params = request.getAsJsonObject("params");
        String toolName = params.get("name").getAsString();
        JsonObject arguments = params.getAsJsonObject("arguments");

        logger.debug("Tool call: {}", toolName);

        try {
            String resultText = toolRegistry.executeTool(toolName, arguments);
            return protocolHandler.createToolResponse(id, resultText);
        } catch (IllegalArgumentException e) {
            logger.error("Unknown tool: {}", toolName);
            return protocolHandler.createErrorResponse(
                id,
                McpProtocolHandler.ErrorCodes.INVALID_PARAMS,
                e.getMessage()
            );
        } catch (Exception e) {
            logger.error("Error executing tool: {}", toolName, e);
            return protocolHandler.createErrorResponse(
                id,
                McpProtocolHandler.ErrorCodes.INTERNAL_ERROR,
                "Tool execution error: " + e.getMessage()
            );
        }
    }
}
