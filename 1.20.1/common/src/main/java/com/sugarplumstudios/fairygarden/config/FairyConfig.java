package com.sugarplumstudios.fairygarden.config;

import com.sugarplumstudios.fairygarden.platform.Services;

/**
 * The mod's two config files, {@code config/fairygarden-common.toml} (world generation) and
 * {@code config/fairygarden-client.toml}. Same layout and keys as the Forge 1.0 files, on every loader and version.
 */
public final class FairyConfig {
    private static final TomlConfig.Builder COMMON = new TomlConfig.Builder();

    // Common (world generation; read once while the mod sets up)
    public static final TomlConfig.IntValue REGION_WEIGHT;
    public static final TomlConfig.BooleanValue BLOSSOM_VALLEY;
    public static final TomlConfig.BooleanValue LAVENDER_FIELDS;
    public static final TomlConfig.BooleanValue STRAWBERRY_MEADOW;
    public static final TomlConfig.BooleanValue MOONLIGHT_FOREST;
    public static final TomlConfig.BooleanValue FAIRY_GLADE;
    private static final TomlConfig COMMON_FILE;

    private static final TomlConfig.Builder CLIENT = new TomlConfig.Builder();

    // Client
    public static final TomlConfig.BooleanValue AMBIENT_PARTICLES;
    public static final TomlConfig.DoubleValue AMBIENT_PARTICLE_DENSITY;
    private static final TomlConfig CLIENT_FILE;

    static {
        TomlConfig.Builder common = COMMON;
        common.comment("Fairy Garden biome generation. Changes apply to newly generated chunks after a restart.").push("worldgen");
        REGION_WEIGHT = common
                .comment("How often the Fairy Garden region appears compared with other TerraBlender regions.",
                        "Vanilla's own region has weight 10 by default. Higher = more fairy biomes. 0 turns the biomes off.")
                .defineInRange("regionWeight", 8, 0, 100);
        BLOSSOM_VALLEY = common.comment("Blossom Valley (replaces meadows, cherry groves and birch forests in the region).")
                .define("blossomValley", true);
        LAVENDER_FIELDS = common.comment("Lavender Fields (replaces sunflower plains and savannas in the region).")
                .define("lavenderFields", true);
        STRAWBERRY_MEADOW = common.comment("Strawberry Meadow (replaces plains in the region).")
                .define("strawberryMeadow", true);
        MOONLIGHT_FOREST = common.comment("Moonlight Forest (replaces dark forests in the region).")
                .define("moonlightForest", true);
        FAIRY_GLADE = common.comment("Fairy Glade (replaces flower forests and old growth birch forests in the region).")
                .define("fairyGlade", true);
        common.pop();
        COMMON_FILE = common.build("fairygarden-common.toml");

        TomlConfig.Builder client = CLIENT;
        client.push("ambience");
        AMBIENT_PARTICLES = client
                .comment("Drifting petals, butterflies, moonlight motes and fairy sparkles in Fairy Garden biomes.")
                .define("ambientParticles", true);
        AMBIENT_PARTICLE_DENSITY = client.comment("Multiplier for how many ambient particles appear.")
                .defineInRange("ambientParticleDensity", 1.0, 0.0, 3.0);
        client.pop();
        CLIENT_FILE = client.build("fairygarden-client.toml");
    }

    private FairyConfig() {
    }

    /** World generation settings; read when the mod starts. */
    public static void loadCommon() {
        COMMON_FILE.load(Services.PLATFORM.configDir());
    }

    /** Client options; read when the client starts. */
    public static void loadClient() {
        CLIENT_FILE.load(Services.PLATFORM.configDir());
    }
}
