package com.gafipro.gafiscript.runtime;

public final class GafiScriptContext {
    private static final ThreadLocal<String> CURRENT =
            new ThreadLocal<>();

    private GafiScriptContext() {}

    public static String currentScript() {
        return CURRENT.get();
    }

    public static void enter(String scriptName) {
        CURRENT.set(scriptName);
    }

    public static void exit() {
        CURRENT.remove();
    }

    public static void runAs(String scriptName, Runnable action) {
        String previous = CURRENT.get();
        CURRENT.set(scriptName);
        try {
            action.run();
        } finally {
            if (previous == null) {
                CURRENT.remove();
            } else {
                CURRENT.set(previous);
            }
        }
    }
}
