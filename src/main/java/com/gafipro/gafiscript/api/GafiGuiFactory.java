package com.gafipro.gafiscript.api;

public final class GafiGuiFactory {
    public GafiGui create(String title, int rows) {
        return new GafiGui(title, rows);
    }

    public void closeOwnedBy(String scriptName) {
        GafiGui.closeOwnedBy(scriptName);
    }
}
