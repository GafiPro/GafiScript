package com.gafipro.gafiscript.api;

import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;

public final class GafiPlayer {
    private final ServerPlayerEntity handle;

    public GafiPlayer(ServerPlayerEntity handle) {
        this.handle = handle;
    }

    public String name() {
        return handle.getGameProfile().name();
    }

    public String uuid() {
        return handle.getUuidAsString();
    }

    public GafiPosition position() {
        return new GafiPosition(handle.getX(), handle.getY(), handle.getZ());
    }

    public GafiWorld world() {
        ServerWorld world = handle.getCommandSource().getWorld();
        return new GafiWorld(world.getServer(), world);
    }

    public double health() {
        return handle.getHealth();
    }

    public void setHealth(double health) {
        Gafi.runSync(() -> handle.setHealth(
                (float) Math.max(0, Math.min(health, handle.getMaxHealth()))
        ));
    }

    public int level() {
        return handle.experienceLevel;
    }

    public void setLevel(int level) {
        Gafi.runSync(() -> handle.setExperienceLevel(Math.max(0, level)));
    }

    public boolean isSneaking() {
        return handle.isSneaking();
    }

    public boolean isSprinting() {
        return handle.isSprinting();
    }

    public boolean isFlying() {
        return handle.getAbilities().flying;
    }

    public boolean isOnGround() {
        return handle.isOnGround();
    }

    public void sendMessage(String message) {
        handle.sendMessage(Text.literal(message), false);
    }

    public void sendActionBar(String message) {
        handle.sendMessage(Text.literal(message), true);
    }

    public void sendTitle(String title) {
        GafiTitles.title(this, title);
    }

    public void sendSubtitle(String subtitle) {
        GafiTitles.subtitle(this, subtitle);
    }

    public GafiInventory inventory() {
        return new GafiInventory(handle.getInventory());
    }

    public void teleport(GafiPosition position) {
        Gafi.runSync(() ->
                handle.requestTeleport(
                        position.x(),
                        position.y(),
                        position.z()
                )
        );
    }

    public void giveItem(String itemId, int amount) {
        Gafi.runSync(() -> {
            var item = net.minecraft.registry.Registries.ITEM.get(
                    net.minecraft.util.Identifier.of(itemId)
            );

            if (item == Items.AIR) return;

            ItemStack stack = new ItemStack(
                    item,
                    Math.max(1, Math.min(amount, 64))
            );

            PlayerInventory inventory = handle.getInventory();
            if (!inventory.insertStack(stack)) {
                handle.dropItem(stack, false);
            }
        });
    }

    public void removeItem(String itemId, int amount) {
        Gafi.runSync(() -> {
            var item = net.minecraft.registry.Registries.ITEM.get(
                    net.minecraft.util.Identifier.of(itemId)
            );

            int remaining = Math.max(0, amount);
            PlayerInventory inventory = handle.getInventory();

            for (int slot = 0; slot < inventory.size() && remaining > 0; slot++) {
                ItemStack stack = inventory.getStack(slot);

                if (stack.isOf(item)) {
                    int take = Math.min(remaining, stack.getCount());
                    stack.decrement(take);
                    remaining -= take;
                }
            }
        });
    }

    public void addEffect(String effectId, int seconds, int amplifier) {
        Gafi.runSync(() -> {
            var entry = net.minecraft.registry.Registries.STATUS_EFFECT
                    .getEntry(net.minecraft.util.Identifier.of(effectId))
                    .orElse(null);

            if (entry != null) {
                handle.addStatusEffect(
                        new StatusEffectInstance(
                                entry,
                                Math.max(1, seconds * 20),
                                Math.max(0, amplifier)
                        )
                );
            }
        });
    }

    public ServerPlayerEntity raw() {
        return handle;
    }
}
