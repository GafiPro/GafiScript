# From command blocks to GafiScript

GafiScript replaces long command-block chains with normal Java code and a Minecraft API.

## Set a block

Command:
    /setblock 10 64 10 minecraft:redstone_block

GafiScript:
    world.setBlock(
        GafiPosition.of(10, 64, 10),
        "minecraft:redstone_block"
    );

## Fill a region

Command:
    /fill 0 64 0 10 65 10 air

GafiScript:
    world.fill(
        GafiPosition.of(0, 64, 0),
        GafiPosition.of(10, 65, 10),
        "minecraft:air"
    );

## Give an item

GafiScript:
    player.giveItem("minecraft:diamond", 5);

## Teleport

GafiScript:
    player.teleport(
        GafiPosition.of(0, 100, 0)
    );

## Delayed action

Instead of a chain of timed command blocks:

    Gafi.scheduler().delaySeconds(3, () -> {
        Gafi.broadcast("Acabou o tempo!");
    });

## Event-driven logic

Instead of repeatedly checking a block:

    Gafi.events().onBlockUse(event -> {
        Gafi.broadcast("Bloco usado!");
    });
