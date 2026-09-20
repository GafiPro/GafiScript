package com.gafipro.gafiscript.runtime;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;

public final class RestrictedClassLoader extends URLClassLoader {
    /*
     * Do not block java.lang.invoke.* here. javac uses invokedynamic and
     * LambdaMetafactory for ordinary Java lambdas and method references.
     * ScriptSecurity rejects direct source-level access to java.lang.invoke,
     * while the JVM must still be able to resolve the compiler-generated
     * lambda machinery at runtime.
     */
    private static final List<String> BLOCKED_PREFIXES = List.of(
            "java.io.",
            "java.net.",
            "java.nio.file.",
            "java.lang.reflect.",
            "sun.",
            "jdk.internal."
    );

    public RestrictedClassLoader(URL[] urls, ClassLoader parent) {
        super(urls, parent);
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve)
            throws ClassNotFoundException {

        for (String prefix : BLOCKED_PREFIXES) {
            if (name.startsWith(prefix)) {
                throw new ClassNotFoundException(
                        "Blocked class: " + name
                );
            }
        }

        if (name.equals("java.lang.Runtime")
                || name.equals("java.lang.ProcessBuilder")
                || name.equals("java.lang.ClassLoader")
                || name.equals("jdk.internal.misc.Unsafe")) {
            throw new ClassNotFoundException(
                    "Blocked class: " + name
            );
        }

        return super.loadClass(name, resolve);
    }
}
