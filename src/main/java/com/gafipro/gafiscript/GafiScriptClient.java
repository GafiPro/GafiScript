package com.gafipro.gafiscript;

import com.gafipro.gafiscript.client.GafiScriptScreen;
import com.gafipro.gafiscript.net.GafiScriptNetworking;
import com.gafipro.gafiscript.registry.ModBlocks;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.ActionResult;

public final class GafiScriptClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        GafiScriptNetworking.registerClient();

        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (!world.isClient() ||
                    world.getBlockState(hitResult.getBlockPos()).getBlock() != ModBlocks.GAFI_SCRIPT_BLOCK) {
                return ActionResult.PASS;
            }

            MinecraftClient client = MinecraftClient.getInstance();
            client.setScreen(new GafiScriptScreen(hitResult.getBlockPos()));
            GafiScriptNetworking.sendScriptRequest(hitResult.getBlockPos());
            return ActionResult.SUCCESS;
        });
    }
}
