package com.gafipro.gafiscript.client;

import com.gafipro.gafiscript.net.GafiScriptNetworking;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import org.lwjgl.glfw.GLFW;

public final class GafiScriptScreen extends Screen {
    private final BlockPos blockPos;
    private final GafiCodeEditor editor;
    private TextFieldWidget nameField;
    private String scriptName = "Main";

    public GafiScriptScreen(BlockPos blockPos) {
        super(Text.translatable("screen.gafiscript.editor"));
        this.blockPos = blockPos;
        this.editor = new GafiCodeEditor();
    }

    @Override
    protected void init() {
        int top = 28;
        nameField = new TextFieldWidget(textRenderer, 12, 6, 210, 20, Text.literal("Script name"));
        nameField.setText(scriptName);
        addDrawableChild(nameField);

        addDrawableChild(ButtonWidget.builder(Text.translatable("gafiscript.ui.save"), button -> save())
                .dimensions(width - 220, 6, 64, 20).build());

        addDrawableChild(ButtonWidget.builder(Text.translatable("gafiscript.ui.run"), button -> run())
                .dimensions(width - 150, 6, 64, 20).build());

        addDrawableChild(ButtonWidget.builder(Text.translatable("gafiscript.ui.reload"), button -> {
                    GafiScriptNetworking.sendScriptRequest(blockPos);
                    nameField.setText(scriptName);
                })
                .dimensions(width - 80, 6, 68, 20).build());

        editor.resize(10, top + 6, width - 20, height - top - 18, textRenderer);
    }

    public void setScriptData(BlockPos pos, String name, String source) {
        if (!blockPos.equals(pos)) return;
        this.scriptName = name == null || name.isBlank() ? "Main" : name;
        this.nameField.setText(this.scriptName);
        this.editor.setText(source == null ? "" : source);
        this.editor.clearProblems();
    }

    private void save() {
        scriptName = nameField.getText();
        GafiScriptNetworking.sendSave(blockPos, scriptName, editor.getText());
    }

    private void run() {
        scriptName = nameField.getText();
        GafiScriptNetworking.sendRun(blockPos, scriptName, editor.getText());
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        boolean control = (modifiers & GLFW.GLFW_MOD_CONTROL) != 0;

        if (control && keyCode == GLFW.GLFW_KEY_S) {
            save();
            return true;
        }

        if (keyCode == GLFW.GLFW_KEY_F5) {
            run();
            return true;
        }

        if (control && keyCode == GLFW.GLFW_KEY_SPACE) {
            editor.toggleCompletion();
            return true;
        }

        if (!nameField.isFocused() && editor.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        if (!nameField.isFocused() && editor.charTyped(chr, modifiers)) {
            return true;
        }
        return super.charTyped(chr, modifiers);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (mouseY >= 34 && mouseX >= 10 && mouseX <= width - 10) {
            editor.mouseClicked(mouseX, mouseY, button);
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context, mouseX, mouseY, delta);

        context.drawTextWithShadow(textRenderer,
                Text.literal("§7Block: " + blockPos.toShortString()),
                235, 11, 0xFFFFFFFF);

        editor.render(context);

        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public void close() {
        super.close();
    }
}
