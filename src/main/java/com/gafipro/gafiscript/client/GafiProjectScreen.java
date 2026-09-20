package com.gafipro.gafiscript.client;

import com.gafipro.gafiscript.net.GafiScriptNetworking;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.input.CharInput;
import net.minecraft.client.input.KeyInput;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class GafiProjectScreen extends Screen {
    private final GafiCodeEditor editor = new GafiCodeEditor();
    private final List<ButtonWidget> fileButtons = new ArrayList<>();

    private TextFieldWidget projectField;
    private TextFieldWidget fileField;

    private String projectName;
    private String selectedFile;
    private List<String> files = List.of();

    public GafiProjectScreen(
            String projectName,
            String selectedFile,
            String files,
            String source
    ) {
        super(Text.literal("GafiScript Project"));
        this.projectName = projectName;
        this.selectedFile = selectedFile;
        this.files = parseFiles(files);
        this.editor.setText(source == null ? "" : source);
    }

    @Override
    protected void init() {
        projectField = new TextFieldWidget(
                textRenderer,
                8,
                6,
                180,
                20,
                Text.literal("Project")
        );
        projectField.setText(projectName);
        projectField.setEditable(false);
        addDrawableChild(projectField);

        addDrawableChild(
                ButtonWidget.builder(
                        Text.literal("Save"),
                        button -> save()
                ).dimensions(width - 212, 6, 62, 20).build()
        );

        addDrawableChild(
                ButtonWidget.builder(
                        Text.literal("Run"),
                        button -> run()
                ).dimensions(width - 145, 6, 62, 20).build()
        );

        addDrawableChild(
                ButtonWidget.builder(
                        Text.literal("Reload"),
                        button -> reload()
                ).dimensions(width - 78, 6, 66, 20).build()
        );

        fileField = new TextFieldWidget(
                textRenderer,
                8,
                height - 24,
                150,
                20,
                Text.literal("NewFile.java")
        );
        addDrawableChild(fileField);

        addDrawableChild(
                ButtonWidget.builder(
                        Text.literal("New file"),
                        button -> createFile()
                ).dimensions(164, height - 24, 70, 20).build()
        );

        for (int i = 0; i < 10; i++) {
            final int index = i;
            ButtonWidget button =
                    ButtonWidget.builder(
                            Text.literal(""),
                            ignored -> {
                                if (index < files.size()) {
                                    selectFile(files.get(index));
                                }
                            }
                    ).dimensions(6, 34 + i * 24, 180, 20).build();

            fileButtons.add(button);
            addDrawableChild(button);
        }

        editor.resize(
                195,
                34,
                width - 205,
                height - 66,
                textRenderer
        );

        refreshFileButtons();
    }

    public void setProjectData(
            String project,
            String file,
            String fileList,
            String source
    ) {
        saveLocal();

        projectName = project;
        selectedFile = file;
        files = parseFiles(fileList);

        projectField.setText(projectName);
        editor.setText(source == null ? "" : source);

        refreshFileButtons();
    }

    private void selectFile(String file) {
        saveLocal();
        selectedFile = file;
        GafiScriptNetworking.sendProjectOpen(
                projectName,
                selectedFile
        );
    }

    private void save() {
        saveLocal();

        GafiScriptNetworking.sendProjectSave(
                projectName,
                selectedFile,
                editor.getText()
        );
    }

    private void run() {
        saveLocal();
        GafiScriptNetworking.sendProjectSave(
                projectName,
                selectedFile,
                editor.getText()
        );
        GafiScriptNetworking.sendProjectRun(
                projectName
        );
    }

    private void reload() {
        GafiScriptNetworking.sendProjectOpen(
                projectName,
                selectedFile
        );
    }

    private void createFile() {
        String requested =
                fileField.getText().trim();

        if (requested.isEmpty()) {
            requested = "NewScript.java";
        }

        if (!requested.endsWith(".java")) {
            requested += ".java";
        }

        selectedFile = requested;
        editor.setText(
                "public class " +
                        requested.substring(
                                0,
                                requested.length() - 5
                        ).replaceAll(
                                "[^A-Za-z0-9_$]",
                                "_"
                        ) +
                        " {\n" +
                        "    public static void start() {\n" +
                        "    }\n" +
                        "}\n"
        );

        save();
    }

    private void saveLocal() {
        if (projectName == null ||
                projectName.isBlank() ||
                selectedFile == null ||
                selectedFile.isBlank()) {
            return;
        }

        GafiScriptNetworking.sendProjectSave(
                projectName,
                selectedFile,
                editor.getText()
        );
    }

    private List<String> parseFiles(String source) {
        if (source == null || source.isBlank()) {
            return List.of("Main.java");
        }

        return Arrays.stream(
                        source.split("\\n")
                )
                .map(String::trim)
                .filter(file -> !file.isEmpty())
                .sorted()
                .toList();
    }

    private void refreshFileButtons() {
        for (int i = 0; i < fileButtons.size(); i++) {
            ButtonWidget button =
                    fileButtons.get(i);

            if (i < files.size()) {
                button.visible = true;
                button.active = true;

                button.setMessage(
                        Text.literal(
                                files.get(i).equals(selectedFile)
                                        ? "§e> " + files.get(i)
                                        : "§f" + files.get(i)
                        )
                );
            } else {
                button.visible = false;
                button.active = false;
            }
        }
    }

    @Override
    public boolean keyPressed(KeyInput input) {
        int keyCode = input.getKeycode();

        if (input.hasCtrl() &&
                keyCode == org.lwjgl.glfw.GLFW.GLFW_KEY_S) {
            save();
            return true;
        }

        if (input.hasCtrl() &&
                (keyCode == org.lwjgl.glfw.GLFW.GLFW_KEY_B ||
                 keyCode == org.lwjgl.glfw.GLFW.GLFW_KEY_F12)) {
            editor.goToDefinition();
            return true;
        }

        if (input.hasCtrl() &&
                keyCode == org.lwjgl.glfw.GLFW.GLFW_KEY_SPACE) {
            editor.toggleCompletion();
            return true;
        }

        if (keyCode ==
                org.lwjgl.glfw.GLFW.GLFW_KEY_F5) {
            run();
            return true;
        }

        if (!fileField.isFocused() &&
                !projectField.isFocused() &&
                editor.keyPressed(
                        keyCode,
                        input.scancode(),
                        input.modifiers()
                )) {
            return true;
        }

        return super.keyPressed(input);
    }

    @Override
    public boolean charTyped(CharInput input) {
        if (!fileField.isFocused() &&
                !projectField.isFocused() &&
                input.isValidChar() &&
                editor.charTyped(
                        input.asString(),
                        input.modifiers()
                )) {
            return true;
        }

        return super.charTyped(input);
    }

    @Override
    public boolean mouseClicked(
            Click click,
            boolean doubled
    ) {
        if (click.x() >= 195 &&
                click.y() >= 34 &&
                click.y() < height - 30) {
            editor.mouseClicked(
                    click.x(),
                    click.y(),
                    click.buttonInfo().button()
            );
        }

        return super.mouseClicked(
                click,
                doubled
        );
    }

    @Override
    public void render(
            DrawContext context,
            int mouseX,
            int mouseY,
            float delta
    ) {
        // Screen#render already renders the background. Rendering it here
        // as well can request Minecraft 1.21.11's blur pass twice in a frame.
        context.drawTextWithShadow(
                textRenderer,
                Text.literal(
                        "§7Project: " + projectName +
                                "  §8File: " + selectedFile
                ),
                205,
                26,
                0xFFFFFFFF
        );

        editor.render(context);

        String hover = editor.hoverTextAt(mouseX, mouseY);
        if (hover != null) {
            int boxWidth = Math.min(width - 20, textRenderer.getWidth(hover) + 12);
            int boxX = Math.max(8, Math.min(mouseX + 8, width - boxWidth - 8));
            int boxY = Math.max(28, mouseY - 22);

            context.fill(
                    boxX,
                    boxY,
                    boxX + boxWidth,
                    boxY + 18,
                    0xEE20242B
            );

            context.drawText(
                    textRenderer,
                    hover,
                    boxX + 6,
                    boxY + 5,
                    0xFFFFFFFF,
                    false
            );
        }

        super.render(
                context,
                mouseX,
                mouseY,
                delta
        );
    }
}
