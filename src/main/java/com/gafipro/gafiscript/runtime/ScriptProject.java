package com.gafipro.gafiscript.runtime;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public record ScriptProject(
        String name,
        String version,
        @SerializedName("main") String mainClass,
        String author,
        String description,
        List<String> dependencies
) {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public ScriptProject {
        name = name == null || name.isBlank() ? "UnnamedProject" : name;
        version = version == null || version.isBlank() ? "1.0.0" : version;
        mainClass = mainClass == null || mainClass.isBlank() ? "Main" : mainClass;
        author = author == null ? "" : author;
        description = description == null ? "" : description;
        dependencies = dependencies == null ? List.of() : List.copyOf(dependencies);
    }

    public static ScriptProject load(Path manifest) {
        try {
            return GSON.fromJson(Files.readString(manifest), ScriptProject.class);
        } catch (Exception e) {
            throw new IllegalStateException("Invalid GafiScript manifest: " + manifest, e);
        }
    }

    public void save(Path manifest) {
        try {
            Files.createDirectories(manifest.getParent());
            Files.writeString(manifest, GSON.toJson(this));
        } catch (Exception e) {
            throw new IllegalStateException("Could not write manifest: " + manifest, e);
        }
    }

    public static ScriptProject createDefault(String name) {
        return new ScriptProject(
                name,
                "1.0.0",
                "Main",
                "",
                "",
                new ArrayList<>()
        );
    }
}
