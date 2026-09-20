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

    public Object invoke(String methodName, Object... arguments) throws Exception {
        Class<?>[] parameterTypes =
                new Class<?>[arguments == null ? 0 : arguments.length];

        if (arguments != null) {
            for (int i = 0; i < arguments.length; i++) {
                parameterTypes[i] =
                        arguments[i] == null
                                ? Object.class
                                : arguments[i].getClass();
            }
        }

        var method =
                mainClass.getDeclaredMethod(
                        methodName,
                        parameterTypes
                );

        if (!java.lang.reflect.Modifier.isStatic(
                method.getModifiers()
        )) {
            throw new IllegalStateException(
                    methodName +
                            "() must be static."
            );
        }

        method.setAccessible(true);

        return method.invoke(
                null,
                arguments == null
                        ? new Object[0]
                        : arguments
        );
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
