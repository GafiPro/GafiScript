# Safe coding

## Avoid infinite loops

Do not do this on the server thread:

    while (true) {
    }

Prefer event-driven code or controlled tasks.

## Use delays instead of blocking

Do not block a server callback with Thread.sleep(...).

Use:

    Gafi.delaySeconds(3, () -> {
        // next phase
    });

## Cancel repeating work

Keep the returned task:

    GafiTask task =
        Gafi.repeatSeconds(1, this::update);

When the work is no longer needed:

    task.cancel();

## Limit region operations

Large fill operations are expensive. The initial World API enforces a 250,000-block limit.

## Check nullable lookups

Player lookup can return null:

    GafiPlayer player = Gafi.player("Gabriel");

    if (player == null) {
        return;
    }

## Be careful with async work

Do not directly mutate Minecraft world state from arbitrary worker code.

Calculate asynchronously, then return to the server thread.

## Avoid retained references

Do not keep unnecessary references to players or entities forever. Prefer UUIDs or short-lived lookups for persistent systems.

## Cleanup on reload

Future script lifecycle APIs will associate tasks/listeners with script ownership. Until then, explicitly cancel resources you create.

## Security

Do not try to bypass GafiScript's security checks. The runtime intentionally rejects several dangerous Java facilities.
