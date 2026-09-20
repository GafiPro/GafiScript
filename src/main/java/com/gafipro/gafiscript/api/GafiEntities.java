package com.gafipro.gafiscript.api;

import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Box;

import java.util.List;

public final class GafiEntities {
    private final ServerWorld world;

    GafiEntities(ServerWorld world) {
        this.world = world;
    }

    public List<GafiEntity> near(
            GafiPosition center,
            double radius
    ) {
        double r = Math.max(0, radius);

        Box box = new Box(
                center.x() - r,
                center.y() - r,
                center.z() - r,
                center.x() + r,
                center.y() + r,
                center.z() + r
        );

        return world.getEntitiesByClass(
                        Entity.class,
                        box,
                        entity -> true
                )
                .stream()
                .map(GafiEntity::new)
                .toList();
    }

    public List<GafiEntity> all() {
        return world.iterateEntities()
                .stream()
                .map(GafiEntity::new)
                .toList();
    }

    public GafiEntity nearest(
            GafiPosition center,
            double radius
    ) {
        return near(center, radius)
                .stream()
                .min(java.util.Comparator.comparingDouble(
                        entity -> entity.distanceTo(center)
                ))
                .orElse(null);
    }
}
