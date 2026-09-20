# Item API

Example:

    ItemStack reward =
        Gafi.items()
            .item("minecraft:diamond")
            .count(3)
            .name("§bReward")
            .lore("First line", "Second line")
            .customModelData(1234)
            .glint(true)
            .build();

The builder uses Minecraft 1.21.11 Data Components for custom name, item name, lore, custom model data, max stack size and glint override.

Advanced components can be supplied through component(type, value).
