package com.gafipro.gafiscript.runtime;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ScriptProjectTest {
    @Test
    void savesAndLoadsManifest() throws Exception {
        var directory =
                Files.createTempDirectory(
                        "gafiscript-project"
                );

        var manifest =
                directory.resolve(
                        "manifest.json"
                );

        var original =
                new ScriptProject(
                        "Example",
                        "1.2.0",
                        "Main",
                        "Gafi",
                        "Example project",
                        java.util.List.of(
                                "Core"
                        )
                );

        original.save(manifest);

        var loaded =
                ScriptProject.load(manifest);

        assertEquals(
                original.name(),
                loaded.name()
        );

        assertEquals(
                original.version(),
                loaded.version()
        );

        assertEquals(
                original.dependencies(),
                loaded.dependencies()
        );

        assertTrue(
                Files.isRegularFile(
                        manifest
                )
        );
    }
}
