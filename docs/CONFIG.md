# Config

Script configuration is stored separately from runtime storage.

    var config =
        Gafi.config("race");

    boolean enabled =
        config.getBoolean(
            "enabled",
            true
        );

    config.set(
        "finish-message",
        "Race complete!"
    );

Config files live under gafiscript/configs/.

Use config for editable server configuration and storage for runtime/player data.
