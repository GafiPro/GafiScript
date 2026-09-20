package com.gafipro.gafiscript.registry;

import com.gafipro.gafiscript.GafiScriptMod;
import com.gafipro.gafiscript.block.GafiScriptBlock;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

public final class ModBlocks {
    public static final Identifier GAFI_SCRIPT_ID = GafiScriptMod.id("gafiscript_block");
    public static final RegistryKey<Block> GAFI_SCRIPT_KEY =
            RegistryKey.of(RegistryKeys.BLOCK, GAFI_SCRIPT_ID);

    public static final Block GAFI_SCRIPT_BLOCK = new GafiScriptBlock(
            AbstractBlock.Settings.create()
                    .registryKey(GAFI_SCRIPT_KEY)
                    .strength(2.0f)
    );

    private ModBlocks() {}

    public static void register() {
        Registry.register(Registries.BLOCK, GAFI_SCRIPT_KEY, GAFI_SCRIPT_BLOCK);

        RegistryKey<Item> itemKey = RegistryKey.of(RegistryKeys.ITEM, GAFI_SCRIPT_ID);
        Registry.register(
                Registries.ITEM,
                itemKey,
                new BlockItem(GAFI_SCRIPT_BLOCK, new Item.Settings().registryKey(itemKey))
        );

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.REDSTONE).register(entries ->
                entries.add(GAFI_SCRIPT_BLOCK));
    }
}
