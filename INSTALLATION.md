# Installation and Setup

## Install Prerequisites

### Arch Linux / CachyOS

```bash
sudo pacman -S jdk17-openjdk
```

Optional (Gradle Wrapper is recommended):
```bash
sudo pacman -S gradle
```

### Ubuntu/Debian

```bash
sudo apt update
sudo apt install openjdk-17-jdk
```

### Fedora

```bash
sudo dnf install java-17-openjdk
```

### macOS

```bash
brew install openjdk@17
```

## Build Project

### With Gradle Wrapper (recommended - no Gradle installation needed)

```bash
cd /home/rainer/projekte/cyclomatic-complexity-mcp
./gradlew clean build
```

### With system-installed Gradle

```bash
cd /home/rainer/projekte/cyclomatic-complexity-mcp
gradle clean build
```

This creates:
- `build/libs/cyclomatic-complexity-mcp.jar` - The executable JAR file

## Configure MCP Server

### For Claude Code

1. Open the Claude Code configuration:
   - Linux: `~/.config/claude/config.json`
   - macOS: `~/Library/Application Support/Claude/config.json`

2. Add the MCP server configuration:

```json
{
  "mcpServers": {
    "cyclomatic-complexity": {
      "command": "java",
      "args": [
        "-jar",
        "/home/rainer/projekte/cyclomatic-complexity-mcp/build/libs/cyclomatic-complexity-mcp.jar"
      ]
    }
  }
}
```

3. Restart Claude Code

## Testing

### Manual Test

You can test the server manually:

```bash
cd /home/rainer/projekte/cyclomatic-complexity-mcp
java -jar build/libs/cyclomatic-complexity-mcp.jar
```

Then send JSON-RPC commands via stdin:

```json
{"jsonrpc":"2.0","id":1,"method":"initialize","params":{}}
```

The server should respond with an initialize response.

### Test with Example Files

After the server is configured in Claude Code, you can ask:

```
Analyze the complexity of /home/rainer/projekte/cyclomatic-complexity-mcp/examples/Example.java
```

Claude Code will automatically use the `analyze_complexity` tool.

## Troubleshooting

### "command not found: java"

Java is not installed or not in PATH. Install Java 17+ (see above).

### "gradle: command not found"

Use the Gradle Wrapper: `./gradlew` instead of `gradle`

### Server won't start in Claude Code

1. Check the path in config.json
2. Make sure the JAR file exists
3. Test the server manually
4. Check Claude Code logs

### Parsing errors

- **Java**: Make sure the code is syntactically correct
- **Assembler**: Supports MASM/NASM/GAS syntax, but not all dialects

## Development

### Run tests (if implemented)

```bash
./gradlew test
```

### Enable debug logging

```bash
java -Dorg.slf4j.simpleLogger.defaultLogLevel=debug \
     -jar build/libs/cyclomatic-complexity-mcp.jar
```

### Code changes

After making code changes:

```bash
./gradlew clean build
```

Then restart Claude Code.
