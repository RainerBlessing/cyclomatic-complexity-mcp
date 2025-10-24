package io.github.complexity.mcp;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

/**
 * Handles MCP protocol-specific operations
 * Creates JSON-RPC 2.0 compliant responses
 */
public class McpProtocolHandler {

    /**
     * Handle initialize request
     */
    public JsonObject handleInitialize(JsonElement id) {
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

        return response;
    }

    /**
     * Create success response for tool execution
     */
    public JsonObject createToolResponse(JsonElement id, String resultText) {
        JsonObject response = new JsonObject();
        response.add("jsonrpc", new JsonPrimitive("2.0"));
        response.add("id", id);

        JsonObject result = new JsonObject();

        // Create content array with text content
        com.google.gson.JsonArray content = new com.google.gson.JsonArray();
        JsonObject textContent = new JsonObject();
        textContent.addProperty("type", "text");
        textContent.addProperty("text", resultText);
        content.add(textContent);

        result.add("content", content);
        response.add("result", result);

        return response;
    }

    /**
     * Create error response
     */
    public JsonObject createErrorResponse(JsonElement id, int code, String message) {
        JsonObject response = new JsonObject();
        response.add("jsonrpc", new JsonPrimitive("2.0"));
        response.add("id", id);

        JsonObject error = new JsonObject();
        error.addProperty("code", code);
        error.addProperty("message", message);

        response.add("error", error);
        return response;
    }

    /**
     * Error codes for JSON-RPC
     */
    public static class ErrorCodes {
        public static final int METHOD_NOT_FOUND = -32601;
        public static final int INVALID_PARAMS = -32602;
        public static final int INTERNAL_ERROR = -32603;
    }
}
