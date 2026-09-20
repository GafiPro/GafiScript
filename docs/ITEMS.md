# Item API

A API pública atual suporta operações simples de inventário:

    player.giveItem("minecraft:diamond", 5);

    player.removeItem("minecraft:diamond", 1);

Também existe GafiInventory para consultar e editar o inventário do jogador.

O ItemBuilder completo da especificação, com data components, lore, enchantments, attributes, custom model data e consumables, ainda não deve ser tratado como implemented até ser testado contra as mappings exatas do Minecraft 1.21.11.
