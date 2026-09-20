package com.gafipro.gafiscript.runtime;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.server.MinecraftServer;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public final class ScriptStateStore {
    private static final Gson GSON =
            new GsonBuilder()
                    .setPrettyPrinting()
                    .create();

    private final Path file;
    private JsonObject root;

    public ScriptStateStore(
            MinecraftServer server,
            String scriptName
    ) {
        String safe =
                scriptName == null || scriptName.isBlank()
                        ? "Main"
                        : scriptName.replaceAll(
                                "[^A-Za-z0-9_$.-]",
                                "_"
                        );

        this.file =
                server.getRunDirectory()
                        .resolve("gafiscript")
                        .resolve("state")
                        .resolve(safe + ".json");

        this.root = load();
    }

    public synchronized void set(
            String key,
            Object value
    ) {
        String[] parts = splitKey(key);

        if (parts.length == 0) {
            throw new IllegalArgumentException(
                    "State key is empty."
            );
        }

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

        current.add(
                parts[parts.length - 1],
                GSON.toJsonTree(value)
        );

        save();
    }

    public synchronized String getString(
            String key,
            String fallback
    ) {
        var value = get(key);

        return value != null && value.isJsonPrimitive()
                ? value.getAsString()
                : fallback;
    }

    public synchronized int getInt(
            String key,
            int fallback
    ) {
        var value = get(key);

        try {
            return value != null && value.isJsonPrimitive()
                    ? value.getAsInt()
                    : fallback;
        } catch (RuntimeException ignored) {
            return fallback;
        }
    }

    public synchronized boolean getBoolean(
            String key,
            boolean fallback
    ) {
        var value = get(key);

        try {
            return value != null && value.isJsonPrimitive()
                    ? value.getAsBoolean()
                    : fallback;
        } catch (RuntimeException ignored) {
            return fallback;
        }
    }

    public synchronized void remove(
            String key
    ) {
        String[] parts = splitKey(key);

        if (parts.length == 0) {
            return;
        }

        JsonObject current = root;

        for (int i = 0; i < parts.length - 1; i++) {
            var child = current.get(parts[i]);

            if (child == null || !child.isJsonObject()) {
                return;
            }

            current = child.getAsJsonObject();
        }

        current.remove(parts[parts.length - 1]);
        save();
    }

    public synchronized void save() {
        try {
            Files.createDirectories(file.getParent());

            Files.writeString(
                    file,
                    GSON.toJson(root),
                    StandardCharsets.UTF_8
            );
        } catch (Exception exception) {
            throw new IllegalStateException(
                    "Could not save script state.",
                    exception
            );
        }
    }

    public synchronized void reload() {
        root = load();
    }

    private synchronized com.google.gson.JsonElement get(
            String key
    ) {
        String[] parts = splitKey(key);
        com.google.gson.JsonElement current = root;

        for (String part : parts) {
            if (current == null || !current.isJsonObject()) {
                return null;
            }

            current = current.getAsJsonObject().get(part);
        }

        return current;
    }

    static String[] splitKey(String key) {
        if (key == null || key.isBlank()) {
            return new String[0];
        }

        return key.trim().split("\\.", -1);
    }

    private JsonObject load() {
        try {
            Files.createDirectories(file.getParent());

            if (!Files.isRegularFile(file)) {
                return new JsonObject();
            }

            String content =
                    Files.readString(
                            file,
                            StandardCharsets.UTF_8
                    );

            if (content.isBlank()) {
                return new JsonObject();
            }

            var parsed =
                    JsonParser.parseString(content);

            return parsed.isJsonObject()
                    ? parsed.getAsJsonObject()
                    : new JsonObject();
        } catch (Exception exception) {
            throw new IllegalStateException(
                    "Could not load script state.",
                    exception
            );
        }
    }
}
