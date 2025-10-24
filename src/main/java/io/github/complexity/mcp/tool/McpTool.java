package io.github.complexity.mcp.tool;

import com.google.gson.JsonObject;

/**
 * Interface for MCP tools following Command Pattern
 * Each tool is a self-contained command with its own schema and execution logic
 */
public interface McpTool {

    /**
     * Get the unique name of this tool
     */
    String getName();

    /**
     * Get the human-readable description of this tool
     */
    String getDescription();

    /**
     * Get the JSON schema for this tool's input parameters
     */
    JsonObject getInputSchema();

    /**
     * Execute the tool with the given arguments
     *
     * @param arguments JSON object containing the tool arguments
     * @return The result as a string (will be wrapped in MCP response)
     * @throws Exception if execution fails
     */
    String execute(JsonObject arguments) throws Exception;
}
