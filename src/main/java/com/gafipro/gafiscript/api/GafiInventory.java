package com.gafipro.gafiscript.api;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;

public final class GafiInventory {
    private final PlayerInventory inventory;

    GafiInventory(PlayerInventory inventory) {
        this.inventory = inventory;
    }

    public int size() {
        return inventory.size();
    }

    public ItemStack get(int slot) {
        return inventory.getStack(slot).copy();
    }

    public void set(int slot, ItemStack stack) {
        Gafi.runSync(() ->
                inventory.setStack(
                        slot,
                        stack == null
                                ? ItemStack.EMPTY
                                : stack.copy()
                )
        );
    }

    public boolean insert(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }

        return inventory.insertStack(
                stack.copy()
        );
    }

    public void clear() {
        Gafi.runSync(inventory::clear);
    }

    public int count(String itemId) {
        var item =
                net.minecraft.registry.Registries.ITEM.get(
                        net.minecraft.util.Identifier.of(itemId)
                );

        int count = 0;

        for (int slot = 0; slot < inventory.size(); slot++) {
            ItemStack stack = inventory.getStack(slot);
            if (stack.isOf(item)) {
                count += stack.getCount();
            }
        }

        return count;
    }

    public boolean contains(String itemId) {
        return count(itemId) > 0;
    }

    public PlayerInventory raw() {
        return inventory;
    }
}
