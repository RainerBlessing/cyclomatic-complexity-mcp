# Cyclomatic Complexity MCP Server

Ein Model Context Protocol (MCP) Server zur Berechnung der zyklomatischen Komplexität für Java und Assembler Code.

## Features

- **Java Analyse**: Nutzt JavaParser für präzise AST-basierte Komplexitätsberechnung
- **Assembler Analyse**: Unterstützt x86/x64 Assembler (MASM/NASM/GAS Syntax)
- **MCP Integration**: Funktioniert direkt mit Claude Code
- **Detaillierte Metriken**: Zeigt Komplexität pro Funktion/Methode
- **Warnungen**: Markiert Funktionen mit hoher Komplexität (>10)

## Installation

### Voraussetzungen

- Java 17 oder höher
- Gradle 8.0 oder höher (oder verwenden Sie den Gradle Wrapper: `./gradlew`)

### Build

```bash
cd cyclomatic-complexity-mcp
./gradlew clean build
```

Dies erstellt eine ausführbare JAR-Datei: `build/libs/cyclomatic-complexity-mcp.jar`

Alternativ mit installiertem System-Gradle:
```bash
gradle clean build
```

## Konfiguration für Claude Code

Fügen Sie folgende Konfiguration zu Ihrer Claude Code Konfiguration hinzu (`~/.config/claude/config.json` oder `~/Library/Application Support/Claude/config.json` auf macOS):

```json
{
  "mcpServers": {
    "cyclomatic-complexity": {
      "command": "java",
      "args": [
        "-jar",
        "/absoluter/pfad/zu/cyclomatic-complexity-mcp/build/libs/cyclomatic-complexity-mcp.jar"
      ]
    }
  }
}
```

Ersetzen Sie `/absoluter/pfad/zu/` mit dem tatsächlichen Pfad zum Projekt.

## Verwendung

Nach der Konfiguration kann Claude Code automatisch auf zwei Tools zugreifen:

### 1. `analyze_complexity` - Datei analysieren

Analysiert eine Datei anhand ihres Pfades:

```
Analysiere die Komplexität von src/Main.java
```

### 2. `analyze_complexity_code` - Code-String analysieren

Analysiert direkt übergebenen Code:

```
Analysiere die Komplexität dieses Java Codes:
[Code hier einfügen]
```

## Unterstützte Sprachen

### Java
- Erkennt: if, else, for, while, do-while, switch/case, catch, ternary (?:)
- Zählt: &&, || Operatoren in Bedingungen
- Basis-Komplexität: 1 pro Methode

### Assembler (x86/x64)
- Erkennt: Alle konditionalen Sprünge (JE, JNE, JZ, JG, JL, etc.)
- Zählt: LOOP-Instruktionen, konditionale MOVEs (CMOV*)
- Unterstützt: PROC/ENDP Blöcke und Label-basierte Funktionen

## Komplexitäts-Metriken

Die Zyklomatische Komplexität (McCabe) wird wie folgt berechnet:

- **1-10**: Einfach, gut wartbar
- **11-20**: Moderat komplex, sollte überprüft werden ⚠️
- **21-50**: Komplex, Refactoring empfohlen
- **>50**: Sehr komplex, dringend überarbeiten

## Beispiel-Output

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

## Entwicklung

### Projekt-Struktur

```
src/main/java/io/github/complexity/
├── calculator/
│   ├── ComplexityCalculator.java       # Interface
│   ├── ComplexityResult.java           # Result DTO
│   ├── JavaComplexityCalculator.java   # Java Implementierung
│   └── AssemblerComplexityCalculator.java # ASM Implementierung
└── mcp/
    └── McpServer.java                   # MCP Server
```

### Tests ausführen

```bash
./gradlew test
```

### Logging

Der Server nutzt SLF4J mit Simple Logger. Log-Level kann über System Properties gesetzt werden:

```bash
java -Dorg.slf4j.simpleLogger.defaultLogLevel=debug -jar build/libs/cyclomatic-complexity-mcp.jar
```

## Lizenz

MIT License

## Beiträge

Beiträge sind willkommen! Bitte erstellen Sie ein Issue oder Pull Request.
