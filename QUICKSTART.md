# Quick Start Guide

## Schritt 1: Java installieren

```bash
# Für Arch Linux / CachyOS
sudo pacman -S jdk17-openjdk

# Testen
java -version
```

Sie sollten Java 17 oder höher sehen.

## Schritt 2: Gradle Wrapper initialisieren

Da der Gradle Wrapper noch nicht vorhanden ist, müssen Sie Gradle einmalig installieren:

```bash
# Arch Linux / CachyOS
sudo pacman -S gradle

# Ins Projekt-Verzeichnis wechseln
cd /home/rainer/projekte/cyclomatic-complexity-mcp

# Gradle Wrapper initialisieren
gradle wrapper --gradle-version 8.5

# Optional: System-Gradle wieder deinstallieren
# sudo pacman -Rs gradle
```

## Schritt 3: Projekt bauen

```bash
./gradlew clean build
```

Das war's! Die JAR-Datei befindet sich jetzt in `build/libs/cyclomatic-complexity-mcp.jar`

## Schritt 4: In Claude Code konfigurieren

1. Öffnen Sie `~/.config/claude/config.json`

2. Fügen Sie hinzu (oder erstellen Sie die Datei, falls sie nicht existiert):

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

3. Claude Code neu starten

## Schritt 5: Testen

In Claude Code:

```
Analysiere die Komplexität von /home/rainer/projekte/cyclomatic-complexity-mcp/examples/Example.java
```

Sie sollten eine detaillierte Analyse der zyklomatischen Komplexität erhalten!

## Häufige Befehle

```bash
# Projekt neu bauen
./gradlew clean build

# Schneller Build (ohne clean)
./gradlew build

# Tests ausführen
./gradlew test

# Alle Tasks anzeigen
./gradlew tasks

# Mit Debug-Logging
./gradlew build --info
```

## Direkter Test (ohne Claude Code)

```bash
# Server starten
java -jar build/libs/cyclomatic-complexity-mcp.jar

# In einem anderen Terminal, JSON-RPC Request senden:
echo '{"jsonrpc":"2.0","id":1,"method":"initialize","params":{}}' | java -jar build/libs/cyclomatic-complexity-mcp.jar
```

## Alternativ: Mit run.sh

```bash
./run.sh
```

Dies baut das Projekt automatisch, falls nötig, und startet den Server.

## Nächste Schritte

- Lesen Sie [README.md](README.md) für Details zu Features und Verwendung
- Schauen Sie sich [INSTALLATION.md](INSTALLATION.md) für Troubleshooting an
- Prüfen Sie die Beispieldateien in `examples/`
