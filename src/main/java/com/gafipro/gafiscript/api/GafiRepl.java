package com.gafipro.gafiscript.api;

import com.gafipro.gafiscript.runtime.ScriptCompiler;
import net.minecraft.server.MinecraftServer;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public final class GafiRepl {
    private GafiRepl() {}

    public static CompletableFuture<String> evaluate(
            MinecraftServer server,
            String session,
            String code
    ) {
        String safeSession =
                session == null || session.isBlank()
                        ? "repl"
                        : session.replaceAll(
                                "[^A-Za-z0-9_$-]",
                                "_"
                        );

        String source =
                buildSource(code);

        return Gafi.scheduler()
                .supplyAsync(() ->
                        ScriptCompiler.compile(
                                "GafiRepl_" +
                                        safeSession +
                                        "_" +
                                        UUID.randomUUID()
                                                .toString()
                                                .replace("-", ""),
                                source,
                                server
                        )
                )
                .thenApply(result -> {
                    if (!result.success()) {
                        return "REPL compile error: " +
                                result.error();
                    }

                    try {
                        Object value =
                                result.script()
                                        .invoke("eval");

                        return value == null
                                ? "ok"
                                : String.valueOf(value);
                    } catch (Throwable throwable) {
                        Throwable cause =
                                throwable.getCause() != null
                                        ? throwable.getCause()
                                        : throwable;

                        return "REPL runtime error: " +
                                cause.getMessage();
                    } finally {
                        try {
                            result.script().close();
                        } catch (Exception ignored) {
                        }
                    }
                });
    }

    private static String buildSource(
            String code
    ) {
        String expression =
                code == null
                        ? ""
                        : code.trim();

        if (expression.isEmpty()) {
            return "public class Repl { public static Object eval() { return null; } }";
        }

        return """
                public class Repl {
                    public static Object eval() throws Exception {
                        try {
                            return (%s);
                        } catch (Throwable expressionError) {
                            %s
                            return null;
                        }
                    }
                }
                """.formatted(
                expression,
                statementFallback(expression)
        );
    }

    private static String statementFallback(
            String expression
    ) {
        String safe =
                expression.replace(
                        "*/",
                        "* /"
                );

        return "com.gafipro.gafiscript.api.Gafi.logInfo(" +
                "String.valueOf("statement: executed"));" +
                "/* " +
                safe +
                " */";
    }
}
