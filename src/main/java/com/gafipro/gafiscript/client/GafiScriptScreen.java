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
import net.minecraft.util.math.BlockPos;

public final class GafiScriptScreen extends Screen {
    private final BlockPos blockPos;
    private final GafiCodeEditor editor;

    private TextFieldWidget nameField;
    private TextFieldWidget searchField;
    private TextFieldWidget replaceField;

    private ButtonWidget nextButton;
    private ButtonWidget replaceButton;
    private ButtonWidget replaceAllButton;

    private String scriptName = "Main";
    private boolean searchVisible;

    public GafiScriptScreen(BlockPos blockPos) {
        super(Text.translatable("screen.gafiscript.editor"));
        this.blockPos = blockPos;
        this.editor = new GafiCodeEditor();
    }

    @Override
    protected void init() {
        nameField = new TextFieldWidget(
                textRenderer,
                12,
                6,
                210,
                20,
                Text.literal("Script name")
        );
        nameField.setText(scriptName);
        addDrawableChild(nameField);

        addDrawableChild(
                ButtonWidget.builder(
                        Text.translatable("gafiscript.ui.save"),
                        button -> save()
                ).dimensions(width - 220, 6, 64, 20).build()
        );

        addDrawableChild(
                ButtonWidget.builder(
                        Text.translatable("gafiscript.ui.run"),
                        button -> run()
                ).dimensions(width - 150, 6, 64, 20).build()
        );

        addDrawableChild(
                ButtonWidget.builder(
                        Text.literal("Docs"),
                        button -> toggleSearch()
                ).dimensions(width - 80, 6, 68, 20).build()
        );

        searchField = new TextFieldWidget(
                textRenderer,
                10,
                height - 24,
                180,
                20,
                Text.literal("Find")
        );

        replaceField = new TextFieldWidget(
                textRenderer,
                195,
                height - 24,
                180,
                20,
                Text.literal("Replace")
        );

        addDrawableChild(searchField);
        addDrawableChild(replaceField);

        nextButton = ButtonWidget.builder(
                Text.literal("Next"),
                button -> editor.findNext(searchField.getText())
        ).dimensions(380, height - 24, 48, 20).build();

        replaceButton = ButtonWidget.builder(
                Text.literal("Replace"),
                button -> editor.replaceCurrent(
                        searchField.getText(),
                        replaceField.getText()
                )
        ).dimensions(432, height - 24, 62, 20).build();

        replaceAllButton = ButtonWidget.builder(
                Text.literal("All"),
                button -> editor.replaceAll(
                        searchField.getText(),
                        replaceField.getText()
                )
        ).dimensions(498, height - 24, 42, 20).build();

        addDrawableChild(nextButton);
        addDrawableChild(replaceButton);
        addDrawableChild(replaceAllButton);

        addDrawableChild(
                ButtonWidget.builder(
                        Text.literal("Format"),
                        button -> editor.formatJava()
                ).dimensions(544, height - 24, 58, 20).build()
        );

        setSearchVisible(false);

        editor.resize(
                10,
                34,
                width - 20,
                height - 66,
                textRenderer
        );
    }

    public void setScriptData(
            BlockPos pos,
            String name,
            String source
    ) {
        if (!blockPos.equals(pos)) {
            return;
        }

        this.scriptName =
                name == null || name.isBlank()
                        ? "Main"
                        : name;

        if (nameField != null) {
            nameField.setText(this.scriptName);
        }

        this.editor.setText(
                source == null ? "" : source
        );

        this.editor.clearProblems();
    }

    private void save() {
        scriptName = nameField.getText();

        GafiScriptNetworking.sendSave(
                blockPos,
                scriptName,
                editor.getText()
        );
    }

    private void run() {
        scriptName = nameField.getText();

        GafiScriptNetworking.sendRun(
                blockPos,
                scriptName,
                editor.getText()
        );
    }

    private void toggleSearch() {
        setSearchVisible(!searchVisible);

        if (searchVisible) {
            searchField.setFocused(true);
        } else {
            nameField.setFocused(true);
        }
    }

    private void setSearchVisible(boolean visible) {
        searchVisible = visible;

        searchField.visible = visible;
        replaceField.visible = visible;
        nextButton.visible = visible;
        replaceButton.visible = visible;
        replaceAllButton.visible = visible;
    }

    @Override
    public boolean keyPressed(KeyInput input) {
        int keyCode = input.getKeycode();
        int scanCode = input.scancode();
        int modifiers = input.modifiers();

        if (input.hasCtrl() &&
                keyCode == org.lwjgl.glfw.GLFW.GLFW_KEY_S) {
            save();
            return true;
        }

        if (input.hasCtrl() &&
                keyCode == org.lwjgl.glfw.GLFW.GLFW_KEY_F) {
            setSearchVisible(true);
            searchField.setFocused(true);
            return true;
        }

        if (input.hasCtrl() &&
                keyCode == org.lwjgl.glfw.GLFW.GLFW_KEY_H) {
            setSearchVisible(true);
            replaceField.setFocused(true);
            return true;
        }

        if (searchVisible &&
                keyCode == org.lwjgl.glfw.GLFW.GLFW_KEY_ENTER) {
            if (replaceField.isFocused()) {
                editor.replaceCurrent(
                        searchField.getText(),
                        replaceField.getText()
                );
            } else {
                editor.findNext(
                        searchField.getText()
                );
            }

            return true;
        }

        if (keyCode ==
                org.lwjgl.glfw.GLFW.GLFW_KEY_F5) {
            run();
            return true;
        }

        if (input.hasCtrl() &&
                keyCode ==
                        org.lwjgl.glfw.GLFW.GLFW_KEY_SPACE) {
            editor.toggleCompletion();
            return true;
        }

        if (!nameField.isFocused() &&
                !searchField.isFocused() &&
                !replaceField.isFocused() &&
                editor.keyPressed(
                        keyCode,
                        scanCode,
                        modifiers
                )) {
            return true;
        }

        return super.keyPressed(input);
    }

    @Override
    public boolean charTyped(CharInput input) {
        if (!nameField.isFocused() &&
                !searchField.isFocused() &&
                !replaceField.isFocused() &&
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
        if (click.y() >= 34 &&
                click.y() < height - 30 &&
                click.x() >= 10 &&
                click.x() <= width - 10) {

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
    public boolean mouseDragged(
            Click click,
            double offsetX,
            double offsetY
    ) {
        if (click.buttonInfo().button() ==
                org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_1 &&
                click.x() >= 10 &&
                click.y() >= 34 &&
                click.y() < height - 30) {
            editor.mouseDragged(
                    click.x() + offsetX,
                    click.y() + offsetY,
                    click.buttonInfo().button()
            );
            return true;
        }

        return super.mouseDragged(click, offsetX, offsetY);
    }

    @Override
    public boolean mouseReleased(Click click) {
        boolean editorHandled = editor.mouseReleased(
                click.buttonInfo().button()
        );
        return editorHandled || super.mouseReleased(click);
    }

    @Override
    public void render(
            DrawContext context,
            int mouseX,
            int mouseY,
            float delta
    ) {
        // Screen#render already renders the screen background. Calling
        // renderBackground() here as well causes Minecraft 1.21.11 to
        // request the blur pass twice in the same frame.
        super.render(
                context,
                mouseX,
                mouseY,
                delta
        );

        context.drawTextWithShadow(
                textRenderer,
                Text.literal(
                        "§7Block: " +
                        blockPos.toShortString()
                ),
                235,
                11,
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
    }
}
