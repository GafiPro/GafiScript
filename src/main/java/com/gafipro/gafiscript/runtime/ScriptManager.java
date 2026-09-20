package com.gafipro.gafiscript.runtime;

import net.minecraft.server.MinecraftServer;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CompletableFuture;

public final class ScriptManager {
    private static final Map<String, ActiveScript> ACTIVE = new ConcurrentHashMap<>();

    private ScriptManager() {}

    public static CompletableFuture<String> runSourceAsync(
            MinecraftServer server,
            String scriptName,
            String source
    ) {
        String safeName = sanitize(scriptName);
        return CompletableFuture.supplyAsync(() ->
                ScriptCompiler.compile(safeName, source, server)
        ).thenCompose(result -> {
            if (!result.success()) {
                return CompletableFuture.completedFuture("Compile error: " + result.error());
            }

            stop(safeName);

            ActiveScript active = new ActiveScript(
                    safeName,
                    result.script(),
                    result.outputDirectory()
            );
            ACTIVE.put(safeName, active);

            return CompletableFuture.runAsync(() -> {
                try {
                    result.script().start();
                } catch (Throwable throwable) {
                    com.gafipro.gafiscript.GafiScriptMod.LOGGER.error(
                            "[GafiScript] Runtime error in " + safeName,
                            throwable
                    );
                }
            }).thenApply(ignored -> "Running " + safeName);
        });
    }

    public static String stop(String scriptName) {
        String safeName = sanitize(scriptName);
        ActiveScript active = ACTIVE.remove(safeName);
        if (active == null) {
            return "Script is not running: " + safeName;
        }

        try {
            active.script.close();
        } catch (Exception ignored) {
        }

        deleteDirectory(active.outputDirectory);
        return "Stopped " + safeName;
    }

    public static String reloadFromFile(MinecraftServer server, String scriptName) {
        String safeName = sanitize(scriptName);
        Path script = scriptsDirectory(server).resolve(safeName + ".java");

        if (!Files.isRegularFile(script)) {
            return "Script file not found: " + script;
        }

        try {
            String source = Files.readString(script);
            runSourceAsync(server, safeName, source);
            return "Reload scheduled: " + safeName;
        } catch (Exception e) {
            return "Reload failed: " + e.getMessage();
        }
    }

    public static java.util.Set<String> activeScripts() {
        return java.util.Set.copyOf(ACTIVE.keySet());
    }

    public static Path scriptsDirectory(MinecraftServer server) {
        Path directory = server.getRunDirectory()
                .resolve("gafiscript")
                .resolve("scripts");

        try {
            Files.createDirectories(directory);
        } catch (Exception e) {
            throw new IllegalStateException("Could not create scripts directory.", e);
        }

        return directory;
    }

    private static String sanitize(String name) {
        if (name == null || name.isBlank()) {
            return "Main";
        }
        return name.replaceAll("[^A-Za-z0-9_$]", "_");
    }

    private static void deleteDirectory(Path root) {
        if (root == null || !Files.exists(root)) return;
        try (var stream = Files.walk(root)) {
            stream.sorted(java.util.Comparator.reverseOrder()).forEach(path -> {
                try {
                    Files.deleteIfExists(path);
                } catch (Exception ignored) {
                }
            });
        } catch (Exception ignored) {
        }
    }

    private record ActiveScript(
            String name,
            CompiledScript script,
            Path outputDirectory
    ) {}
}
