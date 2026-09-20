# GafiScript

GafiScript is a Java-first scripting and automation mod for Minecraft Java.

The goal is to replace large command-block systems with normal Java code plus a documented Minecraft API.

## Current platform

Target:
- Minecraft 1.21.11
- Java 21
- Fabric
- Fabric Loom remap

The project intentionally uses real Java. It does not introduce a second programming language.

## First script

    import static com.gafipro.gafiscript.api.Gafi.*;

    public class Main {
        public static void start() {
            broadcast("Hello from GafiScript!");
        }
    }

## Runtime APIs currently available

    Gafi.broadcast("Hello");

    Gafi.world().setBlock(
        GafiPosition.of(10, 64, 10),
        "minecraft:redstone_block"
    );

    Gafi.scheduler().delaySeconds(3, () -> {
        Gafi.broadcast("3 seconds later");
    });

    Gafi.events().onPlayerJoin(player -> {
        player.sendMessage("Welcome!");
    });

## In-game workflow

1. Place a GafiScript Block.
2. Right-click it as an operator.
3. Edit Java in the integrated editor.
4. Press Ctrl+S to save.
5. Press F5 to compile and run.
6. Use Ctrl+Space for API completion.

## Server command

    /gafiscript help
    /gafiscript list
    /gafiscript run <script>
    /gafiscript stop <script>
    /gafiscript info <script>

File scripts are loaded from the server run directory under:

    gafiscript/scripts/

## Security

GafiScript currently includes source validation and a restricted class loader. This is defensive protection, not a formally verified sandbox. Do not execute hostile untrusted scripts on a public server and assume they are completely isolated.

The initial editor and administration command use operator permission level 2.

## Documentation

Start here:

- docs/FOR_JAVA_DEVELOPERS.md
- docs/FUNCTIONALITY.md
- docs/GAFISCRIPT_EXTENSIONS.md
- docs/SCHEDULER.md
- docs/THREADING.md
- docs/SECURITY.md
- docs/EDITOR.md
- docs/FROM_COMMAND_BLOCKS.md
- docs/ARCHITECTURE.md
- docs/API_INDEX.md
- docs/LIMITATIONS.md

## Examples

The examples directory contains scripts for:
- Hello World;
- countdowns;
- player join;
- block interaction;
- area cleaning;
- boat race automation.

## Build

The intended build command is:

    ./gradlew clean build --no-daemon --max-workers=1

GitHub Actions also validates the project with Java 21 and Gradle 9.6.1.

## Project philosophy

GafiScript should feel like:

    Java
    +
    Minecraft API
    +
    IDE
    +
    Scheduler
    +
    Events
    +
    Security
    +
    Documentation

not a new pseudo-language.

Implemented functionality is documented as implemented. Future features remain explicitly marked as planned so the repository never pretends a feature exists when it does not.
