package com.gafipro.gafiscript;

import com.gafipro.gafiscript.api.Gafi;
import com.gafipro.gafiscript.api.GafiEvents;
import com.gafipro.gafiscript.command.GafiScriptCommands;
import com.gafipro.gafiscript.net.GafiScriptNetworking;
import com.gafipro.gafiscript.registry.ModBlockEntities;
import com.gafipro.gafiscript.registry.ModBlocks;
import com.gafipro.gafiscript.runtime.ScriptManager;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class GafiScriptMod implements ModInitializer {
    public static final String MOD_ID = "gafiscript";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static Identifier id(String path) {
        return Identifier.of(MOD_ID, path);
    }

    @Override
    public void onInitialize() {
        ModBlocks.register();
        ModBlockEntities.register();
        GafiScriptNetworking.registerCommon();
        GafiScriptCommands.register();
        GafiEvents.register();
        GafiEvents.INSTANCE.onTick(event -> Gafi.scheduler().tick());

        ServerLifecycleEvents.SERVER_STARTED.register(Gafi::attachServer);
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            ScriptManager.stopAll();
            Gafi.detachServer(server);
        });

        LOGGER.info("GafiScript initialized.");
    }
}
