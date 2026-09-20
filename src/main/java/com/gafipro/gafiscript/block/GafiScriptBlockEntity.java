package com.gafipro.gafiscript.block;

import com.gafipro.gafiscript.api.GafiPosition;
import com.gafipro.gafiscript.registry.ModBlockEntities;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.math.BlockPos;

public final class GafiScriptBlockEntity extends BlockEntity {
    public static final int MAX_SOURCE_LENGTH = 120_000;

    private String scriptName = "Main";
    private String source =
            "import com.gafipro.gafiscript.api.GafiPosition;\n" +
            "import static com.gafipro.gafiscript.api.Gafi.*;\n\n" +
            "public class Main {\n" +
            "    public static void start() {\n" +
            "        broadcast(\"Hello from GafiScript!\");\n" +
            "        world().setBlock(GafiPosition.of(0, 64, 0), \"minecraft:gold_block\");\n" +
            "    }\n" +
            "}\n";

    public GafiScriptBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.GAFI_SCRIPT, pos, state);
    }

    public String getScriptName() {
        return scriptName;
    }

    public String getSource() {
        return source;
    }

    public void setScriptName(String scriptName) {
        String cleaned = scriptName == null ? "Main" : scriptName.trim();
        if (cleaned.isEmpty()) cleaned = "Main";
        this.scriptName = cleaned.replaceAll("[^A-Za-z0-9_]", "_");
        markDirty();
    }

    public void setSource(String source) {
        if (source == null) source = "";
        if (source.length() > MAX_SOURCE_LENGTH) {
            throw new IllegalArgumentException(
                    "Script exceeds " + MAX_SOURCE_LENGTH + " characters."
            );
        }
        this.source = source;
        markDirty();
    }

    @Override
    protected void writeData(WriteView view) {
        super.writeData(view);
        view.putString("script_name", scriptName);
        view.putString("source", source);
    }

    @Override
    protected void readData(ReadView view) {
        super.readData(view);
        scriptName = view.getString("script_name", "Main");
        source = view.getString("source", "");
    }
}
