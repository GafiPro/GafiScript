package com.gafipro.gafiscript.runtime;

public record ScriptCheckResult(
        boolean success,
        String diagnostics
) {}
