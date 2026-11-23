package com.sugarplumstudios.fairygarden.config;

import net.minecraftforge.common.ForgeConfigSpec;

public final class FairyConfig {
    public static final ForgeConfigSpec COMMON_SPEC;
    public static final ForgeConfigSpec CLIENT_SPEC;

    // Common (world generation; read once while the mod sets up)
    public static final ForgeConfigSpec.IntValue REGION_WEIGHT;
    public static final ForgeConfigSpec.BooleanValue BLOSSOM_VALLEY;
    public static final ForgeConfigSpec.BooleanValue LAVENDER_FIELDS;
    public static final ForgeConfigSpec.BooleanValue STRAWBERRY_MEADOW;
    public static final ForgeConfigSpec.BooleanValue MOONLIGHT_FOREST;
    public static final ForgeConfigSpec.BooleanValue FAIRY_GLADE;

    // Client
    public static final ForgeConfigSpec.BooleanValue AMBIENT_PARTICLES;
    public static final ForgeConfigSpec.DoubleValue AMBIENT_PARTICLE_DENSITY;

    static {
        ForgeConfigSpec.Builder common = new ForgeConfigSpec.Builder();
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
        COMMON_SPEC = common.build();

        ForgeConfigSpec.Builder client = new ForgeConfigSpec.Builder();
        client.push("ambience");
        AMBIENT_PARTICLES = client
                .comment("Drifting petals, butterflies, moonlight motes and fairy sparkles in Fairy Garden biomes.")
                .define("ambientParticles", true);
        AMBIENT_PARTICLE_DENSITY = client.comment("Multiplier for how many ambient particles appear.")
                .defineInRange("ambientParticleDensity", 1.0, 0.0, 3.0);
        client.pop();
        CLIENT_SPEC = client.build();
    }

    private FairyConfig() {
    }
}
