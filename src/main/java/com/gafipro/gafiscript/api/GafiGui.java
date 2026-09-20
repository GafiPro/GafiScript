package com.gafipro.gafiscript.api;

import com.gafipro.gafiscript.runtime.GafiScriptContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public final class GafiGui {
    private static final Set<GafiGui> ACTIVE =
            ConcurrentHashMap.newKeySet();

    private final String ownerScript;
    private final String title;
    private final int rows;
    private final SimpleInventory inventory;

    private Consumer<GafiGuiClickEvent> clickListener;
    private Consumer<GafiGuiCloseEvent> closeListener;

    private final Map<ServerPlayerEntity, GenericContainerScreenHandler> openHandlers =
            new ConcurrentHashMap<>();

    private volatile boolean closing;

    public GafiGui(String title, int rows) {
        if (rows < 1 || rows > 6) {
            throw new IllegalArgumentException(
                    "GUI rows must be between 1 and 6."
            );
        }

        this.ownerScript =
                GafiScriptContext.currentScript();
        this.title =
                title == null ? "" : title;
        this.rows = rows;
        this.inventory =
                new SimpleInventory(rows * 9);
    }

    public String title() {
        return title;
    }

    public int rows() {
        return rows;
    }

    public int size() {
        return inventory.size();
    }

    public GafiGui title(String newTitle) {
        return new GafiGui(
                newTitle,
                rows
        );
    }

    public GafiGui setItem(
            int slot,
            ItemStack stack
    ) {
        checkSlot(slot);
        inventory.setStack(
                slot,
                stack == null
                        ? ItemStack.EMPTY
                        : stack.copy()
        );
        return this;
    }

    public GafiGui clear() {
        inventory.clear();
        return this;
    }

    public GafiGui fill(ItemStack stack) {
        for (int slot = 0; slot < inventory.size(); slot++) {
            inventory.setStack(
                    slot,
                    stack == null
                            ? ItemStack.EMPTY
                            : stack.copy()
            );
        }
        return this;
    }

    public ItemStack item(int slot) {
        checkSlot(slot);
        return inventory.getStack(slot).copy();
    }

    public GafiGui onClick(
            Consumer<GafiGuiClickEvent> listener
    ) {
        this.clickListener = listener;
        return this;
    }

    public GafiGui onClose(
            Consumer<GafiGuiCloseEvent> listener
    ) {
        this.closeListener = listener;
        return this;
    }

    public GafiGui open(GafiPlayer player) {
        ServerPlayerEntity raw = player.raw();

        raw.openHandledScreen(
                new SimpleNamedScreenHandlerFactory(
                        (syncId, playerInventory, serverPlayer) ->
                                createHandler(
                                        syncId,
                                        playerInventory,
                                        serverPlayer
                                ),
                        Text.literal(title)
                )
        );

        ACTIVE.add(this);
        return this;
    }

    public void close() {
        closing = true;
        try {
            for (var entry : openHandlers.entrySet()) {
                ServerPlayerEntity player = entry.getKey();
                GenericContainerScreenHandler handler = entry.getValue();

                if (player.currentScreenHandler == handler) {
                    player.closeHandledScreen();
                }
            }

            openHandlers.clear();
            ACTIVE.remove(this);
        } finally {
            closing = false;
        }
    }

    public String ownerScript() {
        return ownerScript;
    }

    private GenericContainerScreenHandler createHandler(
            int syncId,
            PlayerInventory playerInventory,
            PlayerEntity player
    ) {
        GenericContainerScreenHandler handler =
                new GenericContainerScreenHandler(
                switch (rows) {
                    case 1 -> ScreenHandlerType.GENERIC_9X1;
                    case 2 -> ScreenHandlerType.GENERIC_9X2;
                    case 3 -> ScreenHandlerType.GENERIC_9X3;
                    case 4 -> ScreenHandlerType.GENERIC_9X4;
                    case 5 -> ScreenHandlerType.GENERIC_9X5;
                    default -> ScreenHandlerType.GENERIC_9X6;
                },
                syncId,
                playerInventory,
                inventory,
                rows
        ) {
            {
                if (player instanceof ServerPlayerEntity serverPlayer) {
                    openHandlers.put(serverPlayer, this);
                }
            }

            @Override
            public void onSlotClick(
                    int slotId,
                    int button,
                    SlotActionType actionType,
                    PlayerEntity clicker
            ) {
                GafiGuiClickEvent event =
                        new GafiGuiClickEvent(
                                GafiGui.this,
                                clicker instanceof ServerPlayerEntity serverPlayer
                                        ? new GafiPlayer(serverPlayer)
                                        : null,
                                slotId,
                                button,
                                actionType
                        );

                if (clickListener != null) {
                    runOwned(() ->
                            clickListener.accept(event)
                    );
                }

                if (!event.isCancelled()) {
                    super.onSlotClick(
                            slotId,
                            button,
                            actionType,
                            clicker
                    );
                }
            }

            @Override
            public void onClosed(PlayerEntity closingPlayer) {
                super.onClosed(closingPlayer);

                if (closingPlayer instanceof ServerPlayerEntity serverPlayer) {
                    openHandlers.remove(serverPlayer, this);

                    if (!closing &&
                            closeListener != null) {
                        runOwned(() ->
                                closeListener.accept(
                                        new GafiGuiCloseEvent(
                                                GafiGui.this,
                                                new GafiPlayer(serverPlayer)
                                        )
                                )
                        );
                    }
                }

                if (openHandlers.isEmpty()) {
                    ACTIVE.remove(GafiGui.this);
                }
            }
        };

        return handler;
    }

    private void runOwned(Runnable action) {
        if (ownerScript == null) {
            action.run();
        } else {
            GafiScriptContext.runAs(
                    ownerScript,
                    action
            );
        }
    }

    private void checkSlot(int slot) {
        if (slot < 0 || slot >= inventory.size()) {
            throw new IndexOutOfBoundsException(
                    "GUI slot: " + slot
            );
        }
    }

    public static void closeOwnedBy(String scriptName) {
        ACTIVE.removeIf(gui -> {
            if (!java.util.Objects.equals(
                    scriptName,
                    gui.ownerScript()
            )) {
                return false;
            }

            gui.close();
            return true;
        });
    }

    public static final class GafiGuiClickEvent {
        private final GafiGui gui;
        private final GafiPlayer player;
        private final int slot;
        private final int button;
        private final SlotActionType actionType;
        private boolean cancelled;

        public GafiGuiClickEvent(
                GafiGui gui,
                GafiPlayer player,
                int slot,
                int button,
                SlotActionType actionType
        ) {
            this.gui = gui;
            this.player = player;
            this.slot = slot;
            this.button = button;
            this.actionType = actionType;
        }

        public GafiGui gui() { return gui; }
        public GafiPlayer player() { return player; }
        public int slot() { return slot; }
        public int button() { return button; }
        public SlotActionType actionType() { return actionType; }
        public boolean isCancelled() { return cancelled; }
        public void cancel() { cancelled = true; }
    }

    public record GafiGuiCloseEvent(
            GafiGui gui,
            GafiPlayer player
    ) {}
}
