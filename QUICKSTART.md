# Quick Start Guide

## Step 1: Install Java

```bash
# For Arch Linux / CachyOS
sudo pacman -S jdk17-openjdk

# Test
java -version
```

You should see Java 17 or higher.

## Step 2: Initialize Gradle Wrapper

Since the Gradle Wrapper is not yet present, you need to install Gradle once:

```bash
# Arch Linux / CachyOS
sudo pacman -S gradle

# Change to project directory
cd /home/rainer/projekte/cyclomatic-complexity-mcp

# Initialize Gradle Wrapper
gradle wrapper --gradle-version 8.5

# Optional: Uninstall system Gradle again
# sudo pacman -Rs gradle
```

## Step 3: Build Project

```bash
./gradlew clean build
```

That's it! The JAR file is now located at `build/libs/cyclomatic-complexity-mcp.jar`

## Step 4: Configure in Claude Code

1. Open `~/.config/claude/config.json`

2. Add (or create the file if it doesn't exist):

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

## Step 5: Test

In Claude Code:

```
Analyze the complexity of /home/rainer/projekte/cyclomatic-complexity-mcp/examples/Example.java
```

You should receive a detailed cyclomatic complexity analysis!

## Common Commands

```bash
# Rebuild project
./gradlew clean build

# Quick build (without clean)
./gradlew build

# Run tests
./gradlew test

# Show all tasks
./gradlew tasks

# With debug logging
./gradlew build --info
```

## Direct Test (without Claude Code)

```bash
# Start server
java -jar build/libs/cyclomatic-complexity-mcp.jar

# In another terminal, send JSON-RPC request:
echo '{"jsonrpc":"2.0","id":1,"method":"initialize","params":{}}' | java -jar build/libs/cyclomatic-complexity-mcp.jar
```

## Alternative: Using run.sh

```bash
./run.sh
```

This automatically builds the project if necessary and starts the server.

## Next Steps

- Read [README.md](README.md) for details on features and usage
- Check [INSTALLATION.md](INSTALLATION.md) for troubleshooting
- Examine the example files in `examples/`
