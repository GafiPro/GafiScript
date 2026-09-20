package com.gafipro.gafiscript.runtime;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ScriptDebugInstrumentationTest {
    @Test
    void addsDebuggerProbesToStatements() {
        String source = """
                public class Main {
                    public static void start() {
                        int x = 1;
                        x++;
                    }
                }
                """;

        String instrumented =
                ScriptDebugInstrumentation.instrument(
                        source,
                        "Main"
                );

        assertTrue(instrumented.contains("GafiDebugger.check"));
        assertTrue(instrumented.contains("int x = 1"));
        assertTrue(instrumented.contains("x++"));
    }
}
