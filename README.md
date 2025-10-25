# Cyclomatic Complexity MCP Server

A Model Context Protocol (MCP) Server for calculating cyclomatic complexity of Java, x86/x64 Assembler, and 6502 Assembler code.

## Features

- **Java Analysis**: Uses JavaParser for precise AST-based complexity calculation
- **x86/x64 Assembler Analysis**: Supports MASM/NASM/GAS syntax
- **6502 Assembler Analysis**: Supports ca65, DASM, and generic 6502 syntax
- **MCP Integration**: Works directly with Claude Code
- **Detailed Metrics**: Shows complexity per function/method/subroutine
- **Warnings**: Marks functions with high complexity (>10)

## Installation

### Prerequisites

- Java 17 or higher
- Gradle 8.0 or higher (or use the Gradle Wrapper: `./gradlew`)

### Build

```bash
cd cyclomatic-complexity-mcp
./gradlew clean build
```

This creates an executable JAR file: `build/libs/cyclomatic-complexity-mcp.jar`

Alternatively with system-installed Gradle:
```bash
gradle clean build
```

## Configuration for Claude Code

Add the following configuration to your Claude Code configuration (`~/.config/claude/config.json` or `~/Library/Application Support/Claude/config.json` on macOS):

```json
{
  "mcpServers": {
    "cyclomatic-complexity": {
      "command": "java",
      "args": [
        "-jar",
        "/absolute/path/to/cyclomatic-complexity-mcp/build/libs/cyclomatic-complexity-mcp.jar"
      ]
    }
  }
}
```

Replace `/absolute/path/to/` with the actual path to the project.

## Usage

After configuration, Claude Code can automatically access two tools:

### 1. `analyze_complexity` - Analyze file

Analyzes a file by its path:

```
Analyze the complexity of src/Main.java
```

### 2. `analyze_complexity_code` - Analyze code string

Analyzes directly provided code:

```
Analyze the complexity of this Java code:
[Insert code here]
```

## Supported Languages

### Java
- Detects: if, else, for, while, do-while, switch/case, catch, ternary (?:)
- Counts: &&, || operators in conditions
- Base complexity: 1 per method

### Assembler (x86/x64)
- Detects: All conditional jumps (JE, JNE, JZ, JG, JL, etc.)
- Counts: LOOP instructions, conditional MOVEs (CMOV*)
- Supports: PROC/ENDP blocks and label-based functions

### 6502 Assembler
- Detects: All conditional branches (BEQ, BNE, BCC, BCS, BPL, BMI, BVC, BVS)
- Counts: Bit branches (BBR0-BBR7, BBS0-BBS7 for 65C02)
- Supports:
  - ca65 syntax (.proc/.endproc blocks)
  - DASM syntax (SUBROUTINE directive)
  - Generic label+RTS pattern
- File extensions: .a65, .s65, .asm65, .a
- Base complexity: 1 per subroutine

## Complexity Metrics

Cyclomatic Complexity (McCabe) is calculated as follows:

- **1-10**: Simple, well maintainable
- **11-20**: Moderately complex, should be reviewed ⚠️
- **21-50**: Complex, refactoring recommended
- **>50**: Very complex, urgently needs refactoring

## Example Output

```
File: Calculator.java (Java)
Total Functions: 5
Total Complexity: 23
Max Complexity: 8 in calculateTax(double, boolean, int)

Function Complexities:
  calculateTax(double, boolean, int): 8
  processPayment(Payment): 6
  validateInput(String): 4
  add(int, int): 1
  subtract(int, int): 1
```

## Development

### Project Structure

```
src/main/java/io/github/complexity/
├── calculator/
│   ├── ComplexityCalculator.java          # Interface
│   ├── ComplexityResult.java              # Result DTO
│   ├── JavaComplexityCalculator.java      # Java implementation
│   ├── AssemblerComplexityCalculator.java # x86/x64 ASM implementation
│   └── Mos6502ComplexityCalculator.java   # 6502 ASM implementation
└── mcp/
    └── McpServer.java                      # MCP Server
```

### Run tests

```bash
./gradlew test
```

### Logging

The server uses SLF4J with Simple Logger. Log level can be set via system properties:

```bash
java -Dorg.slf4j.simpleLogger.defaultLogLevel=debug -jar build/libs/cyclomatic-complexity-mcp.jar
```

## Troubleshooting

### Server not connecting / timeout errors

If Claude Code shows timeout errors when connecting to the MCP server, check the following:

1. **Verify JAR path is absolute**: The path in the configuration must be absolute, not relative
   ```json
   "args": ["-jar", "/home/user/path/to/cyclomatic-complexity-mcp.jar"]
   ```

2. **Check Java version**: Requires Java 17 or higher
   ```bash
   java -version
   ```

3. **Rebuild after updates**: Always rebuild after pulling updates
   ```bash
   ./gradlew clean build
   ```

4. **Check MCP logs**: Claude Code logs MCP server output to:
   - Linux: `~/.cache/claude-cli-nodejs/<project>/mcp-logs-cyclomatic-complexity/`
   - macOS: `~/Library/Caches/claude-cli-nodejs/<project>/mcp-logs-cyclomatic-complexity/`

5. **Test server manually**: Verify the server responds to initialize requests
   ```bash
   echo '{"jsonrpc":"2.0","id":1,"method":"initialize","params":{"protocolVersion":"2025-06-18","capabilities":{},"clientInfo":{"name":"test","version":"1.0"}}}' | java -jar build/libs/cyclomatic-complexity-mcp.jar
   ```
   Should return a JSON response with `"protocolVersion":"2025-06-18"` on a single line.

### Server starts but tools not available

If the server connects but tools don't appear in Claude Code:

1. Verify the server responds to `tools/list`:
   ```bash
   (echo '{"jsonrpc":"2.0","id":1,"method":"initialize","params":{"protocolVersion":"2025-06-18","capabilities":{},"clientInfo":{"name":"test","version":"1.0"}}}' && echo '{"jsonrpc":"2.0","method":"notifications/initialized"}' && echo '{"jsonrpc":"2.0","id":2,"method":"tools/list"}') | java -jar build/libs/cyclomatic-complexity-mcp.jar
   ```

2. Check that the response includes `analyze_complexity` and `analyze_complexity_code` tools

## License

MIT License

## Contributing

Contributions are welcome! Please create an issue or pull request.
