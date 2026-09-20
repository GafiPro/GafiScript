package com.gafipro.gafiscript.security;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ParseProblemException;

import java.util.List;
import java.util.Locale;

public final class ScriptSecurity {
    private static final List<String> FORBIDDEN = List.of(
            "java.io.",
            "java.net.",
            "java.nio.file.",
            "java.lang.reflect.",
            "java.lang.invoke.",
            "sun.",
            "jdk.internal.",
            "Runtime.getRuntime",
            "ProcessBuilder",
            "System.exit",
            "System.getProperty",
            "System.setProperty",
            "System.getenv",
            "Class.forName",
            "ClassLoader",
            "Unsafe",
            "MethodHandles",
            "Socket",
            "URLClassLoader"
    );

    private ScriptSecurity() {}

    public static Validation validate(String source) {
        if (source == null || source.isBlank()) {
            return Validation.fail("Script source is empty.");
        }
        if (source.length() > 120_000) {
            return Validation.fail("Script source exceeds 120,000 characters.");
        }

        String lower = source.toLowerCase(Locale.ROOT);

        // Java processes Unicode escapes before parsing. Normalize the parsed
        // source as well so a forbidden API cannot be hidden as
        // "java.lang.\\u0069nvoke" or a similar escaped identifier.
        String normalized = lower;
        try {
            normalized =
                    StaticJavaParser
                            .parse(source)
                            .toString()
                            .toLowerCase(Locale.ROOT);
        } catch (ParseProblemException ignored) {
            // The compiler will report syntax errors later; still perform the
            // raw-source checks here.
        }

        for (String forbidden : FORBIDDEN) {
            String token = forbidden.toLowerCase(Locale.ROOT);

            if (lower.contains(token) ||
                    normalized.contains(token)) {
                return Validation.fail(
                        "Blocked API/reference: " +
                                forbidden
                );
            }
        }

        if (lower.contains("package java.") || lower.contains("package javax.") ||
                lower.contains("package sun.") || lower.contains("package jdk.")) {
            return Validation.fail("System package declarations are not allowed.");
        }

        return Validation.ok();
    }

    public record Validation(boolean valid, String message) {
        static Validation ok() {
            return new Validation(true, "");
        }

        static Validation fail(String message) {
            return new Validation(false, message);
        }
    }
}
