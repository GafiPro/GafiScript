package com.gafipro.gafiscript.runtime;

import java.net.URLClassLoader;

public final class CompiledScript implements AutoCloseable {
    private final String name;
    private final ClassLoader classLoader;
    private final Class<?> mainClass;

    CompiledScript(String name, ClassLoader classLoader, Class<?> mainClass) {
        this.name = name;
        this.classLoader = classLoader;
        this.mainClass = mainClass;
    }

    public String name() {
        return name;
    }

    public void start() throws Exception {
        try {
            var method = mainClass.getMethod("start");
            if (!java.lang.reflect.Modifier.isStatic(method.getModifiers())) {
                throw new IllegalStateException("Script start() must be static.");
            }
            method.invoke(null);
            return;
        } catch (NoSuchMethodException ignored) {
            var method = mainClass.getMethod("main", String[].class);
            if (!java.lang.reflect.Modifier.isStatic(method.getModifiers())) {
                throw new IllegalStateException("Script main(String[]) must be static.");
            }
            method.invoke(null, (Object) new String[0]);
        }
    }

    @Override
    public void close() {
        if (classLoader instanceof URLClassLoader urlClassLoader) {
            try {
                urlClassLoader.close();
            } catch (Exception ignored) {
            }
        }
    }
}
