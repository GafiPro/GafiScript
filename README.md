# GafiScript

GafiScript is a Java-first scripting and automation platform for Minecraft Java 1.21.11 + Fabric.

The script language is real Java 21. The platform adds a Minecraft API, runtime compiler, scheduler, event system, persistent storage, project support and an integrated editor.

## Platform

### Runtime
- Java 21 compilation at runtime
- single-file scripts
- multi-file projects
- project manifests and local dependencies
- JAR dependencies in project libs/
- compile diagnostics
- lifecycle states
- safe cleanup on stop/reload
- REPL
- cooperative debugger probes

### Minecraft API
- players
- entities
- inventories
- blocks and regions
- world time and weather
- dimensions
- biomes
- gamerules
- spawn points
- world border
- particles
- sounds
- effects
- boss bars
- scoreboards
- titles/action bars/chat
- item data components
- GUI inventories
- raycasting
- commands
- custom events
- persistent storage/config/database

### Scheduler
- next tick
- delayed tasks
- repeated tasks
- task groups
- sequences
- async virtual threads
- per-script ownership
- watchdog timing

### IDE
The in-game editor supports:
- Java source editing
- syntax highlighting
- completion
- selection
- copy/cut/paste
- undo/redo
- find/replace
- formatting
- diagnostics
- hover documentation
- go-to-definition
- multi-file project editing

## First script

    import static com.gafipro.gafiscript.api.Gafi.*;

    public class Main {
        public static void start() {
            broadcast("Hello from GafiScript!");
        }
    }

## Project layout

    gafiscript/
      projects/
        Example/
          manifest.json
          src/
            Main.java
          libs/

## Commands

    /gafiscript help
    /gafiscript list
    /gafiscript run <script>
    /gafiscript stop <script>
    /gafiscript info <script>
    /gafiscript repl <code>
    /gafiscript debug enable
    /gafiscript debug break <script> <line>
    /gafiscript debug hits <script>
    /gafiscript project list
    /gafiscript project create <project>
    /gafiscript project edit <project>
    /gafiscript project run <project>
    /gafiscript project reload <project>
    /gafiscript project export <project>
    /gafiscript project import <archive>

## Security

GafiScript uses multiple defensive layers: source validation, Java compilation without annotation processing, generated-bytecode inspection, a restricted classloader, permission checks and lifecycle cleanup.

This is not a formally verified JVM sandbox. Do not treat arbitrary hostile code as safe to execute inside a Minecraft server process.

## Build

    ./gradlew clean build --no-daemon --max-workers=1

Windows:

    gradlew.bat clean build --no-daemon --max-workers=1

## Documentation

Core:
- docs/FOR_JAVA_DEVELOPERS.md
- docs/FUNCTIONALITY.md
- docs/JAVA_VS_GAFISCRIPT.md
- docs/ARCHITECTURE.md
- docs/API_INDEX.md
- docs/MENTAL_MODEL.md

Runtime:
- docs/SCHEDULER.md
- docs/THREADING.md
- docs/STORAGE.md
- docs/CONFIG.md
- docs/PROJECTS.md
- docs/COMMANDS.md
- docs/REPL.md
- docs/DEBUGGER.md
- docs/PROFILING.md
- docs/PERMISSIONS.md

IDE:
- docs/EDITOR.md
- docs/GUI.md

Security:
- docs/SECURITY.md
- docs/SAFE_CODING.md
- docs/ERRORS.md

Guides:
- docs/FROM_COMMAND_BLOCKS.md
- docs/FOR_MOD_DEVELOPERS.md
- docs/ADVANCED.md

Examples are stored under examples/.
