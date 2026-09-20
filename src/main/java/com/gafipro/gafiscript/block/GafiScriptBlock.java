package com.gafipro.gafiscript.block;

import com.gafipro.gafiscript.registry.ModBlockEntities;
import com.gafipro.gafiscript.net.GafiScriptNetworking;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public final class GafiScriptBlock extends BlockWithEntity {
    public GafiScriptBlock(Settings settings) {
        super(settings);
    }

    @Override
    public @Nullable BlockEntity createBlockEntity(net.minecraft.util.math.BlockPos pos, BlockState state) {
        return new GafiScriptBlockEntity(pos, state);
    }

    @Override
    protected ActionResult onUse(
            BlockState state,
            World world,
            net.minecraft.util.math.BlockPos pos,
            PlayerEntity player,
            net.minecraft.util.Hand hand,
            BlockHitResult hit
    ) {
        if (world.isClient) {
            return ActionResult.SUCCESS;
        }

        if (player.hasPermissionLevel(2)) {
            GafiScriptNetworking.sendScriptRequest(player, pos);
        } else {
            player.sendMessage(net.minecraft.text.Text.literal("GafiScript: you need permission level 2."), false);
        }

        return ActionResult.SUCCESS;
    }
}
