package com.gafipro.gafiscript.api;

import net.minecraft.server.MinecraftServer;

import com.google.gson.JsonElement;

public final class GafiDatabase {
    private final GafiStorage backend;

    GafiDatabase(MinecraftServer server, String namespace) {
        this.backend = new GafiStorage(
                server,
                "database/" + (namespace == null ? "default" : namespace)
        );
    }

    public void set(String key, Object value) {
        backend.set(key, value);
    }

    public String getString(String key, String fallback) {
        return backend.getString(key, fallback);
    }

    public int getInt(String key, int fallback) {
        return backend.getInt(key, fallback);
    }

    public JsonElement get(String key) {
        return backend.get(key);
    }

    public void remove(String key) {
        backend.remove(key);
    }

    public void reload() {
        backend.reload();
    }

    public void save() {
        backend.save();
    }
}
