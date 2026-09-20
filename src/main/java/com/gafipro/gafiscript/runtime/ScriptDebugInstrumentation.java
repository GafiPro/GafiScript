package com.gafipro.gafiscript.runtime;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.Statement;

import java.util.ArrayList;
import java.util.List;

public final class ScriptDebugInstrumentation {
    private ScriptDebugInstrumentation() {}

    public static String instrument(
            String source,
            String scriptName
    ) {
        if (source == null ||
                source.isBlank()) {
            return source;
        }

        try {
            CompilationUnit unit =
                    StaticJavaParser.parse(source);

            List<Statement> originals =
                    new ArrayList<>(
                            unit.findAll(Statement.class)
                    );

            for (Statement statement : originals) {
                if (!statement.getBegin().isPresent()) {
                    continue;
                }

                if (statement.toString().contains(
                        "GafiDebugger.check("
                )) {
                    continue;
                }

                Node parent =
                        statement.getParentNode()
                                .orElse(null);

                if (!(parent instanceof BlockStmt block)) {
                    continue;
                }

                int line =
                        statement.getBegin()
                                .get()
                                .line;

                Statement probe =
                        StaticJavaParser.parseStatement(
                                "com.gafipro.gafiscript.api.GafiDebugger.check(" +
                                        quote(scriptName) +
                                        ", " +
                                        line +
                                        ");"
                        );

                int index =
                        block.getStatements()
                                .indexOf(statement);

                if (index >= 0) {
                    block.addStatement(
                            index,
                            probe
                    );
                }
            }

            return unit.toString();
        } catch (Throwable ignored) {
            return source;
        }
    }

    private static String quote(
            String value
    ) {
        return """ +
                value.replace(
                        "\",
                        "\\"
                ).replace(
                        """,
                        "\""
                ) +
                """;
    }
}
