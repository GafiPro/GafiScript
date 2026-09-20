package com.gafipro.gafiscript.runtime;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.Statement;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class ScriptDebugInstrumentation {
    private ScriptDebugInstrumentation() {}

    public static String instrument(
            String source,
            String scriptName
    ) {
        if (source == null || source.isBlank()) {
            return source;
        }

        try {
            CompilationUnit unit = StaticJavaParser.parse(source);
            List<Insertion> insertions = new ArrayList<>();

            for (Statement statement : unit.findAll(Statement.class)) {
                if (statement.getBegin().isEmpty()) {
                    continue;
                }

                if (statement.toString().contains("GafiDebugger.check(")) {
                    continue;
                }

                Node parent = statement.getParentNode().orElse(null);
                if (!(parent instanceof BlockStmt block)) {
                    continue;
                }

                int index = block.getStatements().indexOf(statement);
                if (index < 0) {
                    continue;
                }

                int line = statement.getBegin().get().line;
                Statement probe = StaticJavaParser.parseStatement(
                        "com.gafipro.gafiscript.api.GafiDebugger.check(" +
                                quote(scriptName) +
                                ", " +
                                line +
                                ");"
                );

                insertions.add(
                        new Insertion(
                                block,
                                index,
                                probe
                        )
                );
            }

            insertions.sort(
                    Comparator
                            .comparingInt(
                                    (Insertion value) ->
                                            System.identityHashCode(
                                                    value.block()
                                            )
                            )
                            .thenComparing(Insertion::index)
                            .reversed()
            );

            for (Insertion insertion : insertions) {
                insertion.block().addStatement(
                        insertion.index(),
                        insertion.probe()
                );
            }

            return unit.toString();
        } catch (Throwable ignored) {
            return source;
        }
    }

    private static String quote(String value) {
        String safe = value == null ? "" : value;

        return "\"" +
                safe.replace("\\", "\\\\")
                        .replace("\"", "\\\"") +
                "\"";
    }

    private record Insertion(
            BlockStmt block,
            int index,
            Statement probe
    ) {}
}
