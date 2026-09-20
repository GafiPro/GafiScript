package com.gafipro.gafiscript.api;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.hit.BlockHitResult;

public record GafiRaycastHit(
        boolean hit,
        GafiPosition position,
        String blockId,
        String side
) {
    public static GafiRaycastHit miss(
            GafiPosition position
    ) {
        return new GafiRaycastHit(
                false,
                position,
                "minecraft:air",
                ""
        );
    }

    public static GafiRaycastHit from(
            BlockHitResult result,
            ServerWorld world
    ) {
        var pos = result.getBlockPos();
        var state = world.getBlockState(pos);

        return new GafiRaycastHit(
                true,
                new GafiPosition(
                        result.getPos().x,
                        result.getPos().y,
                        result.getPos().z
                ),
                net.minecraft.registry.Registries.BLOCK
                        .getId(state.getBlock())
                        .toString(),
                result.getSide().getName()
        );
    }
}
