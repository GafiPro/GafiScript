# GUI API

Scripts can create a server-authoritative inventory GUI.

    var gui =
        Gafi.gui()
            .create(
                "Race Controls",
                3
            );

    gui.setItem(
        13,
        Gafi.items()
            .item("minecraft:emerald")
            .name("Start")
            .build()
    );

    gui.onClick(event -> {
        if (event.slot() == 13) {
            event.cancel();
            Gafi.broadcast("Start!");
        }
    });

    gui.open(player);

GUIs are owned by the creating script and are closed during lifecycle cleanup.
