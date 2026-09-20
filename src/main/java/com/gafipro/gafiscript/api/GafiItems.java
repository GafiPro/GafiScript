package com.gafipro.gafiscript.api;

public final class GafiItems {
    private GafiItems() {}

    public static GafiItemBuilder item(
            String itemId
    ) {
        return GafiItemBuilder.of(itemId);
    }
}
