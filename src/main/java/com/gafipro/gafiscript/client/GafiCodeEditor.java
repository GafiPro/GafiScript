package com.gafipro.gafiscript.client;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;
import java.util.Locale;

public final class GafiCodeEditor {
    private static final List<String> KEYWORDS = List.of(
            "public", "private", "protected", "static", "final", "class",
            "interface", "enum", "record", "void", "int", "long", "double",
            "float", "boolean", "char", "byte", "short", "new", "return",
            "if", "else", "for", "while", "do", "switch", "case", "default",
            "try", "catch", "finally", "throw", "throws", "extends",
            "implements", "import", "package", "this", "super", "true",
            "false", "null"
    );

    private static final List<String> API_SUGGESTIONS = List.of(
            "Gafi.",
            "Gafi.world()",
            "Gafi.players()",
            "Gafi.player(",
            "Gafi.scheduler()",
            "Gafi.events()",
            "Gafi.commands()",
            "Gafi.profiler()",
            "Gafi.storage(",
            "Gafi.config(",
            "Gafi.database(",
            "Gafi.random()",
            "Gafi.broadcast(",
            "Gafi.logInfo(",
            "Gafi.logWarn(",
            "Gafi.logError(",
            "Gafi.delayTicks(",
            "Gafi.delaySeconds(",
            "Gafi.repeatTicks(",
            "Gafi.repeatSeconds(",
            "Gafi.scheduler().nextTick(",
            "Gafi.scheduler().delayTicks(",
            "Gafi.scheduler().delaySeconds(",
            "Gafi.scheduler().repeatTicks(",
            "Gafi.scheduler().repeatSeconds(",
            "Gafi.scheduler().runAsync(",
            "Gafi.scheduler().runSync(",
            "Gafi.scheduler().sequence()",
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
            "world.entities()",
            "world.spawnEntity(",
            "world.playSound(",
            "player.sendMessage(",
            "player.sendActionBar(",
            "player.teleport(",
            "player.giveItem(",
            "player.removeItem(",
            "player.addEffect(",
            "player.inventory()",
            "player.health()",
            "player.position()",
            "GafiPosition.of(",
            "GafiTask",
            "GafiPlayer",
            "GafiWorld",
            "GafiEntity",
            "GafiInventory",
            "GafiBossBar"
    );

    private final List<String> lines = new ArrayList<>();
    private final Deque<State> undoStack = new ArrayDeque<>();
    private final Deque<State> redoStack = new ArrayDeque<>();
    private int cursorLine;
    private int cursorColumn;
    private int scrollLine;
    private int selectionAnchor = -1;
    private boolean mouseSelecting;
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

    public void resize(
            int x,
            int y,
            int width,
            int height,
            TextRenderer renderer
    ) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.textRenderer = renderer;
    }

    public void setText(String text) {
        lines.clear();
        String normalized = text == null
                ? ""
                : text.replace("\r\n", "\n")
                        .replace('\r', '\n');
        lines.addAll(Arrays.asList(normalized.split("\n", -1)));

        if (lines.isEmpty()) {
            lines.add("");
        }

        cursorLine = 0;
        cursorColumn = 0;
        scrollLine = 0;
        selectionAnchor = -1;
        mouseSelecting = false;
        undoStack.clear();
        redoStack.clear();
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

    public boolean keyPressed(
            int keyCode,
            int scanCode,
            int modifiers
    ) {
        boolean control =
                (modifiers & GLFW.GLFW_MOD_CONTROL) != 0;
        boolean shift =
                (modifiers & GLFW.GLFW_MOD_SHIFT) != 0;
        boolean alt =
                (modifiers & GLFW.GLFW_MOD_ALT) != 0;

        if (completionVisible) {
            if (keyCode == GLFW.GLFW_KEY_DOWN) {
                completionIndex = Math.min(
                        completionIndex + 1,
                        Math.max(
                                0,
                                completionItems.size() - 1
                        )
                );
                return true;
            }

            if (keyCode == GLFW.GLFW_KEY_UP) {
                completionIndex = Math.max(
                        0,
                        completionIndex - 1
                );
                return true;
            }

            if (keyCode == GLFW.GLFW_KEY_ENTER ||
                    keyCode == GLFW.GLFW_KEY_TAB) {
                acceptCompletion();
                return true;
            }

            if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                completionVisible = false;
                return true;
            }
        }

        if (control && keyCode == GLFW.GLFW_KEY_Z) {
            if (shift) {
                redo();
            } else {
                undo();
            }
            return true;
        }

        if (control && keyCode == GLFW.GLFW_KEY_Y) {
            redo();
            return true;
        }

        if (control && keyCode == GLFW.GLFW_KEY_A) {
            selectAll();
            return true;
        }

        if (control && keyCode == GLFW.GLFW_KEY_C) {
            copySelection();
            return true;
        }

        if (control && keyCode == GLFW.GLFW_KEY_X) {
            cutSelection();
            return true;
        }

        if (control && keyCode == GLFW.GLFW_KEY_V) {
            replaceSelectionWith(
                    clientClipboard(null)
            );
            return true;
        }

        if (control && keyCode == GLFW.GLFW_KEY_BACKSPACE) {
            deletePreviousWord();
            return true;
        }

        if (alt && keyCode == GLFW.GLFW_KEY_UP) {
            moveLine(-1);
            return true;
        }

        if (alt && keyCode == GLFW.GLFW_KEY_DOWN) {
            moveLine(1);
            return true;
        }

        switch (keyCode) {
            case GLFW.GLFW_KEY_LEFT -> {
                moveHorizontal(-1, shift);
                return true;
            }

            case GLFW.GLFW_KEY_RIGHT -> {
                moveHorizontal(1, shift);
                return true;
            }

            case GLFW.GLFW_KEY_UP -> {
                moveVertical(-1, shift);
                return true;
            }

            case GLFW.GLFW_KEY_DOWN -> {
                moveVertical(1, shift);
                return true;
            }

            case GLFW.GLFW_KEY_HOME -> {
                moveToColumn(0, shift);
                return true;
            }

            case GLFW.GLFW_KEY_END -> {
                moveToColumn(
                        lines.get(cursorLine).length(),
                        shift
                );
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
                replaceSelectionWith("    ");
                return true;
            }

            default -> {
            }
        }

        return false;
    }

    public boolean charTyped(
            String text,
            int modifiers
    ) {
        if (text == null || text.isEmpty()) {
            return false;
        }

        boolean inserted = false;

        for (int i = 0; i < text.length(); i++) {
            char chr = text.charAt(i);

            if (chr == '\n' ||
                    Character.isISOControl(chr)) {
                continue;
            }

            replaceSelectionWith(
                    String.valueOf(chr)
            );

            inserted = true;
        }

        if (inserted) {
            completionVisible = false;
        }

        return inserted;
    }

    public void mouseClicked(
            double mouseX,
            double mouseY,
            int button
    ) {
        if (button != GLFW.GLFW_MOUSE_BUTTON_1 ||
                textRenderer == null ||
                !isInsideEditor(mouseX, mouseY)) {
            return;
        }

        int[] position = mouseToCursor(mouseX, mouseY);
        setCursor(position[0], position[1], false);
        mouseSelecting = true;
        completionVisible = false;
    }

    public boolean mouseDragged(
            double mouseX,
            double mouseY,
            int button
    ) {
        if (button != GLFW.GLFW_MOUSE_BUTTON_1 ||
                textRenderer == null ||
                !mouseSelecting) {
            return false;
        }

        int[] position = mouseToCursor(mouseX, mouseY);
        setCursor(position[0], position[1], true);
        completionVisible = false;
        return true;
    }

    public boolean mouseReleased(int button) {
        if (button != GLFW.GLFW_MOUSE_BUTTON_1) {
            return false;
        }

        boolean wasSelecting = mouseSelecting;
        mouseSelecting = false;
        return wasSelecting;
    }

    public boolean mouseScrolled(
            double mouseX,
            double mouseY,
            double verticalAmount
    ) {
        if (textRenderer == null ||
                !isInsideEditor(mouseX, mouseY) ||
                lines.isEmpty()) {
            return false;
        }

        int lineHeight = 10;
        int maxLines = Math.max(1, height / lineHeight);
        int maxScroll = Math.max(0, lines.size() - maxLines);

        if (verticalAmount == 0.0) {
            return false;
        }

        int delta = verticalAmount > 0.0
                ? -3
                : 3;

        int oldScroll = scrollLine;

        scrollLine = Math.max(
                0,
                Math.min(
                        maxScroll,
                        scrollLine + delta
                )
        );

        return oldScroll != scrollLine;
    }

    private boolean isInsideEditor(double mouseX, double mouseY) {
        return mouseX >= x &&
                mouseX <= x + width &&
                mouseY >= y &&
                mouseY <= y + height;
    }

    private int[] mouseToCursor(double mouseX, double mouseY) {
        int lineHeight = 10;
        int firstLine = firstVisibleLine();

        double clampedX = Math.max(
                x + 36,
                Math.min(mouseX, x + width)
        );
        double clampedY = Math.max(
                y,
                Math.min(mouseY, y + height - 1)
        );

        int line = Math.max(
                0,
                Math.min(
                        lines.size() - 1,
                        firstLine + (int) ((clampedY - y) / lineHeight)
                )
        );

        String current = lines.get(line);
        int target = Math.max(
                0,
                (int) (clampedX - x - 36)
        );

        int column = 0;
        int bestDistance = Integer.MAX_VALUE;

        for (int i = 0; i <= current.length(); i++) {
            int measured = textRenderer.getWidth(
                    current.substring(0, i)
            );
            int distance = Math.abs(measured - target);

            if (distance < bestDistance) {
                bestDistance = distance;
                column = i;
            }
        }

        return new int[]{line, Math.min(column, current.length())};
    }

    public void selectAll() {
        mouseSelecting = false;
        selectionAnchor = 0;
        setCursorFromOffset(
                getText().length(),
                true
        );
    }

    public String selectedText() {
        if (!hasSelection()) {
            return "";
        }

        String text = getText();

        int start = selectionStart();
        int end = selectionEnd();

        return text.substring(start, end);
    }

    public String selectedOrAllText() {
        return hasSelection()
                ? selectedText()
                : getText();
    }

    public void copySelection() {
        clientClipboard(selectedText());
    }

    public void cutSelection() {
        if (!hasSelection()) return;
        clientClipboard(selectedText());
        replaceSelectionWith("");
    }

    public void replaceSelectionWith(
            String replacement
    ) {
        if (replacement == null) {
            return;
        }

        pushUndo();

        int start = selectionStart();
        int end = selectionEnd();

        String text = getText();

        if (hasSelection()) {
            text =
                    text.substring(0, start) +
                    replacement +
                    text.substring(end);
        } else {
            int cursor = cursorOffset();

            text =
                    text.substring(0, cursor) +
                    replacement +
                    text.substring(cursor);

            start = cursor;
        }

        setDocument(
                text,
                start + replacement.length()
        );

        redoStack.clear();
    }

    public boolean goToDefinition() {
        String prefix = currentIdentifier();
        if (prefix.isBlank()) {
            return false;
        }

        String source = getText();

        java.util.regex.Pattern pattern =
                java.util.regex.Pattern.compile(
                        "\\b(?:class|interface|enum|record)\\s+" +
                        java.util.regex.Pattern.quote(prefix) +
                        "\\b"
                );

        var matcher = pattern.matcher(source);

        if (!matcher.find()) {
            pattern =
                    java.util.regex.Pattern.compile(
                            "\\b" +
                            java.util.regex.Pattern.quote(prefix) +
                            "\\s*\\("
                    );

            matcher = pattern.matcher(source);
        }

        int cursor = cursorOffset();

        int best = -1;

        while (matcher.find()) {
            if (matcher.start() != cursor) {
                best = matcher.start();
                break;
            }
        }

        if (best < 0) {
            matcher = pattern.matcher(source);
            if (matcher.find()) {
                best = matcher.start();
            }
        }

        if (best < 0) {
            return false;
        }

        selectionAnchor = -1;
        setCursorFromOffset(best, false);
        return true;
    }

    public String hoverTextAt(
            double mouseX,
            double mouseY
    ) {
        if (textRenderer == null ||
                mouseX < x + 36 ||
                mouseY < y ||
                mouseX > x + width ||
                mouseY > y + height) {
            return null;
        }

        int lineHeight = 10;

        int relativeLine =
                (int) ((mouseY - y) / lineHeight);

        int lineIndex =
                firstVisibleLine() + relativeLine;

        if (relativeLine < 0 ||
                lineIndex < 0 ||
                lineIndex >= lines.size()) {
            return null;
        }

        String line = lines.get(lineIndex);
        int target =
                Math.max(
                        0,
                        (int) (
                                mouseX -
                                        x -
                                        36
                        )
                );

        int column = 0;

        while (column < line.length() &&
                textRenderer.getWidth(
                        line.substring(
                                0,
                                column + 1
                        )
                ) <= target) {
            column++;
        }

        int start = column;

        while (start > 0 &&
                Character.isJavaIdentifierPart(
                        line.charAt(start - 1)
                )) {
            start--;
        }

        int end = column;

        while (end < line.length() &&
                Character.isJavaIdentifierPart(
                        line.charAt(end)
                )) {
            end++;
        }

        if (start == end) {
            return null;
        }

        String token =
                line.substring(
                        start,
                        end
                );

        return switch (token) {
            case "Gafi" ->
                    "Gafi: main static entry point for the server API.";
            case "GafiWorld" ->
                    "GafiWorld: blocks, entities, dimensions, weather, time and regions.";
            case "GafiPlayer" ->
                    "GafiPlayer: player state, inventory, effects, messages and teleport.";
            case "GafiEntity" ->
                    "GafiEntity: generic server entity wrapper.";
            case "GafiScheduler" ->
                    "GafiScheduler: delayed, repeated, sequential and async execution.";
            case "GafiEvents" ->
                    "GafiEvents: lifecycle and player interaction listeners.";
            case "GafiCustomEvents" ->
                    "GafiCustomEvents: script-defined event channels.";
            case "GafiItemBuilder" ->
                    "GafiItemBuilder: Minecraft 1.21.11 data-component item builder.";
            case "GafiScoreboard" ->
                    "GafiScoreboard: objectives, scores and display slots.";
            case "GafiBossBar" ->
                    "GafiBossBar: server boss bar controls.";
            case "GafiGui" ->
                    "GafiGui: server-authoritative custom inventory GUI.";
            case "GafiDebugger" ->
                    "GafiDebugger: cooperative source-line breakpoint probes.";
            default ->
                    token.startsWith("on")
                            ? "Event callback: register a listener and keep its returned handle for manual cleanup."
                            : null;
        };
    }

    private String currentIdentifier() {
        String line = lines.get(cursorLine);

        int start =
                Math.min(
                        cursorColumn,
                        line.length()
                );

        while (start > 0 &&
                Character.isJavaIdentifierPart(
                        line.charAt(start - 1)
                )) {
            start--;
        }

        int end =
                Math.min(
                        cursorColumn,
                        line.length()
                );

        while (end < line.length() &&
                Character.isJavaIdentifierPart(
                        line.charAt(end)
                )) {
            end++;
        }

        return line.substring(start, end);
    }

    public void findNext(
            String query
    ) {
        if (query == null || query.isEmpty()) {
            return;
        }

        String text = getText();
        int start =
                hasSelection()
                        ? selectionEnd()
                        : cursorOffset();

        int index =
                text.indexOf(query, start);

        if (index < 0) {
            index = text.indexOf(query);
        }

        if (index >= 0) {
            selectionAnchor = index;
            setCursorFromOffset(
                    index + query.length(),
                    true
            );
        }
    }

    public void replaceCurrent(
            String query,
            String replacement
    ) {
        if (query == null || query.isEmpty()) {
            return;
        }

        if (!hasSelection() ||
                !selectedText().equals(query)) {
            findNext(query);
        }

        if (hasSelection() &&
                selectedText().equals(query)) {
            replaceSelectionWith(replacement);
        }
    }

    public int replaceAll(
            String query,
            String replacement
    ) {
        if (query == null ||
                query.isEmpty()) {
            return 0;
        }

        String text = getText();
        int count = 0;
        int from = 0;

        while (true) {
            int index = text.indexOf(query, from);

            if (index < 0) break;

            count++;
            from = index + query.length();
        }

        if (count == 0) {
            return 0;
        }

        pushUndo();

        setDocument(
                text.replace(query, replacement),
                0
        );

        redoStack.clear();

        return count;
    }

    public void formatJava() {
        pushUndo();

        String source = getText();
        StringBuilder output = new StringBuilder();
        int indent = 0;

        for (String rawLine :
                source.replace("\r\n", "\n")
                        .split("\n", -1)) {

            String line = rawLine.strip();

            if (line.startsWith("}")) {
                indent = Math.max(0, indent - 1);
            }

            output.append("    ".repeat(indent))
                    .append(line)
                    .append('\n');

            if (line.endsWith("{")) {
                indent++;
            }

            if (line.equals("}") ||
                    line.endsWith("};")) {
                indent = Math.max(0, indent - 1);
            }
        }

        setDocument(
                output.toString().replaceFirst("\\n$", ""),
                Math.min(
                        cursorOffset(),
                        output.length()
                )
        );

        redoStack.clear();
    }

    private int firstVisibleLine() {
        int lineHeight = 10;
        int maxLines = Math.max(1, height / lineHeight);
        int maxScroll = Math.max(0, lines.size() - maxLines);

        return Math.max(
                0,
                Math.min(
                        maxScroll,
                        scrollLine
                )
        );
    }

    private void drawSelectionHighlight(
            DrawContext context,
            int lineIndex,
            int drawX,
            int drawY
    ) {
        if (!hasSelection()) {
            return;
        }

        int lineStart = lineStartOffset(lineIndex);
        int lineEnd = lineStart + lines.get(lineIndex).length();
        int start = Math.max(selectionStart(), lineStart);
        int end = Math.min(selectionEnd(), lineEnd);

        if (start > end ||
                (start == end && selectionEnd() != lineEnd)) {
            return;
        }

        String line = lines.get(lineIndex);
        int startColumn = Math.max(0, start - lineStart);
        int endColumn = Math.max(startColumn, end - lineStart);

        int startX = drawX + textRenderer.getWidth(
                line.substring(0, Math.min(startColumn, line.length()))
        );
        int endX = drawX + textRenderer.getWidth(
                line.substring(0, Math.min(endColumn, line.length()))
        );

        if (endX <= startX) {
            endX = startX + 3;
        }

        context.fill(
                startX,
                drawY - 1,
                endX,
                drawY + 9,
                0xAA3A506B
        );
    }

    private int lineStartOffset(int lineIndex) {
        int offset = 0;

        for (int i = 0; i < lineIndex; i++) {
            offset += lines.get(i).length() + 1;
        }

        return offset;
    }

    public void render(
            DrawContext context
    ) {
        if (textRenderer == null) {
            return;
        }

        context.fill(
                x,
                y,
                x + width,
                y + height,
                0xDD101216
        );

        context.fill(
                x,
                y,
                x + 32,
                y + height,
                0xFF191C21
        );

        int lineHeight = 10;

        int maxLines =
                Math.max(
                        1,
                        height / lineHeight
                );

        int firstLine = firstVisibleLine();

        for (int visible = 0;
             visible < maxLines;
             visible++) {

            int lineIndex =
                    firstLine + visible;

            if (lineIndex >= lines.size()) {
                break;
            }

            int drawY =
                    y +
                    visible * lineHeight +
                    2;

            drawSelectionHighlight(
                    context,
                    lineIndex,
                    x + 36,
                    drawY
            );

            context.drawText(
                    textRenderer,
                    Integer.toString(lineIndex + 1),
                    x + 4,
                    drawY,
                    0xFF6E7580,
                    false
            );

            drawCodeLine(
                    context,
                    lines.get(lineIndex),
                    x + 36,
                    drawY
            );
        }

        int cursorY =
                y +
                (cursorLine - firstLine) *
                        lineHeight +
                1;

        int cursorX =
                x +
                36 +
                textRenderer.getWidth(
                        lines.get(cursorLine)
                                .substring(
                                        0,
                                        Math.min(
                                                cursorColumn,
                                                lines.get(cursorLine)
                                                        .length()
                                        )
                                )
                );

        context.fill(
                cursorX,
                cursorY,
                cursorX + 1,
                cursorY + 9,
                0xFFFFFFFF
        );

        if (completionVisible &&
                !completionItems.isEmpty()) {

            renderCompletion(
                    context,
                    cursorX,
                    cursorY
            );
        }

        if (!problems.isEmpty()) {
            int problemY =
                    y + height - 30;

            context.fill(
                    x + 36,
                    problemY,
                    x + width,
                    y + height,
                    0xEE2A1515
            );

            context.drawText(
                    textRenderer,
                    "Problems: " +
                            problems.getFirst(),
                    x + 40,
                    problemY + 6,
                    0xFFFF7777,
                    false
            );
        }
    }

    private void drawCodeLine(
            DrawContext context,
            String line,
            int drawX,
            int drawY
    ) {
        if (line.isBlank()) {
            return;
        }

        String trimmed = line.trim();
        int color = 0xFFE6E6E6;

        if (trimmed.startsWith("//")) {
            color = 0xFF6A9955;
        } else if (
                trimmed.startsWith("import ") ||
                trimmed.startsWith("package ")
        ) {
            color = 0xFF569CD6;
        } else if (
                KEYWORDS.stream()
                        .anyMatch(trimmed::startsWith)
        ) {
            color = 0xFF569CD6;
        }

        context.drawText(
                textRenderer,
                line,
                drawX,
                drawY,
                color,
                false
        );
    }

    private void renderCompletion(
            DrawContext context,
            int cursorX,
            int cursorY
    ) {
        int boxWidth =
                Math.min(
                        360,
                        width -
                                Math.max(
                                        0,
                                        cursorX - x
                                ) -
                                4
                );

        int rows =
                Math.min(
                        8,
                        completionItems.size()
                );

        int boxHeight =
                rows * 14 + 4;

        int boxX =
                Math.max(
                        x + 36,
                        Math.min(
                                cursorX,
                                x + width - boxWidth
                        )
                );

        int boxY =
                Math.min(
                        y + height - boxHeight,
                        cursorY + 10
                );

        context.fill(
                boxX,
                boxY,
                boxX + boxWidth,
                boxY + boxHeight,
                0xFF20242B
        );

        for (int i = 0; i < rows; i++) {
            boolean selected =
                    i == completionIndex;

            int rowY =
                    boxY +
                    2 +
                    i * 14;

            if (selected) {
                context.fill(
                        boxX,
                        rowY,
                        boxX + boxWidth,
                        rowY + 14,
                        0xFF3A4352
                );
            }

            context.drawText(
                    textRenderer,
                    completionItems.get(i),
                    boxX + 5,
                    rowY + 3,
                    0xFFE6E6E6,
                    false
            );
        }
    }

    private void updateCompletions() {
        String prefix = currentPrefix();
        String lower =
                prefix.toLowerCase(Locale.ROOT);

        if (lower.startsWith("gafi.scheduler().")) {
            completionItems = List.of(
                    "nextTick(",
                    "delayTicks(",
                    "delaySeconds(",
                    "repeatTicks(",
                    "repeatSeconds(",
                    "group(",
                    "sequence(",
                    "runAsync(",
                    "runSync(",
                    "supplyAsync("
            );
        } else if (lower.startsWith("gafi.events().")) {
            completionItems = List.of(
                    "onPlayerJoin(",
                    "onPlayerLeave(",
                    "onPlayerDeath(",
                    "onBlockBreak(",
                    "onBlockUse(",
                    "onTick("
            );
        } else if (lower.startsWith("gafi.commands().")) {
            completionItems =
                    List.of("register(");
        } else if (lower.startsWith("gafi.profiler().")) {
            completionItems =
                    List.of(
                            "measure(",
                            "snapshot(",
                            "snapshotAll(",
                            "reset("
                    );
        } else if (lower.startsWith("gafi.")) {
            completionItems = API_SUGGESTIONS.stream()
                    .filter(
                            item ->
                                    item.toLowerCase(
                                                    Locale.ROOT
                                            )
                                            .startsWith(lower)
                    )
                    .toList();
        } else {
            completionItems = KEYWORDS.stream()
                    .filter(
                            item ->
                                    item.startsWith(prefix)
                    )
                    .toList();
        }

        if (completionItems.isEmpty()) {
            completionItems =
                    API_SUGGESTIONS.stream()
                            .limit(8)
                            .toList();
        }

        completionIndex =
                Math.min(
                        completionIndex,
                        completionItems.size() - 1
                );
    }

    private String currentPrefix() {
        String line =
                lines.get(cursorLine);

        int end =
                Math.min(
                        cursorColumn,
                        line.length()
                );

        int start = end;

        while (start > 0) {
            char c =
                    line.charAt(start - 1);

            if (!(
                    Character.isJavaIdentifierPart(c) ||
                    c == '.' ||
                    c == '('
            )) {
                break;
            }

            start--;
        }

        return line.substring(
                start,
                end
        );
    }

    private void acceptCompletion() {
        if (completionItems.isEmpty()) {
            return;
        }

        String completion =
                completionItems.get(
                        Math.min(
                                completionIndex,
                                completionItems.size() - 1
                        )
                );

        String prefix =
                currentPrefix();

        if (!prefix.isEmpty()) {
            deleteCharacters(prefix.length());
        }

        replaceSelectionWith(completion);

        completionVisible = false;
    }

    private void enter() {
        String current =
                lines.get(cursorLine);

        String before =
                current.substring(
                        0,
                        cursorColumn
                );

        String after =
                current.substring(
                        cursorColumn
                );

        String indent =
                before.substring(
                        0,
                        before.length() -
                                before.stripLeading().length()
                );

        if (before.endsWith("{")) {
            indent += "    ";
        }

        replaceSelectionWith("");

        pushUndo();

        lines.set(
                cursorLine,
                before
        );

        lines.add(
                ++cursorLine,
                indent + after
        );

        cursorColumn =
                indent.length();

        setSelectionAnchor(-1);

        redoStack.clear();
        analyze();
    }

    private void backspace() {
        if (hasSelection()) {
            replaceSelectionWith("");
            return;
        }

        if (cursorColumn > 0) {
            pushUndo();

            String current =
                    lines.get(cursorLine);

            lines.set(
                    cursorLine,
                    current.substring(
                            0,
                            cursorColumn - 1
                    ) +
                    current.substring(
                            cursorColumn
                    )
            );

            cursorColumn--;
            redoStack.clear();
        } else if (cursorLine > 0) {
            pushUndo();

            String current =
                    lines.remove(cursorLine);

            cursorLine--;
            cursorColumn =
                    lines.get(cursorLine).length();

            lines.set(
                    cursorLine,
                    lines.get(cursorLine) +
                            current
            );

            redoStack.clear();
        }

        analyze();
    }

    private void delete() {
        if (hasSelection()) {
            replaceSelectionWith("");
            return;
        }

        String current =
                lines.get(cursorLine);

        if (cursorColumn <
                current.length()) {

            pushUndo();

            lines.set(
                    cursorLine,
                    current.substring(
                            0,
                            cursorColumn
                    ) +
                    current.substring(
                            cursorColumn + 1
                    )
            );

            redoStack.clear();
        } else if (
                cursorLine < lines.size() - 1
        ) {
            pushUndo();

            lines.set(
                    cursorLine,
                    current +
                            lines.remove(
                                    cursorLine + 1
                            )
            );

            redoStack.clear();
        }

        analyze();
    }

    private void deletePreviousWord() {
        if (hasSelection()) {
            replaceSelectionWith("");
            return;
        }

        String text = getText();
        int cursor = cursorOffset();
        int start = cursor;

        while (start > 0 &&
                Character.isWhitespace(
                        text.charAt(start - 1)
                )) {
            start--;
        }

        while (start > 0 &&
                !Character.isWhitespace(
                        text.charAt(start - 1)
                )) {
            start--;
        }

        selectRange(start, cursor);
        replaceSelectionWith("");
    }

    private void deleteCharacters(int count) {
        if (count <= 0) {
            return;
        }

        int start =
                Math.max(
                        0,
                        cursorOffset() - count
                );

        selectRange(
                start,
                cursorOffset()
        );

        replaceSelectionWith("");
    }

    private void moveHorizontal(
            int delta,
            boolean selecting
    ) {
        int line = cursorLine;
        int column = cursorColumn;

        if (delta < 0) {
            if (column > 0) {
                column--;
            } else if (line > 0) {
                line--;
                column =
                        lines.get(line).length();
            }
        } else {
            if (column <
                    lines.get(line).length()) {
                column++;
            } else if (line <
                    lines.size() - 1) {
                line++;
                column = 0;
            }
        }

        setCursor(
                line,
                column,
                selecting
        );
    }

    private void moveVertical(
            int delta,
            boolean selecting
    ) {
        int line =
                Math.max(
                        0,
                        Math.min(
                                lines.size() - 1,
                                cursorLine + delta
                        )
                );

        setCursor(
                line,
                Math.min(
                        cursorColumn,
                        lines.get(line).length()
                ),
                selecting
        );
    }

    private void moveToColumn(
            int column,
            boolean selecting
    ) {
        setCursor(
                cursorLine,
                Math.max(
                        0,
                        Math.min(
                                column,
                                lines.get(cursorLine).length()
                        )
                ),
                selecting
        );
    }

    private void moveLine(int delta) {
        int source =
                Math.max(
                        0,
                        Math.min(
                                lines.size() - 1,
                                cursorLine + delta
                        )
                );

        if (source == cursorLine) {
            return;
        }

        pushUndo();

        String value =
                lines.remove(cursorLine);

        lines.add(
                source,
                value
        );

        cursorLine = source;
        redoStack.clear();
    }

    private void setCursor(
            int line,
            int column,
            boolean selecting
    ) {
        if (selecting) {
            if (selectionAnchor < 0) {
                selectionAnchor =
                        cursorOffset();
            }
        } else {
            selectionAnchor = -1;
        }

        cursorLine =
                Math.max(
                        0,
                        Math.min(
                                lines.size() - 1,
                                line
                        )
                );

        ensureCursorVisible();

        cursorColumn =
                Math.max(
                        0,
                        Math.min(
                                lines.get(cursorLine).length(),
                                column
                        )
                );

        ensureCursorVisible();
    }

    private void setCursorFromOffset(
            int offset,
            boolean selecting
    ) {
        int clamped =
                Math.max(
                        0,
                        Math.min(
                                offset,
                                getText().length()
                        )
                );

        if (selecting &&
                selectionAnchor < 0) {
            selectionAnchor =
                    cursorOffset();
        }

        if (!selecting) {
            selectionAnchor = -1;
        }

        int remaining = clamped;

        for (int i = 0;
             i < lines.size();
             i++) {

            int lineLength =
                    lines.get(i).length();

            if (remaining <= lineLength) {
                cursorLine = i;
                cursorColumn = remaining;
                ensureCursorVisible();
                return;
            }

            remaining -= lineLength;

            if (i < lines.size() - 1) {
                remaining--;
            }
        }

        cursorLine =
                lines.size() - 1;

        cursorColumn =
                lines.get(cursorLine).length();

        ensureCursorVisible();
    }

    private void ensureCursorVisible() {
        int lineHeight = 10;
        int maxLines = Math.max(1, height / lineHeight);

        if (cursorLine < scrollLine) {
            scrollLine = cursorLine;
            return;
        }

        int lastVisible = scrollLine + maxLines - 1;

        if (cursorLine > lastVisible) {
            scrollLine =
                    Math.max(
                            0,
                            cursorLine - maxLines + 1
                    );
        }
    }

    private int cursorOffset() {
        int offset = 0;

        for (int i = 0;
             i < cursorLine;
             i++) {
            offset +=
                    lines.get(i).length();

            offset++;
        }

        return offset + cursorColumn;
    }

    private boolean hasSelection() {
        return selectionAnchor >= 0 &&
                selectionAnchor != cursorOffset();
    }

    private int selectionStart() {
        return Math.min(
                selectionAnchor,
                cursorOffset()
        );
    }

    private int selectionEnd() {
        return Math.max(
                selectionAnchor,
                cursorOffset()
        );
    }

    private void selectRange(
            int start,
            int end
    ) {
        selectionAnchor = start;
        setCursorFromOffset(end, true);
    }

    private void setSelectionAnchor(int value) {
        selectionAnchor = value;
    }

    private boolean lineIntersectsSelection(
            int lineIndex
    ) {
        if (!hasSelection()) {
            return false;
        }

        int startOffset = 0;

        for (int i = 0;
             i < lineIndex;
             i++) {
            startOffset +=
                    lines.get(i).length() +
                            1;
        }

        int endOffset =
                startOffset +
                lines.get(lineIndex).length();

        return endOffset >= selectionStart() &&
                startOffset <= selectionEnd();
    }

    private void pushUndo() {
        undoStack.push(captureState());

        while (undoStack.size() > 100) {
            undoStack.removeLast();
        }
    }

    private void undo() {
        if (undoStack.isEmpty()) {
            return;
        }

        redoStack.push(captureState());
        restoreState(undoStack.pop());
    }

    private void redo() {
        if (redoStack.isEmpty()) {
            return;
        }

        undoStack.push(captureState());
        restoreState(redoStack.pop());
    }

    private State captureState() {
        return new State(
                getText(),
                cursorOffset(),
                selectionAnchor
        );
    }

    private void restoreState(State state) {
        setDocument(
                state.text(),
                state.cursorOffset()
        );

        selectionAnchor =
                state.selectionAnchor();
    }

    private void setDocument(
            String text,
            int cursorOffset
    ) {
        lines.clear();
        String normalized = text
                .replace("\r\n", "\n")
                .replace('\r', '\n');
        lines.addAll(Arrays.asList(normalized.split("\n", -1)));

        if (lines.isEmpty()) {
            lines.add("");
        }

        setCursorFromOffset(
                Math.min(
                        cursorOffset,
                        text.length()
                ),
                false
        );

        analyze();
    }

    private void analyze() {
        problems.clear();

        String source = getText();

        if (source.contains(
                "sendMessag("
        )) {
            problems.add(
                    "Possible typo: did you mean sendMessage()?"
            );
        }

        if (source.contains(
                "while (true)"
        ) &&
                !source.contains("break;")) {
            problems.add(
                    "Potential infinite loop: while (true) without an obvious break."
            );
        }
    }

    private String clientClipboard(
            String replacement
    ) {
        net.minecraft.client.MinecraftClient client =
                net.minecraft.client.MinecraftClient.getInstance();

        if (replacement != null) {
            client.keyboard.setClipboard(
                    replacement
            );
            return replacement;
        }

        return client.keyboard.getClipboard();
    }

    private record State(
            String text,
            int cursorOffset,
            int selectionAnchor
    ) {}
}
