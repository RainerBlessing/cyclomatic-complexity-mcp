# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Cyclomatic Complexity MCP Server - A Model Context Protocol Server for calculating cyclomatic complexity of Java, x86/x64 Assembler, and 6502 Assembler code.

**Main Purpose**: Integration with Claude Code to automatically analyze code complexity and identify refactoring candidates.

## Build Commands

**IMPORTANT**: Always use `./gradlew` (Gradle Wrapper) instead of `gradle` to avoid version conflicts.

### Build project
```bash
./gradlew clean build
```

Creates: `build/libs/cyclomatic-complexity-mcp.jar` (Shadow JAR with all dependencies)

### Quick build (without clean)
```bash
./gradlew build
```

### Run tests
```bash
./gradlew test
```

### Run single test
```bash
./gradlew test --tests "ClassName.testMethodName"
```

### Build with debug info
```bash
./gradlew build --info
```

### Build Shadow JAR (with all dependencies)
```bash
./gradlew shadowJar
```

### Update Gradle Wrapper
```bash
gradle wrapper --gradle-version 9.1
```

## Project Architecture

### Main Components

1. **MCP Server** (`io.github.complexity.mcp.McpServer`)
   - Implements JSON-RPC over stdin/stdout
   - Handles MCP protocol requests: initialize, tools/list, tools/call
   - Application entry point

2. **Complexity Calculators** (`io.github.complexity.calculator`)
   - **Interface**: `ComplexityCalculator` - Base interface for all analyzers
   - **JavaComplexityCalculator**: Uses JavaParser for AST-based analysis
   - **AssemblerComplexityCalculator**: Pattern-based analysis for x86/x64 ASM
   - **Mos6502ComplexityCalculator**: Pattern-based analysis for 6502 ASM
   - **ComplexityResult**: DTO for analysis results

### Package Structure

```
io.github.complexity/
├── calculator/
│   ├── ComplexityCalculator.java          # Interface
│   ├── ComplexityResult.java              # Data Transfer Object
│   ├── JavaComplexityCalculator.java      # Java implementation
│   ├── AssemblerComplexityCalculator.java # x86/x64 ASM implementation
│   └── Mos6502ComplexityCalculator.java   # 6502 ASM implementation
└── mcp/
    └── McpServer.java                      # MCP Protocol Handler
```

### Cyclomatic Complexity Calculation

**Java** (AST-based via JavaParser):
- Base Complexity: 1
- +1 for: if, for, while, do-while, for-each, switch cases, catch, ternary (?:), && and || operators

**x86/x64 Assembler** (Pattern-based):
- Base Complexity: 1
- +1 for: Conditional Jumps (JE, JNE, JZ, JG, etc.), LOOP instructions, CMOV instructions
- Detects PROC/ENDP blocks and label-based functions

**6502 Assembler** (Pattern-based):
- Base Complexity: 1 per subroutine
- +1 for: Conditional branches (BEQ, BNE, BCC, BCS, BPL, BMI, BVC, BVS)
- +1 for: Bit branches (BBR0-BBR7, BBS0-BBS7 for 65C02)
- Detects subroutines via:
  - ca65 .proc/.endproc blocks
  - DASM SUBROUTINE directive
  - Generic label + RTS pattern

### MCP Protocol Flow

1. **Initialize**: Client sends initialize → Server responds with capabilities
2. **Tools List**: Client requests available tools → Server lists analyze_complexity, analyze_complexity_code
3. **Tool Call**: Client calls tool → Server performs analysis → Returns formatted text

### Dependencies

- **JavaParser 3.25.8**: Java AST parsing
- **Gson 2.10.1**: JSON serialization for MCP protocol
- **SLF4J 2.0.9**: Logging
- **JUnit 5.10.1**: Testing

### Build Configuration

- **Gradle**: 9.1+ (via Wrapper)
- **Java**: 17+ (Target: 17, Runtime: supports up to Java 25)
- **Shadow Plugin**: 9.2.2 (com.gradleup.shadow) - for Fat JAR creation

## Development Guidelines

### Adding a New Language

1. Create `XyzComplexityCalculator implements ComplexityCalculator`
2. Implement `calculate()` and `getLanguage()`
3. Register in `McpServer` constructor: `calculators.put("xyz", new XyzComplexityCalculator())`
4. Update `detectLanguage()` for auto-detection
5. Add tests and examples

### Adding an MCP Tool

1. Create tool definition in `handleToolsList()`
2. Implement handler method (e.g. `handleNewTool()`)
3. Add case in `handleToolsCall()`
4. Update README with usage example

### Testing

- Manual tests: `java -jar build/libs/cyclomatic-complexity-mcp.jar`
- Send JSON-RPC requests via stdin
- Use example files in `examples/`

### Logging

For debug output:
```bash
java -Dorg.slf4j.simpleLogger.defaultLogLevel=debug \
     -jar build/libs/cyclomatic-complexity-mcp.jar
```

## Code Conventions

- **Java 17** Features allowed
- **Package Structure**: Feature-based (calculator, mcp, parser)
- **Naming**:
  - Classes: PascalCase
  - Methods: camelCase
  - Constants: UPPER_SNAKE_CASE
- **Error Handling**: Exceptions with meaningful messages
- **Logging**: SLF4J for all log output (NOT System.out for server logic)

## Important Notes

- **Stdin/Stdout**: Server uses stdio for MCP communication. No System.out.println() in server code!
- **JSON-RPC Format**: Strict adherence to JSON-RPC 2.0 spec required
- **Blocking I/O**: Server is single-threaded, blocks on stdin reads
- **Error Responses**: Always return proper JSON-RPC error objects

## Configuration

MCP Server is configured via Claude Code config.json:

```json
{
  "mcpServers": {
    "cyclomatic-complexity": {
      "command": "java",
      "args": ["-jar", "/path/to/build/libs/cyclomatic-complexity-mcp.jar"]
    }
  }
}
```

## Troubleshooting

### Server won't start
- Check: Java 17+ installed (`java -version`)
- Check: JAR exists in `build/libs/`
- Rebuild: `./gradlew clean build`

### Parsing errors
- **Java**: JavaParser requires syntactically correct code
- **Assembler**: Limited dialect support (MASM/NASM/GAS)

### MCP Communication errors
- Check JSON-RPC format with validator
- Enable debug logging
- Test manually with stdin/stdout
