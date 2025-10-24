# Installation und Setup

## Voraussetzungen installieren

### Arch Linux / CachyOS

```bash
sudo pacman -S jdk17-openjdk
```

Optional (Gradle Wrapper wird empfohlen):
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

## Projekt bauen

### Mit Gradle Wrapper (empfohlen - keine Gradle-Installation nötig)

```bash
cd /home/rainer/projekte/cyclomatic-complexity-mcp
./gradlew clean build
```

### Mit installiertem System-Gradle

```bash
cd /home/rainer/projekte/cyclomatic-complexity-mcp
gradle clean build
```

Dies erstellt:
- `build/libs/cyclomatic-complexity-mcp.jar` - Die ausführbare JAR-Datei

## MCP Server konfigurieren

### Für Claude Code

1. Öffnen Sie die Claude Code Konfiguration:
   - Linux: `~/.config/claude/config.json`
   - macOS: `~/Library/Application Support/Claude/config.json`

2. Fügen Sie die MCP Server Konfiguration hinzu:

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

3. Starten Sie Claude Code neu

## Testen

### Manueller Test

Sie können den Server manuell testen:

```bash
cd /home/rainer/projekte/cyclomatic-complexity-mcp
java -jar build/libs/cyclomatic-complexity-mcp.jar
```

Dann senden Sie JSON-RPC Befehle via stdin:

```json
{"jsonrpc":"2.0","id":1,"method":"initialize","params":{}}
```

Der Server sollte mit einer Initialize-Response antworten.

### Test mit Beispieldateien

Nachdem der Server in Claude Code konfiguriert ist, können Sie fragen:

```
Analysiere die Komplexität von /home/rainer/projekte/cyclomatic-complexity-mcp/examples/Example.java
```

Claude Code wird automatisch das `analyze_complexity` Tool nutzen.

## Troubleshooting

### "command not found: java"

Java ist nicht installiert oder nicht im PATH. Installieren Sie Java 17+ (siehe oben).

### "gradle: command not found"

Nutzen Sie den Gradle Wrapper: `./gradlew` statt `gradle`

### Server startet nicht in Claude Code

1. Prüfen Sie den Pfad in der config.json
2. Stellen Sie sicher, dass die JAR-Datei existiert
3. Testen Sie den Server manuell
4. Prüfen Sie die Logs von Claude Code

### Parsing-Fehler

- **Java**: Stellen Sie sicher, dass der Code syntaktisch korrekt ist
- **Assembler**: Unterstützt MASM/NASM/GAS Syntax, aber nicht alle Dialekte

## Entwicklung

### Tests ausführen (wenn implementiert)

```bash
./gradlew test
```

### Debug-Logging aktivieren

```bash
java -Dorg.slf4j.simpleLogger.defaultLogLevel=debug \
     -jar build/libs/cyclomatic-complexity-mcp.jar
```

### Code-Änderungen

Nach Änderungen am Code:

```bash
./gradlew clean build
```

Dann Claude Code neu starten.
