# Storage

Use persistent JSON storage for script-owned data.

    var storage = Gafi.storage("race");

    storage.set("players.Gabriel.wins", 3);

    int wins =
        storage.getInt(
            "players.Gabriel.wins",
            0
        );

Data is stored under the server run directory in gafiscript/storage/.

Nested paths use dots.

Storage writes are synchronized and persisted immediately.
