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

        return Gafi.scheduler()
                .supplyAsync(() -> {
                    String id =
                            "GafiRepl_" +
                                    safeSession +
                                    "_" +
                                    UUID.randomUUID()
                                            .toString()
                                            .replace("-", "");

                    ScriptCompiler.CompilationResult result =
                            ScriptCompiler.compile(
                                    id,
                                    buildExpressionSource(code),
                                    server
                            );

                    boolean statementMode = false;

                    if (!result.success()) {
                        statementMode = true;
                        result =
                                ScriptCompiler.compile(
                                        id + "_stmt",
                                        buildStatementSource(code),
                                        server
                                );
                    }

                    return new EvalResult(
                            result,
                            statementMode
                    );
                })
                .thenApply(eval -> {
                    ScriptCompiler.CompilationResult result =
                            eval.result();

                    if (!result.success()) {
                        return "REPL compile error: " +
                                result.error();
                    }

                    try {
                        Object value =
                                result.script()
                                        .invoke("eval");

                        return eval.statementMode()
                                ? "ok"
                                : value == null
                                ? "null"
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

    private static String buildExpressionSource(
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
                        return (%s);
                    }
                }
                """.formatted(expression);
    }

    private static String buildStatementSource(
            String code
    ) {
        String statement =
                code == null
                        ? ""
                        : code.trim()
                                .replace(
                                        "*/",
                                        "* /"
                                );

        return """
                public class Repl {
                    public static Object eval() throws Exception {
                        %s
                        return null;
                    }
                }
                """.formatted(statement);
    }

    private record EvalResult(
            ScriptCompiler.CompilationResult result,
            boolean statementMode
    ) {}
