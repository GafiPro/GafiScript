package com.gafipro.gafiscript.block;

import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public final class GafiScriptBlock extends BlockWithEntity {
    public GafiScriptBlock(Settings settings) {
        super(settings);
    }

    @Override
    public @Nullable BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new GafiScriptBlockEntity(pos, state);
    }

    @Override
    protected ActionResult onUse(
            BlockState state,
            World world,
            BlockPos pos,
            PlayerEntity player,
            net.minecraft.util.Hand hand,
            BlockHitResult hit
    ) {
        if (!world.isClient && !player.hasPermissionLevel(2)) {
            player.sendMessage(
                    net.minecraft.text.Text.literal("GafiScript: you need permission level 2."),
                    false
            );
        }
        return ActionResult.SUCCESS;
    }
}
