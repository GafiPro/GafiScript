package com.gafipro.gafiscript.runtime;

import com.gafipro.gafiscript.security.ScriptBytecodeValidator;
import org.junit.jupiter.api.Test;

import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class GafiScriptAuditTest {

    @Test
    void compilerGeneratedLambdaBytecodePassesSecurityValidation() throws Exception {
        JavaCompiler compiler = requireCompiler();
        Path output = Files.createTempDirectory("gafiscript-lambda-test-");

        try {
            String source = """
                    public class Main {
                        public static Runnable make() {
                            return () -> {};
                        }
                    }
                    """;

            DiagnosticCollector<JavaFileObject> diagnostics =
                    new DiagnosticCollector<>();

            try (StandardJavaFileManager fileManager =
                         compiler.getStandardFileManager(
                                 diagnostics,
                                 Locale.ROOT,
                                 StandardCharsets.UTF_8
                         )) {

                fileManager.setLocation(
                        javax.tools.StandardLocation.CLASS_OUTPUT,
                        List.of(output.toFile())
                );

                JavaCompiler.CompilationTask task =
                        compiler.getTask(
                                null,
                                fileManager,
                                diagnostics,
                                List.of(
                                        "-proc:none",
                                        "-encoding", "UTF-8",
                                        "-source", "21",
                                        "-target", "21"
                                ),
                                null,
                                List.of(new SourceObject("Main", source))
                        );

                assertTrue(
                        Boolean.TRUE.equals(task.call()),
                        formatDiagnostics(diagnostics)
                );
            }

            ScriptBytecodeValidator.Validation validation =
                    ScriptBytecodeValidator.validateDirectory(output);

            assertTrue(validation.valid(), validation.message());

            try (RestrictedClassLoader loader =
                         new RestrictedClassLoader(
                                 new URL[]{output.toUri().toURL()},
                                 getClass().getClassLoader()
                         )) {
                Class<?> main =
                        loader.loadClass("Main");

                Object runnable =
                        main.getMethod("make").invoke(null);

                assertNotNull(runnable);
                assertTrue(runnable instanceof Runnable);

                assertDoesNotThrow(
                        () -> ((Runnable) runnable).run()
                );
            }
        } finally {
            deleteTree(output);
        }
    }

    @Test
    void restrictedClassLoaderAllowsLambdaRuntimeButStillBlocksDangerousApis()
            throws Exception {

        RestrictedClassLoader loader =
                new RestrictedClassLoader(
                        new URL[0],
                        getClass().getClassLoader()
                );

        try {
            // The JVM must still be able to resolve its own lambda runtime.
            // Direct user access to java.lang.invoke remains blocked by
            // ScriptSecurity before compilation.
            assertDoesNotThrow(() ->
                    Class.forName(
                            "java.lang.invoke.LambdaMetafactory",
                            false,
                            loader
                    )
            );

            assertThrows(
                    ClassNotFoundException.class,
                    () -> Class.forName(
                            "java.lang.Runtime",
                            false,
                            loader
                    )
            );

            assertThrows(
                    ClassNotFoundException.class,
                    () -> Class.forName(
                            "java.nio.file.Files",
                            false,
                            loader
                    )
            );
        } finally {
            loader.close();
        }
    }

    @Test
    void repositoryExamplesCompile() throws Exception {
        Path examples = Path.of("examples");

        assertTrue(
                Files.isDirectory(examples),
                "examples directory is missing"
        );

        List<Path> sources;
        try (var stream = Files.walk(examples)) {
            sources = stream
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".java"))
                    .sorted()
                    .toList();
        }

        assertFalse(sources.isEmpty(), "No GafiScript examples found.");

        JavaCompiler compiler = requireCompiler();
        Path output =
                Files.createTempDirectory(
                        "gafiscript-examples-test-"
                );

        try {
            DiagnosticCollector<JavaFileObject> diagnostics =
                    new DiagnosticCollector<>();

            List<JavaFileObject> units =
                    new java.util.ArrayList<>();

            for (Path sourcePath : sources) {
                String source =
                        Files.readString(
                                sourcePath,
                                StandardCharsets.UTF_8
                        );

                String className =
                        findPrimaryTypeName(
                                source,
                                sourcePath.getFileName()
                                        .toString()
                                        .replaceFirst(
                                                "\\.java$",
                                                ""
                                        )
                        );

                units.add(
                        new SourceObject(
                                className,
                                source
                        )
                );
            }

            try (StandardJavaFileManager fileManager =
                         compiler.getStandardFileManager(
                                 diagnostics,
                                 Locale.ROOT,
                                 StandardCharsets.UTF_8
                         )) {

                fileManager.setLocation(
                        javax.tools.StandardLocation.CLASS_OUTPUT,
                        List.of(output.toFile())
                );

                String classPath = buildCompilerClassPath();

                JavaCompiler.CompilationTask task =
                        compiler.getTask(
                                null,
                                fileManager,
                                diagnostics,
                                List.of(
                                        "-proc:none",
                                        "-encoding", "UTF-8",
                                        "-source", "21",
                                        "-target", "21",
                                        "-classpath", classPath
                                ),
                                null,
                                units
                        );

                assertTrue(
                        Boolean.TRUE.equals(task.call()),
                        formatDiagnostics(diagnostics) +
                                "\nExamples: " +
                                sources
                );
            }
        } finally {
            deleteTree(output);
        }
    }

    private static String findPrimaryTypeName(
            String source,
            String fallback
    ) {
        var matcher =
                java.util.regex.Pattern
                        .compile(
                                "\\bpublic\\s+(?:final\\s+|abstract\\s+)?" +
                                "(?:class|interface|enum|record)\\s+" +
                                "([A-Za-z_$][A-Za-z0-9_$]*)"
                        )
                        .matcher(source);

        if (!matcher.find()) {
            matcher =
                    java.util.regex.Pattern
                            .compile(
                                    "\\b(?:class|interface|enum|record)\\s+" +
                                    "([A-Za-z_$][A-Za-z0-9_$]*)"
                            )
                            .matcher(source);
        }

        return matcher.find()
                ? matcher.group(1)
                : fallback;
    }

    private static JavaCompiler requireCompiler() {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        assertNotNull(
                compiler,
                "A JDK compiler is required to run the GafiScript audit tests."
        );
        return compiler;
    }

    private static String buildCompilerClassPath() {
        java.util.LinkedHashSet<String> entries =
                new java.util.LinkedHashSet<>();

        String configuredClassPath =
                System.getProperty(
                        "gafiscript.test.classpath",
                        ""
                );

        if (!configuredClassPath.isBlank()) {
            for (String entry :
                    configuredClassPath.split(
                            java.io.File.pathSeparator
                    )) {
                if (!entry.isBlank()) {
                    entries.add(entry);
                }
            }
        }

        String systemClassPath =
                System.getProperty("java.class.path", "");

        if (!systemClassPath.isBlank()) {
            for (String entry :
                    systemClassPath.split(java.io.File.pathSeparator)) {
                if (!entry.isBlank()) {
                    entries.add(entry);
                }
            }
        }

        for (Class<?> type : List.of(
                GafiScriptAuditTest.class,
                com.gafipro.gafiscript.api.Gafi.class,
                net.minecraft.server.MinecraftServer.class,
                net.fabricmc.loader.api.FabricLoader.class
        )) {
            try {
                URL location =
                        type.getProtectionDomain()
                                .getCodeSource()
                                .getLocation();
                entries.add(
                        Path.of(location.toURI())
                                .toAbsolutePath()
                                .toString()
                );
            } catch (Exception ignored) {
            }
        }

        return String.join(
                java.io.File.pathSeparator,
                entries
        );
    }

    private static String formatDiagnostics(
            DiagnosticCollector<JavaFileObject> diagnostics
    ) {
        return diagnostics.getDiagnostics()
                .stream()
                .map(diagnostic ->
                        diagnostic.getKind() +
                        " line " +
                        diagnostic.getLineNumber() +
                        ", column " +
                        diagnostic.getColumnNumber() +
                        ": " +
                        diagnostic.getMessage(Locale.ROOT)
                )
                .collect(Collectors.joining("\n"));
    }

    private static void deleteTree(Path root) throws Exception {
        if (root == null || !Files.exists(root)) {
            return;
        }

        try (var stream = Files.walk(root)) {
            stream.sorted(java.util.Comparator.reverseOrder())
                    .forEach(path -> {
                        try {
                            Files.deleteIfExists(path);
                        } catch (Exception ignored) {
                        }
                    });
        }
    }

    private static final class SourceObject extends SimpleJavaFileObject {
        private final String source;

        private SourceObject(String className, String source) {
            super(
                    URI.create(
                            "string:///" +
                            className +
                            Kind.SOURCE.extension
                    ),
                    Kind.SOURCE
            );
            this.source = source;
        }

        @Override
        public CharSequence getCharContent(
                boolean ignoreEncodingErrors
        ) {
            return source;
        }
    }
}
