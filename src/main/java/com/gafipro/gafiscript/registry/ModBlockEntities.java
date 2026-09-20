package com.gafipro.gafiscript.registry;

import com.gafipro.gafiscript.GafiScriptMod;
import com.gafipro.gafiscript.block.GafiScriptBlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public final class ModBlockEntities {
    public static final BlockEntityType<GafiScriptBlockEntity> GAFI_SCRIPT =
            BlockEntityType.Builder.create(
                    GafiScriptBlockEntity::new,
                    ModBlocks.GAFI_SCRIPT_BLOCK
            ).build();

    private ModBlockEntities() {}

    public static void register() {
        Registry.register(
                Registries.BLOCK_ENTITY_TYPE,
                GafiScriptMod.id("gafiscript_block"),
                GAFI_SCRIPT
        );
    }
}
