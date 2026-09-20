package com.gafipro.gafiscript.api;

import net.minecraft.util.hit.BlockHitResult;

public record GafiRaycastHit(
        boolean hit,
        GafiPosition position,
        String blockId,
        String side
) {
    public static GafiRaycastHit miss(GafiPosition position) {
        return new GafiRaycastHit(
                false,
                position,
                "minecraft:air",
                ""
        );
    }

    public static GafiRaycastHit from(BlockHitResult result) {
        var pos = result.getBlockPos();

        return new GafiRaycastHit(
                true,
                new GafiPosition(
                        result.getPos().x,
                        result.getPos().y,
                        result.getPos().z
                ),
                net.minecraft.registry.Registries.BLOCK
                        .getId(
                                result.getBlockPos() == null
                                        ? net.minecraft.block.Blocks.AIR
                                        : result.getBlockState()
                                                .getBlock()
                        )
                        .toString(),
                result.getSide().getName()
        );
    }
}
