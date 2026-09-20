package com.gafipro.gafiscript.api;

import net.minecraft.server.MinecraftServer;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.charset.StandardCharsets;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public final class GafiConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private final Path file;
    private JsonObject root;

    GafiConfig(MinecraftServer server, String namespace) {
        String safe = namespace == null || namespace.isBlank()
                ? "default"
                : namespace.replaceAll("[^A-Za-z0-9._-]", "_");

        this.file = server.getRunDirectory()
                .resolve("gafiscript")
                .resolve("configs")
                .resolve(safe + ".json");

        this.root = load();
    }

    public synchronized String getString(String path, String defaultValue) {
        var element = get(path);
        return element != null && element.isJsonPrimitive()
                ? element.getAsString()
                : defaultValue;
    }

    public synchronized int getInt(String path, int defaultValue) {
        try {
            var element = get(path);
            return element != null && element.isJsonPrimitive()
                    ? element.getAsInt()
                    : defaultValue;
        } catch (RuntimeException ignored) {
            return defaultValue;
        }
    }

    public synchronized boolean getBoolean(String path, boolean defaultValue) {
        try {
            var element = get(path);
            return element != null && element.isJsonPrimitive()
                    ? element.getAsBoolean()
                    : defaultValue;
        } catch (RuntimeException ignored) {
            return defaultValue;
        }
    }

    public synchronized void set(String path, Object value) {
        GafiStorageBridge.set(root, path, GSON.toJsonTree(value));
        save();
    }

    public synchronized void reload() {
        root = load();
    }

    public synchronized void save() {
        try {
            Files.createDirectories(file.getParent());
            Files.writeString(file, GSON.toJson(root), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new IllegalStateException("Could not save config: " + file, e);
        }
    }

    public Path file() {
        return file;
    }

    private com.google.gson.JsonElement get(String path) {
        String[] parts = path == null ? new String[0] : path.trim().split("\\.");
        com.google.gson.JsonElement current = root;

        for (String part : parts) {
            if (part.isEmpty() || current == null || !current.isJsonObject()) return null;
            current = current.getAsJsonObject().get(part);
        }

        return current;
    }

    private JsonObject load() {
        try {
            Files.createDirectories(file.getParent());
            if (!Files.isRegularFile(file)) return new JsonObject();

            String content = Files.readString(file, StandardCharsets.UTF_8);
            if (content.isBlank()) return new JsonObject();

            var parsed = JsonParser.parseString(content);
            return parsed.isJsonObject() ? parsed.getAsJsonObject() : new JsonObject();
        } catch (Exception e) {
            throw new IllegalStateException("Could not load config: " + file, e);
        }
    }

    private static final class GafiStorageBridge {
        private static void set(JsonObject root, String path, com.google.gson.JsonElement value) {
            String[] parts = path == null ? new String[0] : path.trim().split("\\.");
            if (parts.length == 0) throw new IllegalArgumentException("Config path is empty.");

            JsonObject current = root;
            for (int i = 0; i < parts.length - 1; i++) {
                var child = current.get(parts[i]);
                if (child == null || !child.isJsonObject()) {
                    JsonObject created = new JsonObject();
                    current.add(parts[i], created);
                    current = created;
                } else {
                    current = child.getAsJsonObject();
                }
            }

            current.add(parts[parts.length - 1], value);
        }
    }
}
