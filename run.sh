#!/bin/bash
# Simple launcher script for the MCP server

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
JAR_FILE="$SCRIPT_DIR/build/libs/cyclomatic-complexity-mcp.jar"

if [ ! -f "$JAR_FILE" ]; then
    echo "JAR file not found. Building project..."
    cd "$SCRIPT_DIR"
    ./gradlew clean build
    if [ $? -ne 0 ]; then
        echo "Build failed!"
        exit 1
    fi
fi

exec java -jar "$JAR_FILE" "$@"
