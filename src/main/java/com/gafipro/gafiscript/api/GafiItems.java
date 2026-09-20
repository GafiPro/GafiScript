package com.gafipro.gafiscript.api;

public final class GafiItems {
    public GafiItems() {}

    public static GafiItemBuilder item(
            String itemId
    ) {
        return GafiItemBuilder.of(itemId);
    }
}
