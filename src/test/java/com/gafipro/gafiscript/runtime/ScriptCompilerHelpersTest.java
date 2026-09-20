package com.gafipro.gafiscript.runtime;

import com.github.javaparser.StaticJavaParser;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class ScriptCompilerHelpersTest {
    @Test
    void parsesStandardJavaSyntax() {
        assertDoesNotThrow(() -> StaticJavaParser.parse("""
                import java.util.List;

                public class Main {
                    public static void start() {
                        List<String> values = List.of("A", "B");
                        values.forEach(System.out::println);
                    }
                }
                """));
    }
}
