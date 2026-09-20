package com.gafipro.gafiscript.api;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.world.ServerWorld;

public final class GafiEntity {
    private final Entity handle;

    public GafiEntity(Entity handle) {
        this.handle = handle;
    }

    public String uuid() {
        return handle.getUuidAsString();
    }

    public String type() {
        return net.minecraft.registry.Registries.ENTITY_TYPE
                .getId(handle.getType())
                .toString();
    }

    public String name() {
        return handle.getName().getString();
    }

    public GafiPosition position() {
        return new GafiPosition(
                handle.getX(),
                handle.getY(),
                handle.getZ()
        );
    }

    public double distanceTo(GafiPosition position) {
        return position().distanceTo(position);
    }

    public boolean isAlive() {
        return handle.isAlive();
    }

    public boolean isRemoved() {
        return handle.isRemoved();
    }

    public double health() {
        return handle instanceof LivingEntity living
                ? living.getHealth()
                : 0.0;
    }

    public double maxHealth() {
        return handle instanceof LivingEntity living
                ? living.getMaxHealth()
                : 0.0;
    }

    public void setHealth(double health) {
        if (handle instanceof LivingEntity living) {
            Gafi.runSync(() ->
                    living.setHealth(
                            (float) Math.max(
                                    0,
                                    Math.min(
                                            health,
                                            living.getMaxHealth()
                                    )
                            )
                    )
            );
        }
    }

    public void velocity(double x, double y, double z) {
        Gafi.runSync(() ->
                handle.setVelocity(x, y, z)
        );
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

    public void remove() {
        Gafi.runSync(() ->
                handle.remove(
                        Entity.RemovalReason.DISCARDED
                )
        );
    }

    public GafiWorld world() {
        if (!(handle.getWorld() instanceof ServerWorld world)) {
            throw new IllegalStateException(
                    "Entity is not currently in a server world."
            );
        }

        return new GafiWorld(
                world.getServer(),
                world
        );
    }

    public Entity raw() {
        return handle;
    }
}
