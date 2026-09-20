package com.gafipro.gafiscript.runtime;

import com.github.javaparser.ParseProblemException;
import com.github.javaparser.StaticJavaParser;
import com.gafipro.gafiscript.GafiScriptMod;
import com.gafipro.gafiscript.security.ScriptSecurity;
import net.minecraft.server.MinecraftServer;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.StandardLocation;
import javax.tools.ToolProvider;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.Collectors;

public final class ScriptCompiler {
    private ScriptCompiler() {}

    public static CompilationResult compile(
            String scriptName,
            String source,
            MinecraftServer server
    ) {
        ScriptSecurity.Validation security = ScriptSecurity.validate(source);
        if (!security.valid()) {
            return CompilationResult.failure(security.message());
        }

        try {
            StaticJavaParser.parse(source);
        } catch (ParseProblemException exception) {
            return CompilationResult.failure(
                    exception.getProblems().stream()
                            .map(Object::toString)
                            .collect(Collectors.joining("\n"))
            );
        }

        String className = findMainClass(source, scriptName);
        return compileUnits(
                server,
                List.of(new SourceUnit(className, source)),
                className
        );
    }

    public static CompilationResult compileProject(
            MinecraftServer server,
            Path projectDirectory
    ) {
        if (!Files.isDirectory(projectDirectory)) {
            return CompilationResult.failure(
                    "Project directory does not exist: " + projectDirectory
            );
        }

        Path manifest = projectDirectory.resolve("manifest.json");
        if (!Files.isRegularFile(manifest)) {
            return CompilationResult.failure(
                    "Project manifest.json is missing."
            );
        }

        final ScriptProject project;
        try {
            project = ScriptProject.load(manifest);
        } catch (Exception exception) {
            return CompilationResult.failure(exception.getMessage());
        }

        Path sourceDirectory = projectDirectory.resolve("src");

        if (!Files.isDirectory(sourceDirectory)) {
            return CompilationResult.failure(
                    "Project src/ directory is missing."
            );
        }

        List<SourceUnit> units = new ArrayList<>();

        try (var stream = Files.walk(sourceDirectory)) {
            List<Path> javaFiles = stream
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".java"))
                    .sorted()
                    .toList();

            if (javaFiles.isEmpty()) {
                return CompilationResult.failure(
                        "Project contains no Java source files."
                );
            }

            for (Path file : javaFiles) {
                String source = Files.readString(
                        file,
                        StandardCharsets.UTF_8
                );

                ScriptSecurity.Validation security =
                        ScriptSecurity.validate(source);

                if (!security.valid()) {
                    return CompilationResult.failure(
                            file.getFileName() + ": " +
                            security.message()
                    );
                }

                try {
                    StaticJavaParser.parse(source);
                } catch (ParseProblemException exception) {
                    return CompilationResult.failure(
                            file.getFileName() + ": " +
                            exception.getProblems().stream()
                                    .map(Object::toString)
                                    .collect(Collectors.joining("\n"))
                    );
                }

                units.add(
                        new SourceUnit(
                                findMainClass(
                                        source,
                                        file.getFileName()
                                                .toString()
                                                .replaceFirst(
                                                        "\\.java$",
                                                        ""
                                                )
                                ),
                                source
                        )
                );
            }
        } catch (Exception exception) {
            return CompilationResult.failure(
                    "Could not read project sources: " +
                    exception.getMessage()
            );
        }

        return compileUnits(
                server,
                units,
                project.mainClass()
        );
    }

    private static CompilationResult compileUnits(
            MinecraftServer server,
            List<SourceUnit> units,
            String mainClass
    ) {
        Path outputDirectory = server.getRunDirectory()
                .resolve("gafiscript")
                .resolve("compiled")
                .resolve(UUID.randomUUID().toString());

        try {
            Files.createDirectories(outputDirectory);
        } catch (IOException exception) {
            return CompilationResult.failure(
                    "Could not create compiler output directory: " +
                    exception.getMessage()
            );
        }

        JavaCompiler compiler = getCompiler();

        if (compiler == null) {
            return CompilationResult.failure(
                    "No Java compiler is available."
            );
        }

        DiagnosticCollector<JavaFileObject> diagnostics =
                new DiagnosticCollector<>();

        try (StandardJavaFileManager fileManager =
                     compiler.getStandardFileManager(
                             diagnostics,
                             Locale.ROOT,
                             StandardCharsets.UTF_8
                     )) {

            fileManager.setLocation(
                    StandardLocation.CLASS_OUTPUT,
                    List.of(outputDirectory.toFile())
            );

            List<String> options = List.of(
                    "-encoding", "UTF-8",
                    "-g",
                    "-proc:none",
                    "-parameters",
                    "-source", "21",
                    "-target", "21",
                    "-classpath", buildClassPath()
            );

            List<JavaFileObject> sourceObjects = units.stream()
                    .map(unit ->
                            (JavaFileObject) new SourceFileObject(
                                    unit.binaryName(),
                                    unit.source()
                            )
                    )
                    .toList();

            JavaCompiler.CompilationTask task =
                    compiler.getTask(
                            null,
                            fileManager,
                            diagnostics,
                            options,
                            null,
                            sourceObjects
                    );

            Boolean success = task.call();

            if (!Boolean.TRUE.equals(success)) {
                return CompilationResult.failure(
                        formatDiagnostics(diagnostics)
                );
            }

            var bytecodeValidation =
                    com.gafipro.gafiscript.security.ScriptBytecodeValidator
                            .validateDirectory(outputDirectory);

            if (!bytecodeValidation.valid()) {
                deleteRecursively(outputDirectory);
                return CompilationResult.failure(
                        "Security validation failed: " +
                                bytecodeValidation.message()
                );
            }

            RestrictedClassLoader classLoader =
                    new RestrictedClassLoader(
                            new URL[]{outputDirectory.toUri().toURL()},
                            GafiScriptMod.class.getClassLoader()
                    );

            Class<?> main =
                    Class.forName(mainClass, true, classLoader);

            return CompilationResult.success(
                    new CompiledScript(
                            mainClass,
                            classLoader,
                            main
                    ),
                    outputDirectory
            );
        } catch (Throwable throwable) {
            try {
                deleteRecursively(outputDirectory);
            } catch (IOException ignored) {
            }

            return CompilationResult.failure(
                    throwable.getClass().getSimpleName() +
                    ": " +
                    throwable.getMessage()
            );
        }
    }

    private static JavaCompiler getCompiler() {
        JavaCompiler system = ToolProvider.getSystemJavaCompiler();

        if (system != null) {
            return system;
        }

        try {
            Class<?> type = Class.forName(
                    "org.eclipse.jdt.internal.compiler.tool.EclipseCompiler"
            );

            return (JavaCompiler)
                    type.getDeclaredConstructor().newInstance();
        } catch (Throwable ignored) {
            return null;
        }
    }

    private static String findMainClass(
            String source,
            String fallback
    ) {
        var packageMatcher = java.util.regex.Pattern
                .compile(
                        "\\bpackage\\s+([A-Za-z_$][A-Za-z0-9_$.]*)\\s*;"
                )
                .matcher(source);

        String packageName =
                packageMatcher.find()
                        ? packageMatcher.group(1)
                        : "";

        var matcher = java.util.regex.Pattern
                .compile(
                        "\\bpublic\\s+" +
                        "(?:final\\s+|abstract\\s+)?" +
                        "class\\s+" +
                        "([A-Za-z_$][A-Za-z0-9_$]*)"
                )
                .matcher(source);

        if (!matcher.find()) {
            matcher = java.util.regex.Pattern
                    .compile(
                            "\\bclass\\s+" +
                            "([A-Za-z_$][A-Za-z0-9_$]*)"
                    )
                    .matcher(source);
        }

        String className = matcher.find()
                ? matcher.group(1)
                : fallback.replaceAll(
                        "[^A-Za-z0-9_$]",
                        "_"
                );

        return packageName.isBlank()
                ? className
                : packageName + "." + className;
    }

    private static String buildClassPath() {
        List<String> entries = new ArrayList<>();

        String systemClassPath =
                System.getProperty("java.class.path", "");

        if (!systemClassPath.isBlank()) {
            entries.add(systemClassPath);
        }

        for (Class<?> type : List.of(
                GafiScriptMod.class,
                com.gafipro.gafiscript.api.Gafi.class,
                net.minecraft.server.MinecraftServer.class
        )) {
            try {
                URL url =
                        type.getProtectionDomain()
                                .getCodeSource()
                                .getLocation();

                entries.add(
                        Path.of(url.toURI()).toString()
                );
            } catch (Exception ignored) {
            }
        }

        return String.join(
                File.pathSeparator,
                entries
        );
    }

    private static String formatDiagnostics(
            DiagnosticCollector<JavaFileObject> diagnostics
    ) {
        if (diagnostics.getDiagnostics().isEmpty()) {
            return "Java compilation failed without diagnostics.";
        }

        StringBuilder builder = new StringBuilder();

        for (Diagnostic<? extends JavaFileObject> diagnostic :
                diagnostics.getDiagnostics()) {

            builder.append(diagnostic.getKind())
                    .append(" line ")
                    .append(diagnostic.getLineNumber())
                    .append(", column ")
                    .append(diagnostic.getColumnNumber())
                    .append(": ")
                    .append(diagnostic.getMessage(Locale.ROOT))
                    .append('\n');
        }

        return builder.toString().trim();
    }

    private static void deleteRecursively(Path root)
            throws IOException {
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
        }
    }

    private record SourceUnit(
            String binaryName,
            String source
    ) {}

    public record CompilationResult(
            boolean success,
            CompiledScript script,
            Path outputDirectory,
            String error
    ) {
        static CompilationResult success(
                CompiledScript script,
                Path outputDirectory
        ) {
            return new CompilationResult(
                    true,
                    script,
                    outputDirectory,
                    ""
            );
        }

        static CompilationResult failure(String error) {
            return new CompilationResult(
                    false,
                    null,
                    null,
                    error == null
                            ? "Unknown compilation error."
                            : error
            );
        }
    }

    private static final class SourceFileObject
            extends javax.tools.SimpleJavaFileObject {

        private final String source;

        private SourceFileObject(
                String className,
                String source
        ) {
            super(
                    java.net.URI.create(
                            "string:///" +
                            className.replace(
                                    '.',
                                    '/'
                            ) +
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
