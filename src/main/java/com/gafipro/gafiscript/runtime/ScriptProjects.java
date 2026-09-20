package com.gafipro.gafiscript.runtime;

import net.minecraft.server.MinecraftServer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class ScriptProjects {
    private ScriptProjects() {}

    public static Path root(MinecraftServer server) {
        return server.getRunDirectory()
                .resolve("gafiscript")
                .resolve("projects");
    }

    public static Path projectDirectory(
            MinecraftServer server,
            String name
    ) {
        String safe =
                name == null || name.isBlank()
                        ? "UnnamedProject"
                        : name.replaceAll(
                                "[^A-Za-z0-9._-]",
                                "_"
                        );

        return root(server).resolve(safe);
    }

    public static Path manifest(
            MinecraftServer server,
            String name
    ) {
        return projectDirectory(
                server,
                name
        ).resolve("manifest.json");
    }

    public static List<String> list(
            MinecraftServer server
    ) {
        Path root = root(server);

        try {
            Files.createDirectories(root);

            try (var stream =
                         Files.list(root)) {

                return stream
                        .filter(Files::isDirectory)
                        .filter(path ->
                                Files.isRegularFile(
                                        path.resolve(
                                                "manifest.json"
                                        )
                                )
                        )
                        .map(path ->
                                path.getFileName()
                                        .toString()
                        )
                        .sorted()
                        .toList();
            }
        } catch (Exception e) {
            throw new IllegalStateException(
                    "Could not list GafiScript projects.",
                    e
            );
        }
    }

    public static ScriptProject load(
            MinecraftServer server,
            String name
    ) {
        Path manifest =
                manifest(server, name);

        if (!Files.isRegularFile(manifest)) {
            throw new IllegalArgumentException(
                    "Project not found: " + name
            );
        }

        return ScriptProject.load(
                manifest
        );
    }

    public static Path ensureProject(
            MinecraftServer server,
            String name
    ) {
        Path directory =
                projectDirectory(
                        server,
                        name
                );

        try {
            Files.createDirectories(
                    directory.resolve("src")
            );
            Files.createDirectories(
                    directory.resolve("libs")
            );

            Path manifest =
                    directory.resolve(
                            "manifest.json"
                    );

            if (!Files.exists(manifest)) {
                ScriptProject
                        .createDefault(name)
                        .save(manifest);

                Path main =
                        directory
                                .resolve("src")
                                .resolve("Main.java");

                if (!Files.exists(main)) {
                    Files.writeString(
                            main,
                            "import static " +
                            "com.gafipro.gafiscript.api.Gafi.*;\n\n" +
                            "public class Main {\n" +
                            "    public static void start() {\n" +
                            "        broadcast(\"Hello from GafiScript project!\");\n" +
                            "    }\n" +
                            "}\n"
                    );
                }
            }

            return directory;
        } catch (Exception e) {
            throw new IllegalStateException(
                    "Could not create project: " + name,
                    e
            );
        }
    }

    public static DependencySet resolveDependencies(
            MinecraftServer server,
            String projectName
    ) {
        List<Path> sources =
                new ArrayList<>();

        List<Path> jars =
                new ArrayList<>();

        resolveRecursive(
                server,
                projectName,
                new HashSet<>(),
                sources,
                jars
        );

        return new DependencySet(
                List.copyOf(sources),
                List.copyOf(jars)
        );
    }

    private static void resolveRecursive(
            MinecraftServer server,
            String projectName,
            Set<String> visiting,
            List<Path> sources,
            List<Path> jars
    ) {
        String safe =
                projectName == null ||
                        projectName.isBlank()
                        ? "UnnamedProject"
                        : projectName.replaceAll(
                                "[^A-Za-z0-9._-]",
                                "_"
                        );

        if (!visiting.add(safe)) {
            throw new IllegalStateException(
                    "Circular project dependency: " +
                            safe
            );
        }

        ScriptProject project =
                load(
                        server,
                        safe
                );

        Path directory =
                projectDirectory(
                        server,
                        safe
                );

        Path sourceDirectory =
                directory.resolve("src");

        if (Files.isDirectory(sourceDirectory)) {
            try (var stream =
                         Files.walk(sourceDirectory)) {

                stream.filter(Files::isRegularFile)
                        .filter(path ->
                                path.toString()
                                        .endsWith(".java")
                        )
                        .sorted()
                        .forEach(sources::add);

            } catch (IOException e) {
                throw new IllegalStateException(
                        "Could not read project sources: " +
                                safe,
                        e
                );
            }
        }

        Path libs =
                directory.resolve("libs");

        if (Files.isDirectory(libs)) {
            try (var stream =
                         Files.list(libs)) {

                stream.filter(Files::isRegularFile)
                        .filter(path ->
                                path.toString()
                                        .endsWith(".jar")
                        )
                        .sorted()
                        .forEach(jars::add);

            } catch (IOException e) {
                throw new IllegalStateException(
                        "Could not read project libraries: " +
                                safe,
                        e
                );
            }
        }

        for (String dependency :
                project.dependencies()) {

            resolveRecursive(
                    server,
                    dependency,
                    visiting,
                    sources,
                    jars
            );
        }

        visiting.remove(safe);
    }

    public record DependencySet(
            List<Path> sourceFiles,
            List<Path> libraryJars
    ) {}

    public static List<Path> javaFiles(
            MinecraftServer server,
            String projectName
    ) {
        return resolveDependencies(
                server,
                projectName
        ).sourceFiles();
    }

    public static List<Path> libraryJars(
            MinecraftServer server,
            String projectName
    ) {
        return resolveDependencies(
                server,
                projectName
        ).libraryJars();
    }
}
