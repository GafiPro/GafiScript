package com.gafipro.gafiscript.api;

import com.gafipro.gafiscript.runtime.GafiScriptContext;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public final class GafiCustomEvents {
    private final Map<String, List<Listener>> listeners =
            new ConcurrentHashMap<>();

    public GafiCustomEventHandle on(
            String event,
            Consumer<Object> listener
    ) {
        String name = normalize(event);

        Listener item =
                new Listener(
                        GafiScriptContext.currentScript(),
                        listener
                );

        List<Listener> bucket =
                listeners.computeIfAbsent(
                        name,
                        ignored ->
                                new CopyOnWriteArrayList<>()
                );

        bucket.add(item);

        return new GafiCustomEventHandle(
                item.ownerScript(),
                () -> bucket.remove(item)
        );
    }

    public int emit(
            String event,
            Object payload
    ) {
        String name = normalize(event);
        List<Listener> bucket =
                listeners.get(name);

        if (bucket == null) {
            return 0;
        }

        int count = 0;

        for (Listener listener : bucket) {
            try {
                if (listener.ownerScript() == null) {
                    listener.consumer().accept(payload);
                } else {
                    GafiScriptContext.runAs(
                            listener.ownerScript(),
                            () -> listener.consumer().accept(payload)
                    );
                }

                count++;
            } catch (Throwable throwable) {
                com.gafipro.gafiscript.GafiScriptMod.LOGGER.error(
                        "GafiScript custom event failed: {}",
                        name,
                        throwable
                );
            }
        }

        return count;
    }

    public void clearAll() {
        listeners.clear();
    }

    public void unregisterOwnedBy(
            String scriptName
    ) {
        if (scriptName == null) return;

        listeners.values().forEach(
                bucket ->
                        bucket.removeIf(
                                listener ->
                                        scriptName.equals(
                                                listener.ownerScript()
                                        )
                        )
        );
    }

    private static String normalize(String event) {
        if (event == null ||
                event.isBlank()) {
            throw new IllegalArgumentException(
                    "Custom event name cannot be empty."
            );
        }

        return event.trim().toLowerCase(
                java.util.Locale.ROOT
        );
    }

    private record Listener(
            String ownerScript,
            Consumer<Object> consumer
    ) {}
}
