# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Cyclomatic Complexity MCP Server - Ein Model Context Protocol Server zur Berechnung der zyklomatischen Komplexität für Java und Assembler (x86/x64) Code.

**Hauptzweck**: Integration mit Claude Code, um automatisch Code-Komplexität zu analysieren und Refactoring-Kandidaten zu identifizieren.

## Build Commands

**WICHTIG**: Verwenden Sie immer `./gradlew` (Gradle Wrapper) statt `gradle` um Versionskonflikte zu vermeiden.

### Projekt bauen
```bash
./gradlew clean build
```

Erstellt: `build/libs/cyclomatic-complexity-mcp.jar` (Shadow JAR mit allen Dependencies)

### Schneller Build (ohne clean)
```bash
./gradlew build
```

### Tests ausführen
```bash
./gradlew test
```

### Einzelnen Test ausführen
```bash
./gradlew test --tests "ClassName.testMethodName"
```

### Build mit Debug-Info
```bash
./gradlew build --info
```

### Shadow JAR bauen (mit allen Dependencies)
```bash
./gradlew shadowJar
```

### Gradle Wrapper aktualisieren
```bash
gradle wrapper --gradle-version 9.1
```

## Project Architecture

### Hauptkomponenten

1. **MCP Server** (`io.github.complexity.mcp.McpServer`)
   - Implementiert JSON-RPC über stdin/stdout
   - Handelt MCP-Protokoll Requests: initialize, tools/list, tools/call
   - Entry Point der Anwendung

2. **Complexity Calculators** (`io.github.complexity.calculator`)
   - **Interface**: `ComplexityCalculator` - Basis-Interface für alle Analyzer
   - **JavaComplexityCalculator**: Nutzt JavaParser für AST-basierte Analyse
   - **AssemblerComplexityCalculator**: Pattern-basierte Analyse für x86/x64 ASM
   - **ComplexityResult**: DTO für Analyse-Ergebnisse

### Package-Struktur

```
io.github.complexity/
├── calculator/
│   ├── ComplexityCalculator.java       # Interface
│   ├── ComplexityResult.java           # Data Transfer Object
│   ├── JavaComplexityCalculator.java   # Java-Implementierung
│   └── AssemblerComplexityCalculator.java # ASM-Implementierung
└── mcp/
    └── McpServer.java                   # MCP Protocol Handler
```

### Zyklomatische Komplexität Berechnung

**Java** (AST-basiert via JavaParser):
- Base Complexity: 1
- +1 für: if, for, while, do-while, for-each, switch cases, catch, ternary (?:), && und || Operatoren

**Assembler** (Pattern-basiert):
- Base Complexity: 1
- +1 für: Conditional Jumps (JE, JNE, JZ, JG, etc.), LOOP instructions, CMOV instructions
- Erkennt PROC/ENDP blocks und Label-basierte Funktionen

### MCP Protocol Flow

1. **Initialize**: Client sendet initialize → Server antwortet mit capabilities
2. **Tools List**: Client fragt verfügbare Tools ab → Server listet analyze_complexity, analyze_complexity_code
3. **Tool Call**: Client ruft Tool → Server führt Analyse aus → Gibt formatted text zurück

### Dependencies

- **JavaParser 3.25.8**: Java AST parsing
- **Gson 2.10.1**: JSON serialization für MCP protocol
- **SLF4J 2.0.9**: Logging
- **JUnit 5.10.1**: Testing

### Build Configuration

- **Gradle**: 9.1+ (via Wrapper)
- **Java**: 17+ (Target: 17, Runtime: unterstützt bis Java 25)
- **Shadow Plugin**: 9.2.2 (com.gradleup.shadow) - für Fat JAR Creation

## Development Guidelines

### Neue Sprache hinzufügen

1. Erstellen Sie `XyzComplexityCalculator implements ComplexityCalculator`
2. Implementieren Sie `calculate()` und `getLanguage()`
3. Registrieren Sie in `McpServer` constructor: `calculators.put("xyz", new XyzComplexityCalculator())`
4. Aktualisieren Sie `detectLanguage()` für Auto-Detection
5. Fügen Sie Tests und Beispiele hinzu

### MCP Tool hinzufügen

1. Erstellen Sie Tool-Definition in `handleToolsList()`
2. Implementieren Sie Handler-Methode (z.B. `handleNewTool()`)
3. Fügen Sie Case in `handleToolsCall()` hinzu
4. Aktualisieren Sie README mit Verwendungsbeispiel

### Testing

- Manuelle Tests: `java -jar build/libs/cyclomatic-complexity-mcp.jar`
- JSON-RPC Requests via stdin senden
- Beispieldateien in `examples/` nutzen

### Logging

Für Debug-Output:
```bash
java -Dorg.slf4j.simpleLogger.defaultLogLevel=debug \
     -jar build/libs/cyclomatic-complexity-mcp.jar
```

## Code Conventions

- **Java 17** Features erlaubt
- **Package Structure**: Feature-basiert (calculator, mcp, parser)
- **Naming**:
  - Classes: PascalCase
  - Methods: camelCase
  - Constants: UPPER_SNAKE_CASE
- **Error Handling**: Exceptions mit aussagekräftigen Messages
- **Logging**: SLF4J für alle Log-Ausgaben (NICHT System.out für Server-Logik)

## Important Notes

- **Stdin/Stdout**: Server nutzt stdio für MCP communication. Kein System.out.println() im Server-Code!
- **JSON-RPC Format**: Strict adherence zu JSON-RPC 2.0 spec erforderlich
- **Blocking I/O**: Server ist single-threaded, blockiert auf stdin reads
- **Error Responses**: Immer proper JSON-RPC error objects zurückgeben

## Configuration

MCP Server wird via Claude Code config.json konfiguriert:

```json
{
  "mcpServers": {
    "cyclomatic-complexity": {
      "command": "java",
      "args": ["-jar", "/pfad/zu/build/libs/cyclomatic-complexity-mcp.jar"]
    }
  }
}
```

## Troubleshooting

### Server startet nicht
- Prüfen: Java 17+ installiert (`java -version`)
- Prüfen: JAR existiert in `build/libs/`
- Neu bauen: `./gradlew clean build`

### Parsing-Fehler
- **Java**: JavaParser erfordert syntaktisch korrekten Code
- **Assembler**: Limitierte Dialect-Unterstützung (MASM/NASM/GAS)

### MCP Communication Fehler
- Check JSON-RPC Format mit validator
- Enable debug logging
- Test manual mit stdin/stdout
