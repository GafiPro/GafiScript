package dev.gafipro.gafishader;
import net.minecraft.text.Text;
import java.util.LinkedHashMap;
import java.util.Map;

public final class ComplementaryCatalog {
    private static final Map<String, String> GROUPS = new LinkedHashMap<>();
    static {
        GROUPS.put("atmosphere", "NIGHT_STAR_AMOUNT, AURORA_STYLE_DEFINE, AURORA_CONDITION, NIGHT_NEBULAE, NIGHT_NEBULA_I, RAINBOWS, RAINBOW_STYLE_DEFINE");
        GROUPS.put("clouds", "CLOUD_STYLE_DEFINE, CLOUD_ALT1, CLOUD_SPEED_MULT, CLOUD_SHADOWS, CLOUD_UNBOUND_AMOUNT, CLOUD_UNBOUND_SIZE_MULT, DOUBLE_REIM_CLOUDS, CLOUD_ALT2");
        GROUPS.put("fog", "BORDER_FOG, CAVE_FOG, ATM_FOG_MULT, ATM_FOG_DISTANCE, ATM_FOG_ALTITUDE, LIGHTSHAFT_BEHAVIOUR, LIGHTSHAFT_SMOKE, LIGHTSHAFT_SUNSET_SATURATION");
        GROUPS.put("sunmoon", "SUN_MOON_STYLE_DEFINE, SUN_ANGLE, SUN_MOON_HORIZON, SUN_MOON_DURING_RAIN");
        GROUPS.put("weather", "RAIN_STYLE, SPECIAL_BIOME_WEATHER, WEATHER_TEX_OPACITY, IMPROVED_RAIN_DEFINE, SUN_MOON_DURING_RAIN");
        GROUPS.put("water", "WATER_STYLE_DEFINE, WATER_CAUSTIC_STYLE_DEFINE, WATER_ALPHA_MULT, WATER_FOG_MULT, WATER_FOAM_I, WATER_REFRACTION_INTENSITY, WAVING_WATER_VERTEX");
        GROUPS.put("materials", "emissive/glowing materials, PBR settings and glowing ores/blocks");
        GROUPS.put("camera", "BLOOM, VIGNETTE, MOTION_BLUR, LENS_FLARE and related camera options");
        GROUPS.put("color", "EXPOSURE, CONTRAST, SATURATION, VIBRANCE and color grading");
        GROUPS.put("dimensions", "End and Nether atmosphere, sky, fog and dimension-specific effects");
    }
    private ComplementaryCatalog() {}

    public static String helpText() {
        StringBuilder builder = new StringBuilder("Complementary Reimagined known option groups:");
        for (Map.Entry<String, String> entry : GROUPS.entrySet()) builder.append("\n- ").append(entry.getKey()).append(": ").append(entry.getValue());
        builder.append("\n\nDirect setters for arbitrary shader-pack options are not faked: current Iris public API exposes shader control/config access and its GUI, but not a stable public setter for every pack option.");
        return builder.toString();
    }

    public static Text groupText(String group) {
        String value = GROUPS.get(group.toLowerCase());
        if (value == null) return Text.literal("Grupo desconhecido. Usa /gafishader complementary");
        return Text.literal(group + ": " + value);
    }
}
