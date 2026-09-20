# Threading

Minecraft has thread-affinity constraints.

## Server thread

World and player state should be changed from the server thread.

    Gafi.scheduler().runSync(() -> {
        Gafi.world().setBlock(
            GafiPosition.of(0, 64, 0),
            "minecraft:stone"
        );
    });

## Async work

Use asynchronous execution for calculations that do not directly manipulate Minecraft state.

    Gafi.scheduler().runAsync(() -> {
        String value = expensiveCalculation();

        Gafi.scheduler().runSync(() -> {
            Gafi.broadcast(value);
        });
    });

## Do not block ticks

Avoid Thread.sleep(...) inside a server callback.

Use Gafi.scheduler().delaySeconds(...).

## Race conditions

When data is shared between async work and the server thread, use normal Java concurrency primitives and minimise shared mutable state.

## Important limitation

GafiScript currently provides a lightweight scheduler and runtime. It is not a full actor system and does not automatically make arbitrary Java code thread-safe.
