package com.gafipro.gafiscript.runtime;

import net.minecraft.server.MinecraftServer;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public final class ScriptProjectArchive {
    private static final String EXTENSION = ".gafiscript";

    private ScriptProjectArchive() {}

    public static Path exportProject(
            MinecraftServer server,
            String name
    ) {
        String safe = sanitize(name);
        Path project = ScriptProjects.projectDirectory(server, safe);

        if (!Files.isDirectory(project)) {
            throw new IllegalArgumentException(
                    "Project not found: " + safe
            );
        }

        Path exportDir = server.getRunDirectory()
                .resolve("gafiscript")
                .resolve("exports");

        Path output = exportDir.resolve(safe + EXTENSION);

        try {
            Files.createDirectories(exportDir);

            try (ZipOutputStream zip =
                         new ZipOutputStream(
                                 new BufferedOutputStream(
                                         Files.newOutputStream(output)
                                 )
                         );
                 var stream = Files.walk(project)) {

                stream.filter(Files::isRegularFile)
                        .forEach(path -> {
                            String relative =
                                    project.relativize(path)
                                            .toString()
                                            .replace(
                                                    '\\',
                                                    '/'
                                            );

                            try {
                                zip.putNextEntry(
                                        new ZipEntry(relative)
                                );

                                try (InputStream input =
                                             new BufferedInputStream(
                                                     Files.newInputStream(path)
                                             )) {
                                    input.transferTo(zip);
                                }

                                zip.closeEntry();
                            } catch (IOException e) {
                                throw new ArchiveException(e);
                            }
                        });
            }

            return output;
        } catch (ArchiveException e) {
            throw new IllegalStateException(
                    "Could not export project.",
                    e.getCause()
            );
        } catch (IOException e) {
            throw new IllegalStateException(
                    "Could not export project.",
                    e
            );
        }
    }

    public static Path importProject(
            MinecraftServer server,
            Path archive
    ) {
        if (!Files.isRegularFile(archive)) {
            throw new IllegalArgumentException(
                    "Archive not found: " + archive
            );
        }

        Path projectsRoot =
                ScriptProjects.root(server);

        Path tempRoot =
                projectsRoot.resolve(
                        ".import-" +
                                java.util.UUID.randomUUID()
        );

        try {
            Files.createDirectories(tempRoot);

            try (ZipInputStream zip =
                         new ZipInputStream(
                                 new BufferedInputStream(
                                         Files.newInputStream(archive)
                                 )
                         )) {

                ZipEntry entry;

                while ((entry = zip.getNextEntry()) != null) {
                    if (entry.isDirectory()) {
                        zip.closeEntry();
                        continue;
                    }

                    Path destination =
                            tempRoot.resolve(
                                    entry.getName()
                            ).normalize();

                    if (!destination.startsWith(tempRoot)) {
                        throw new SecurityException(
                                "Archive contains an unsafe path: " +
                                        entry.getName()
                        );
                    }

                    Files.createDirectories(
                            destination.getParent()
                    );

                    try (var output =
                                 new BufferedOutputStream(
                                         Files.newOutputStream(
                                                 destination
                                         )
                                 )) {
                        zip.transferTo(output);
                    }

                    zip.closeEntry();
                }
            }

            Path manifest =
                    tempRoot.resolve("manifest.json");

            if (!Files.isRegularFile(manifest)) {
                throw new IllegalArgumentException(
                        "Archive has no manifest.json."
                );
            }

            ScriptProject project =
                    ScriptProject.load(manifest);

            String safeName =
                    sanitize(project.name());

            Path destination =
                    projectsRoot.resolve(safeName);

            if (Files.exists(destination)) {
                throw new IllegalArgumentException(
                        "Project already exists: " +
                                safeName
                );
            }

            Files.move(
                    tempRoot,
                    destination
            );

            return destination;
        } catch (IOException e) {
            deleteRecursively(tempRoot);

            throw new IllegalStateException(
                    "Could not import project.",
                    e
            );
        } catch (RuntimeException e) {
            deleteRecursively(tempRoot);
            throw e;
        }
    }

    private static String sanitize(String name) {
        String safe =
                name == null || name.isBlank()
                        ? "UnnamedProject"
                        : name;

        return safe.replaceAll(
                "[^A-Za-z0-9._-]",
                "_"
        );
    }

    private static void deleteRecursively(Path root) {
        if (!Files.exists(root)) return;

        try (var stream = Files.walk(root)) {
            stream.sorted(
                            java.util.Comparator.reverseOrder()
                    )
                    .forEach(path -> {
                        try {
                            Files.deleteIfExists(path);
                        } catch (IOException ignored) {
                        }
                    });
        } catch (IOException ignored) {
        }
    }

    private static final class ArchiveException
            extends RuntimeException {
        private ArchiveException(IOException cause) {
            super(cause);
        }
    }
}
