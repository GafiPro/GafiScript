package com.gafipro.gafiscript.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonParser;
import com.google.gson.JsonArray;
import net.minecraft.server.MinecraftServer;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Objects;

public final class GafiStorage {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private final Path file;
    private JsonObject root;

    GafiStorage(MinecraftServer server, String namespace) {
        this.file = server.getRunDirectory()
                .resolve("gafiscript")
                .resolve("storage")
                .resolve(safeName(namespace) + ".json");
        this.root = load();
    }

    public synchronized boolean has(String path) {
        return find(path) != null;
    }

    public synchronized String getString(String path, String defaultValue) {
        JsonElement element = find(path);
        return element != null && element.isJsonPrimitive()
                ? element.getAsString()
                : defaultValue;
    }

    public synchronized int getInt(String path, int defaultValue) {
        JsonElement element = find(path);
        try {
            return element != null && element.isJsonPrimitive()
                    ? element.getAsInt()
                    : defaultValue;
        } catch (RuntimeException ignored) {
            return defaultValue;
        }
    }

    public synchronized long getLong(String path, long defaultValue) {
        JsonElement element = find(path);
        try {
            return element != null && element.isJsonPrimitive()
                    ? element.getAsLong()
                    : defaultValue;
        } catch (RuntimeException ignored) {
            return defaultValue;
        }
    }

    public synchronized double getDouble(String path, double defaultValue) {
        JsonElement element = find(path);
        try {
            return element != null && element.isJsonPrimitive()
                    ? element.getAsDouble()
                    : defaultValue;
        } catch (RuntimeException ignored) {
            return defaultValue;
        }
    }

    public synchronized boolean getBoolean(String path, boolean defaultValue) {
        JsonElement element = find(path);
        try {
            return element != null && element.isJsonPrimitive()
                    ? element.getAsBoolean()
                    : defaultValue;
        } catch (RuntimeException ignored) {
            return defaultValue;
        }
    }

    public synchronized JsonElement get(String path) {
        JsonElement element = find(path);
        return element == null ? null : element.deepCopy();
    }

    public synchronized void set(String path, Object value) {
        Objects.requireNonNull(path, "path");
        JsonElement element = GSON.toJsonTree(value);
        put(path, element == null ? null : element);
        save();
    }

    public synchronized void setJson(String path, JsonElement value) {
        put(path, value == null ? null : value.deepCopy());
        save();
    }

    public synchronized void remove(String path) {
        String[] parts = split(path);
        if (parts.length == 0) return;

        JsonObject parent = root;
        for (int i = 0; i < parts.length - 1; i++) {
            JsonElement next = parent.get(parts[i]);
            if (next == null || !next.isJsonObject()) return;
            parent = next.getAsJsonObject();
        }

        parent.remove(parts[parts.length - 1]);
        save();
    }

    public synchronized JsonObject snapshot() {
        return root.deepCopy();
    }

    public synchronized void reload() {
        root = load();
    }

    public synchronized void save() {
        try {
            Files.createDirectories(file.getParent());
            Files.writeString(
                    file,
                    GSON.toJson(root),
                    StandardCharsets.UTF_8
            );
        } catch (IOException e) {
            throw new IllegalStateException(
                    "Could not save GafiScript storage: " + file,
                    e
            );
        }
    }

    public Path file() {
        return file;
    }

    private JsonObject load() {
        try {
            Files.createDirectories(file.getParent());
            if (!Files.isRegularFile(file)) {
                return new JsonObject();
            }

            String content = Files.readString(file, StandardCharsets.UTF_8);
            if (content.isBlank()) {
                return new JsonObject();
            }

            JsonElement parsed = JsonParser.parseString(content);
            return parsed.isJsonObject()
                    ? parsed.getAsJsonObject()
                    : new JsonObject();
        } catch (Exception e) {
            throw new IllegalStateException(
                    "Could not load GafiScript storage: " + file,
                    e
            );
        }
    }

    private JsonElement find(String path) {
        String[] parts = split(path);
        if (parts.length == 0) return null;

        JsonElement current = root;
        for (String part : parts) {
            if (current == null || !current.isJsonObject()) return null;
            current = current.getAsJsonObject().get(part);
        }
        return current;
    }

    private void put(String path, JsonElement value) {
        String[] parts = split(path);
        if (parts.length == 0) {
            throw new IllegalArgumentException("Storage path is empty.");
        }

        JsonObject current = root;
        for (int i = 0; i < parts.length - 1; i++) {
            JsonElement child = current.get(parts[i]);
            if (child == null || !child.isJsonObject()) {
                JsonObject created = new JsonObject();
                current.add(parts[i], created);
                current = created;
            } else {
                current = child.getAsJsonObject();
            }
        }

        String leaf = parts[parts.length - 1];
        if (value == null || value.isJsonNull()) {
            current.remove(leaf);
        } else {
            current.add(leaf, value);
        }
    }

    private static String[] split(String path) {
        return path == null
                ? new String[0]
                : path.trim().split("\\.");
    }

    private static String safeName(String input) {
        String cleaned = input == null ? "default" : input.trim();
        if (cleaned.isEmpty()) cleaned = "default";
        return cleaned.replaceAll("[^A-Za-z0-9._-]", "_");
    }
}
