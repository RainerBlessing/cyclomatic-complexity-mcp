package io.github.complexity.mcp;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.github.complexity.calculator.CalculatorFactory;
import io.github.complexity.mcp.tool.AnalyzeComplexityCodeTool;
import io.github.complexity.mcp.tool.AnalyzeComplexityTool;
import io.github.complexity.mcp.tool.McpTool;

import java.util.HashMap;
import java.util.Map;

/**
 * Registry for MCP tools using Command Pattern
 * Manages tool definitions and execution
 */
public class ToolRegistry {

    private final Map<String, McpTool> tools = new HashMap<>();

    public ToolRegistry(CalculatorFactory calculatorFactory) {
        // Register tools
        registerTool(new AnalyzeComplexityTool(calculatorFactory));
        registerTool(new AnalyzeComplexityCodeTool(calculatorFactory));
    }

    /**
     * Register a tool
     */
    public void registerTool(McpTool tool) {
        tools.put(tool.getName(), tool);
    }

    /**
     * Get list of available tools with their schemas
     */
    public JsonObject getToolsList(JsonElement id) {
        JsonObject response = new JsonObject();
        response.add("jsonrpc", new com.google.gson.JsonPrimitive("2.0"));
        response.add("id", id);

        JsonArray toolsArray = new JsonArray();
        for (McpTool tool : tools.values()) {
            JsonObject toolDef = new JsonObject();
            toolDef.addProperty("name", tool.getName());
            toolDef.addProperty("description", tool.getDescription());
            toolDef.add("inputSchema", tool.getInputSchema());
            toolsArray.add(toolDef);
        }

        JsonObject result = new JsonObject();
        result.add("tools", toolsArray);

        response.add("result", result);
        return response;
    }

    /**
     * Execute a tool by name
     */
    public String executeTool(String toolName, JsonObject arguments) throws Exception {
        McpTool tool = tools.get(toolName);
        if (tool == null) {
            throw new IllegalArgumentException("Unknown tool: " + toolName);
        }
        return tool.execute(arguments);
    }

    /**
     * Check if a tool exists
     */
    public boolean hasTool(String toolName) {
        return tools.containsKey(toolName);
    }
}
