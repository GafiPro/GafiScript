package com.gafipro.gafiscript.runtime;

import com.gafipro.gafiscript.api.Gafi;
import com.gafipro.gafiscript.runtime.GafiScriptContext;
import net.minecraft.server.MinecraftServer;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public final class ScriptManager {
    private static final Map<String, ActiveScript> ACTIVE =
            new ConcurrentHashMap<>();

    private ScriptManager() {}

    public static CompletableFuture<String> runSourceAsync(
            MinecraftServer server,
            String scriptName,
            String source
    ) {
        String safeName = sanitize(scriptName);

        return Gafi.scheduler().supplyAsync(() ->
                ScriptCompiler.compile(safeName, source, server)
        ).thenCompose(result ->
                activateCompiled(server, safeName, result)
        );
    }

    public static CompletableFuture<String> runProjectAsync(
            MinecraftServer server,
            String projectName
    ) {
        String safeName = sanitize(projectName);

        return Gafi.scheduler().supplyAsync(() ->
                ScriptCompiler.compileProject(
                        server,
                        ScriptProjects.projectDirectory(
                                server,
                                safeName
                        )
                )
        ).thenCompose(result ->
                activateCompiled(server, safeName, result)
        );
    }

    private static CompletableFuture<String> activateCompiled(
            MinecraftServer server,
            String name,
            ScriptCompiler.CompilationResult result
    ) {
        if (!result.success()) {
            return CompletableFuture.completedFuture(
                    "Compile error: " + result.error()
            );
        }

        CompletableFuture<String> startResult =
                new CompletableFuture<>();

        server.execute(() -> {
            stop(name);

            ActiveScript active = new ActiveScript(
                    name,
                    result.script(),
                    result.outputDirectory()
            );

            ACTIVE.put(name, active);

            try {
                GafiScriptContext.enter(name);
                result.script().start();
                startResult.complete("Running " + name);
            } catch (Throwable throwable) {
                ACTIVE.remove(name);

                try {
                    result.script().close();
                } catch (Exception ignored) {
                }

                deleteDirectory(result.outputDirectory());

                com.gafipro.gafiscript.GafiScriptMod.LOGGER.error(
                        "[GafiScript] Runtime error in " + name,
                        throwable
                );

                Throwable cause =
                        throwable.getCause() != null
                                ? throwable.getCause()
                                : throwable;

                startResult.complete(
                        "Runtime error in " +
                        name +
                        ": " +
                        cause.getMessage()
                );
            } finally {
                GafiScriptContext.exit();
            }
        });

        return startResult;
    }

    public static String stop(String scriptName) {
        String safeName = sanitize(scriptName);
        ActiveScript active = ACTIVE.remove(safeName);

        Gafi.scheduler().cancelOwnedBy(safeName);
        Gafi.commands().unregisterOwnedBy(safeName);
        Gafi.events().unregisterOwnedBy(safeName);
        Gafi.gui().closeOwnedBy(safeName);

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

    public static String reloadFromFile(
            MinecraftServer server,
            String scriptName
    ) {
        String safeName = sanitize(scriptName);
        Path script =
                scriptsDirectory(server)
                        .resolve(safeName + ".java");

        if (!Files.isRegularFile(script)) {
            return "Script file not found: " + script;
        }

        try {
            String source =
                    Files.readString(script);

            runSourceAsync(
                    server,
                    safeName,
                    source
            );

            return "Reload scheduled: " + safeName;
        } catch (Exception exception) {
            return "Reload failed: " +
                    exception.getMessage();
        }
    }

    public static String reloadProject(
            MinecraftServer server,
            String projectName
    ) {
        String safeName = sanitize(projectName);
        Path directory =
                ScriptProjects.projectDirectory(
                        server,
                        safeName
                );

        if (!Files.isDirectory(directory)) {
            return "Project not found: " + safeName;
        }

        runProjectAsync(server, safeName);
        return "Project reload scheduled: " + safeName;
    }

    public static String createProject(
            MinecraftServer server,
            String projectName
    ) {
        try {
            Path directory =
                    ScriptProjects.ensureProject(
                            server,
                            projectName
                    );

            return "Project ready: " + directory;
        } catch (Exception exception) {
            return "Project creation failed: " +
                    exception.getMessage();
        }
    }

    public static Set<String> projects(
            MinecraftServer server
    ) {
        return Set.copyOf(
                ScriptProjects.list(server)
        );
    }

    public static String exportProject(
            MinecraftServer server,
            String projectName
    ) {
        try {
            Path archive = ScriptProjectArchive.exportProject(
                    server,
                    projectName
            );
            return "Project exported: " + archive;
        } catch (Exception exception) {
            return "Project export failed: " + exception.getMessage();
        }
    }

    public static String importProject(
            MinecraftServer server,
            String archivePath
    ) {
        try {
            Path archive = Path.of(archivePath).toAbsolutePath().normalize();
            Path allowedRoot = server.getRunDirectory()
                    .resolve("gafiscript")
                    .resolve("exports")
                    .toAbsolutePath()
                    .normalize();

            if (!archive.startsWith(allowedRoot)) {
                return "Imports are restricted to the GafiScript exports directory.";
            }

            Path project = ScriptProjectArchive.importProject(
                    server,
                    archive
            );
            return "Project imported: " + project;
        } catch (Exception exception) {
            return "Project import failed: " + exception.getMessage();
        }
    }

    public static void stopAll() {
        ACTIVE.keySet().forEach(
                ScriptManager::stop
        );
    }

    public static Set<String> activeScripts() {
        return Set.copyOf(ACTIVE.keySet());
    }

    public static Path scriptsDirectory(
            MinecraftServer server
    ) {
        Path directory =
                server.getRunDirectory()
                        .resolve("gafiscript")
                        .resolve("scripts");

        try {
            Files.createDirectories(directory);
        } catch (Exception exception) {
            throw new IllegalStateException(
                    "Could not create scripts directory.",
                    exception
            );
        }

        return directory;
    }

    private static String sanitize(String name) {
        if (name == null || name.isBlank()) {
            return "Main";
        }

        return name.replaceAll(
                "[^A-Za-z0-9_$.-]",
                "_"
        );
    }

    private static void deleteDirectory(Path root) {
        if (root == null || !Files.exists(root)) {
            return;
        }

        try (var stream = Files.walk(root)) {
            stream.sorted(
                            java.util.Comparator.reverseOrder()
                    )
                    .forEach(path -> {
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
