package com.gafipro.gafiscript.client;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public final class GafiCodeEditor {
    private static final List<String> KEYWORDS = List.of(
            "public", "private", "protected", "static", "final", "class", "interface",
            "enum", "record", "void", "int", "long", "double", "float", "boolean",
            "char", "byte", "short", "new", "return", "if", "else", "for", "while",
            "do", "switch", "case", "default", "try", "catch", "finally", "throw",
            "throws", "extends", "implements", "import", "package", "this", "super",
            "true", "false", "null"
    );

    private static final List<String> API_SUGGESTIONS = List.of(
            "Gafi.",
            "Gafi.world()",
            "Gafi.players()",
            "Gafi.scheduler()",
            "Gafi.broadcast(",
            "Gafi.logInfo(",
            "Gafi.logWarn(",
            "Gafi.logError(",
            "Gafi.runSync(",
            "Gafi.scheduler().nextTick(",
            "Gafi.scheduler().delayTicks(",
            "Gafi.scheduler().delaySeconds(",
            "Gafi.scheduler().repeatTicks(",
            "Gafi.scheduler().repeatSeconds(",
            "Gafi.scheduler().runAsync(",
            "Gafi.events()",
            "Gafi.events().onPlayerJoin(",
            "Gafi.events().onPlayerLeave(",
            "Gafi.events().onPlayerDeath(",
            "Gafi.events().onBlockBreak(",
            "Gafi.events().onBlockUse(",
            "Gafi.events().onTick(",
            "world.setBlock(",
            "world.fill(",
            "world.breakBlock(",
            "world.findBlocks(",
            "player.sendMessage(",
            "player.sendActionBar(",
            "player.teleport(",
            "player.giveItem(",
            "player.removeItem(",
            "player.addEffect(",
            "player.health()",
            "player.position()",
            "GafiPosition.of("
    );

    private final List<String> lines = new ArrayList<>();
    private int cursorLine;
    private int cursorColumn;
    private int x;
    private int y;
    private int width;
    private int height;
    private TextRenderer textRenderer;
    private boolean completionVisible;
    private int completionIndex;
    private List<String> completionItems = List.of();
    private final List<String> problems = new ArrayList<>();

    public GafiCodeEditor() {
        lines.add("");
    }

    public void resize(int x, int y, int width, int height, TextRenderer renderer) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.textRenderer = renderer;
    }

    public void setText(String text) {
        lines.clear();
        lines.addAll(Arrays.asList(text.replace("\r\n", "\n").replace('\r', '\n').split("\n", -1)));
        if (lines.isEmpty()) lines.add("");
        cursorLine = 0;
        cursorColumn = 0;
        completionVisible = false;
        analyze();
    }

    public String getText() {
        return String.join("\n", lines);
    }

    public void clearProblems() {
        problems.clear();
    }

    public void toggleCompletion() {
        completionVisible = !completionVisible;
        completionIndex = 0;
        updateCompletions();
    }

    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        boolean control = (modifiers & GLFW.GLFW_MOD_CONTROL) != 0;
        boolean shift = (modifiers & GLFW.GLFW_MOD_SHIFT) != 0;

        if (completionVisible) {
            if (keyCode == GLFW.GLFW_KEY_DOWN) {
                completionIndex = Math.min(completionIndex + 1, Math.max(0, completionItems.size() - 1));
                return true;
            }
            if (keyCode == GLFW.GLFW_KEY_UP) {
                completionIndex = Math.max(0, completionIndex - 1);
                return true;
            }
            if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_TAB) {
                acceptCompletion();
                return true;
            }
            if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                completionVisible = false;
                return true;
            }
        }

        switch (keyCode) {
            case GLFW.GLFW_KEY_LEFT -> {
                if (cursorColumn > 0) cursorColumn--;
                else if (cursorLine > 0) {
                    cursorLine--;
                    cursorColumn = lines.get(cursorLine).length();
                }
                completionVisible = false;
                return true;
            }
            case GLFW.GLFW_KEY_RIGHT -> {
                if (cursorColumn < lines.get(cursorLine).length()) cursorColumn++;
                else if (cursorLine < lines.size() - 1) {
                    cursorLine++;
                    cursorColumn = 0;
                }
                completionVisible = false;
                return true;
            }
            case GLFW.GLFW_KEY_UP -> {
                if (cursorLine > 0) {
                    cursorLine--;
                    cursorColumn = Math.min(cursorColumn, lines.get(cursorLine).length());
                }
                return true;
            }
            case GLFW.GLFW_KEY_DOWN -> {
                if (cursorLine < lines.size() - 1) {
                    cursorLine++;
                    cursorColumn = Math.min(cursorColumn, lines.get(cursorLine).length());
                }
                return true;
            }
            case GLFW.GLFW_KEY_HOME -> {
                cursorColumn = 0;
                return true;
            }
            case GLFW.GLFW_KEY_END -> {
                cursorColumn = lines.get(cursorLine).length();
                return true;
            }
            case GLFW.GLFW_KEY_BACKSPACE -> {
                backspace();
                return true;
            }
            case GLFW.GLFW_KEY_DELETE -> {
                delete();
                return true;
            }
            case GLFW.GLFW_KEY_ENTER -> {
                enter();
                return true;
            }
            case GLFW.GLFW_KEY_TAB -> {
                insertText("    ");
                return true;
            }
            case GLFW.GLFW_KEY_A -> {
                if (control) {
                    cursorLine = lines.size() - 1;
                    cursorColumn = lines.get(cursorLine).length();
                    return true;
                }
            }
            case GLFW.GLFW_KEY_C -> {
                if (control) {
                    clientClipboard(getText());
                    return true;
                }
            }
            case GLFW.GLFW_KEY_X -> {
                if (control) {
                    clientClipboard(getText());
                    setText("");
                    return true;
                }
            }
            case GLFW.GLFW_KEY_V -> {
                if (control) {
                    insertText(clientClipboard(null));
                    return true;
                }
            }
            default -> {
            }
        }

        if (shift) {
            // Reserved for future real range selections.
        }

        return false;
    }

    public boolean charTyped(char chr, int modifiers) {
        if (chr == '\n' || Character.isISOControl(chr)) return false;
        insertText(String.valueOf(chr));
        completionVisible = false;
        return true;
    }

    public void mouseClicked(double mouseX, double mouseY, int button) {
        if (mouseX < x || mouseX > x + width || mouseY < y || mouseY > y + height) return;

        int lineHeight = 10;
        int line = Math.max(0, (int) ((mouseY - y) / lineHeight));
        if (line >= lines.size()) line = lines.size() - 1;

        String current = lines.get(line);
        int estimated = 0;
        int column = 0;
        for (int i = 0; i <= current.length(); i++) {
            int nextWidth = textRenderer.getWidth(current.substring(0, i));
            if (Math.abs(nextWidth - (mouseX - x - 34)) < Math.abs(estimated - (mouseX - x - 34))) {
                estimated = nextWidth;
                column = i;
            }
        }

        cursorLine = line;
        cursorColumn = Math.min(column, current.length());
        completionVisible = false;
    }

    public void render(DrawContext context) {
        if (textRenderer == null) return;

        context.fill(x, y, x + width, y + height, 0xDD101216);
        context.fill(x, y, x + 32, y + height, 0xFF191C21);

        int lineHeight = 10;
        int maxLines = Math.max(1, height / lineHeight);
        int firstLine = Math.max(0, cursorLine - maxLines + 2);

        for (int visible = 0; visible < maxLines; visible++) {
            int lineIndex = firstLine + visible;
            if (lineIndex >= lines.size()) break;

            int drawY = y + visible * lineHeight + 2;
            context.drawText(textRenderer, Integer.toString(lineIndex + 1), x + 4, drawY, 0xFF6E7580, false);
            drawCodeLine(context, lines.get(lineIndex), x + 36, drawY);
        }

        int cursorY = y + (cursorLine - firstLine) * lineHeight + 1;
        int cursorX = x + 36 + textRenderer.getWidth(lines.get(cursorLine).substring(0, Math.min(cursorColumn, lines.get(cursorLine).length())));
        context.fill(cursorX, cursorY, cursorX + 1, cursorY + 9, 0xFFFFFFFF);

        if (completionVisible && !completionItems.isEmpty()) {
            renderCompletion(context, cursorX, cursorY);
        }

        if (!problems.isEmpty()) {
            int problemY = y + height - 30;
            context.fill(x + 36, problemY, x + width, y + height, 0xEE2A1515);
            context.drawText(textRenderer, "Problems: " + problems.getFirst(), x + 40, problemY + 6, 0xFFFF7777, false);
        }
    }

    private void drawCodeLine(DrawContext context, String line, int drawX, int drawY) {
        if (line.isBlank()) return;

        String trimmed = line.trim();
        int color = 0xFFE6E6E6;

        if (trimmed.startsWith("//")) {
            color = 0xFF6A9955;
        } else if (trimmed.startsWith("import ") || trimmed.startsWith("package ")) {
            color = 0xFF569CD6;
        } else if (KEYWORDS.stream().anyMatch(trimmed::startsWith)) {
            color = 0xFF569CD6;
        }

        context.drawText(textRenderer, line, drawX, drawY, color, false);
    }

    private void renderCompletion(DrawContext context, int cursorX, int cursorY) {
        int boxWidth = Math.min(360, width - Math.max(0, cursorX - x) - 4);
        int rows = Math.min(8, completionItems.size());
        int boxHeight = rows * 14 + 4;
        int boxX = Math.max(x + 36, Math.min(cursorX, x + width - boxWidth));
        int boxY = Math.min(y + height - boxHeight, cursorY + 10);

        context.fill(boxX, boxY, boxX + boxWidth, boxY + boxHeight, 0xFF20242B);
        for (int i = 0; i < rows; i++) {
            boolean selected = i == completionIndex;
            int rowY = boxY + 2 + i * 14;
            if (selected) {
                context.fill(boxX, rowY, boxX + boxWidth, rowY + 14, 0xFF3A4352);
            }
            context.drawText(textRenderer, completionItems.get(i), boxX + 5, rowY + 3, 0xFFE6E6E6, false);
        }
    }

    private void updateCompletions() {
        String prefix = currentPrefix();
        String lower = prefix.toLowerCase(Locale.ROOT);

        if (lower.startsWith("gafi.scheduler().")) {
            completionItems = List.of(
                    "nextTick(",
                    "delayTicks(",
                    "delaySeconds(",
                    "repeatTicks(",
                    "repeatSeconds(",
                    "runAsync(",
                    "runSync("
            );
        } else if (lower.startsWith("gafi.")) {
            completionItems = API_SUGGESTIONS.stream()
                    .filter(item -> item.toLowerCase(Locale.ROOT).startsWith(lower))
                    .toList();
        } else {
            completionItems = KEYWORDS.stream()
                    .filter(item -> item.startsWith(prefix))
                    .toList();
        }

        if (completionItems.isEmpty()) {
            completionItems = API_SUGGESTIONS.stream().limit(8).toList();
        }
        completionIndex = Math.min(completionIndex, completionItems.size() - 1);
    }

    private String currentPrefix() {
        String line = lines.get(cursorLine);
        int end = Math.min(cursorColumn, line.length());
        int start = end;
        while (start > 0) {
            char c = line.charAt(start - 1);
            if (!(Character.isJavaIdentifierPart(c) || c == '.' || c == '(')) break;
            start--;
        }
        return line.substring(start, end);
    }

    private void acceptCompletion() {
        if (completionItems.isEmpty()) return;
        String completion = completionItems.get(Math.min(completionIndex, completionItems.size() - 1));
        String prefix = currentPrefix();

        int remove = Math.min(prefix.length(), cursorColumn);
        for (int i = 0; i < remove; i++) backspace();
        insertText(completion);
        completionVisible = false;
    }

    private void insertText(String text) {
        if (text == null || text.isEmpty()) return;

        String current = lines.get(cursorLine);
        String before = current.substring(0, cursorColumn);
        String after = current.substring(cursorColumn);

        String[] parts = text.replace("\r\n", "\n").split("\n", -1);
        lines.set(cursorLine, before + parts[0]);

        for (int i = 1; i < parts.length; i++) {
            cursorLine++;
            lines.add(cursorLine, parts[i]);
        }

        cursorColumn = parts.length == 1 ? before.length() + parts[0].length() : parts[parts.length - 1].length();
        if (parts.length == 1) {
            lines.set(cursorLine, before + parts[0] + after);
        } else {
            lines.set(cursorLine, parts[parts.length - 1] + after);
        }

        analyze();
    }

    private void enter() {
        String current = lines.get(cursorLine);
        String before = current.substring(0, cursorColumn);
        String after = current.substring(cursorColumn);
        String indent = before.substring(0, before.length() - before.stripLeading().length());

        lines.set(cursorLine, before);
        lines.add(++cursorLine, indent);
        cursorColumn = indent.length();
        insertText(after);
    }

    private void backspace() {
        if (cursorColumn > 0) {
            String current = lines.get(cursorLine);
            lines.set(cursorLine, current.substring(0, cursorColumn - 1) + current.substring(cursorColumn));
            cursorColumn--;
        } else if (cursorLine > 0) {
            String current = lines.remove(cursorLine);
            cursorLine--;
            cursorColumn = lines.get(cursorLine).length();
            lines.set(cursorLine, lines.get(cursorLine) + current);
        }
        analyze();
    }

    private void delete() {
        String current = lines.get(cursorLine);
        if (cursorColumn < current.length()) {
            lines.set(cursorLine, current.substring(0, cursorColumn) + current.substring(cursorColumn + 1));
        } else if (cursorLine < lines.size() - 1) {
            lines.set(cursorLine, current + lines.remove(cursorLine + 1));
        }
        analyze();
    }

    private void analyze() {
        problems.clear();
        String source = getText();
        if (source.contains("sendMessag(")) {
            problems.add("Possible typo: did you mean sendMessage()?");
        }
        if (source.contains("while (true)") && !source.contains("break;")) {
            problems.add("Potential infinite loop: while (true) without an obvious break.");
        }
    }

    private String clientClipboard(String replacement) {
        net.minecraft.client.MinecraftClient client = net.minecraft.client.MinecraftClient.getInstance();
        if (replacement != null) {
            client.keyboard.setClipboard(replacement);
            return replacement;
        }
        return client.keyboard.getClipboard();
    }
}
