package com.gafipro.gafiscript.security;

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
        for (String forbidden : FORBIDDEN) {
            if (lower.contains(forbidden.toLowerCase(Locale.ROOT))) {
                return Validation.fail("Blocked API/reference: " + forbidden);
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
