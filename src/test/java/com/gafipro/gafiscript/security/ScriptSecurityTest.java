package com.gafipro.gafiscript.security;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ScriptSecurityTest {
    @Test
    void allowsNormalJava() {
        ScriptSecurity.Validation validation = ScriptSecurity.validate(
                "public class Main { public static void start() { int x = 1 + 2; } }"
        );
        assertTrue(validation.valid());
    }

    @Test
    void blocksProcessExecution() {
        ScriptSecurity.Validation validation = ScriptSecurity.validate(
                "public class Main { public static void start() { Runtime.getRuntime().exec(\"whoami\"); } }"
        );
        assertFalse(validation.valid());
    }

    @Test
    void blocksUnicodeEscapedForbiddenPackage() {
        String escaped =
                "import java.lang." +
                "\\u0069nvoke.MethodHandles; " +
                "public class Main {}";

        ScriptSecurity.Validation validation =
                ScriptSecurity.validate(escaped);

        assertFalse(validation.valid());
    }

    @Test
    void blocksFilesystemPackages() {
        ScriptSecurity.Validation validation = ScriptSecurity.validate(
                "import java.nio.file.Files; public class Main {}"
        );
        assertFalse(validation.valid());
    }

    @Test
    void blocksOversizedSource() {
        ScriptSecurity.Validation validation =
                ScriptSecurity.validate("a".repeat(120_001));
        assertFalse(validation.valid());
    }
}
