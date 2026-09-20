package com.gafipro.gafiscript.api;

import com.gafipro.gafiscript.runtime.ScriptStateStore;

public final class GafiState {
    private final ScriptStateStore store;

    public GafiState(
            ScriptStateStore store
    ) {
        this.store = store;
    }

    public void set(
            String key,
            Object value
    ) {
        store.set(key, value);
    }

    public String getString(
            String key,
            String fallback
    ) {
        return store.getString(
                key,
                fallback
        );
    }

    public int getInt(
            String key,
            int fallback
    ) {
        return store.getInt(
                key,
                fallback
        );
    }

    public boolean getBoolean(
            String key,
            boolean fallback
    ) {
        return store.getBoolean(
                key,
                fallback
        );
    }

    public void remove(
            String key
    ) {
        store.remove(key);
    }

    public void save() {
        store.save();
    }

    public void reload() {
        store.reload();
    }
}
