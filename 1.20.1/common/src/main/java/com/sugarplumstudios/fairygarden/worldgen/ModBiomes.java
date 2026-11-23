package com.sugarplumstudios.fairygarden.worldgen;

import com.sugarplumstudios.fairygarden.FairyGarden;
import com.sugarplumstudios.fairygarden.registry.ModEntities;
import com.sugarplumstudios.fairygarden.registry.ModSounds;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BiomeDefaultFeatures;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.data.worldgen.placement.VegetationPlacements;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.Musics;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.AmbientAdditionsSettings;
import net.minecraft.world.level.biome.AmbientMoodSettings;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.biome.BiomeSpecialEffects;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import com.sugarplumstudios.fairygarden.platform.RegistryEntry;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * The five Fairy Garden biomes. Palettes are deliberately mixed (pink, lavender, cream, mint, white, soft blue)
 * so no biome reads as "everything pink". Vanilla features are added in vanilla's own order and all Fairy Garden
 * vegetation comes first, in one global order, which keeps the feature-order graph acyclic across all biomes.
 */
public final class ModBiomes {
    public static final ResourceKey<Biome> BLOSSOM_VALLEY = key("blossom_valley");
    public static final ResourceKey<Biome> LAVENDER_FIELDS = key("lavender_fields");
    public static final ResourceKey<Biome> STRAWBERRY_MEADOW = key("strawberry_meadow");
    public static final ResourceKey<Biome> MOONLIGHT_FOREST = key("moonlight_forest");
    public static final ResourceKey<Biome> FAIRY_GLADE = key("fairy_glade");
    public static final List<ResourceKey<Biome>> ALL = List.of(BLOSSOM_VALLEY, LAVENDER_FIELDS, STRAWBERRY_MEADOW, MOONLIGHT_FOREST, FAIRY_GLADE);

    public static void bootstrap(BootstapContext<Biome> context) {
        HolderGetter<PlacedFeature> placed = context.lookup(Registries.PLACED_FEATURE);
        HolderGetter<ConfiguredWorldCarver<?>> carvers = context.lookup(Registries.CONFIGURED_CARVER);
        context.register(BLOSSOM_VALLEY, blossomValley(placed, carvers));
        context.register(LAVENDER_FIELDS, lavenderFields(placed, carvers));
        context.register(STRAWBERRY_MEADOW, strawberryMeadow(placed, carvers));
        context.register(MOONLIGHT_FOREST, moonlightForest(placed, carvers));
        context.register(FAIRY_GLADE, fairyGlade(placed, carvers));
    }

    // ---------------------------------------------------------------------------------------------- biomes

    private static Biome blossomValley(HolderGetter<PlacedFeature> placed, HolderGetter<ConfiguredWorldCarver<?>> carvers) {
        BiomeGenerationSettings.Builder gen = baseGeneration(placed, carvers);
        gen.addFeature(GenerationStep.Decoration.LAKES, ModPlacedFeatures.STREAM);
        ores(gen);
        vegetation(gen, ModPlacedFeatures.TREES_BLOSSOM_VALLEY, ModPlacedFeatures.PATCH_BLOSSOM_PETALS, ModPlacedFeatures.PATCH_SNOW_BLOSSOM_PETALS,
                ModPlacedFeatures.FLOWERS_BLOSSOM_VALLEY, ModPlacedFeatures.PATCH_FAIRY_FOXGLOVE, ModPlacedFeatures.PATCH_HYDRANGEA,
                ModPlacedFeatures.PATCH_BLUSH_GRASS, ModPlacedFeatures.CARPET_MEADOW);
        gen.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.PATCH_GRASS_PLAIN);
        vanillaFinish(gen);

        MobSpawnSettings.Builder spawns = baseSpawns();
        spawns.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.RABBIT, 4, 2, 3));
        spawns.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.HORSE, 2, 2, 4));
        butterflies(spawns, 14);

        return biome(0.7F, 0.8F, spawns, gen, new BiomeSpecialEffects.Builder()
                .skyColor(0x9DD7FF).fogColor(0xF6E4EE).waterColor(0x7FD9E8).waterFogColor(0x3F8FA8)
                .grassColorOverride(0x9BD67E).foliageColorOverride(0xA5D98A), null, ModSounds.BLOSSOM_VALLEY_ADDITIONS, 0.008);
    }

    private static Biome lavenderFields(HolderGetter<PlacedFeature> placed, HolderGetter<ConfiguredWorldCarver<?>> carvers) {
        BiomeGenerationSettings.Builder gen = baseGeneration(placed, carvers);
        ores(gen);
        vegetation(gen, ModPlacedFeatures.LAVENDER_FIELD, ModPlacedFeatures.TREES_LAVENDER_FIELDS, ModPlacedFeatures.FLOWERS_LAVENDER_FIELDS,
                ModPlacedFeatures.PATCH_LAVENDER, ModPlacedFeatures.CARPET_LAVENDER);
        gen.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.PATCH_GRASS_PLAIN);
        vanillaFinish(gen);

        MobSpawnSettings.Builder spawns = baseSpawns();
        spawns.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.HORSE, 4, 2, 5));
        spawns.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.DONKEY, 1, 1, 2));
        butterflies(spawns, 20);

        return biome(0.8F, 0.4F, spawns, gen, new BiomeSpecialEffects.Builder()
                .skyColor(0xB7B5F5).fogColor(0xEEE6FA).waterColor(0x8FB3F2).waterFogColor(0x4A5FA8)
                .grassColorOverride(0xA9C98A).foliageColorOverride(0xA0C080),
                ModSounds.LAVENDER_FIELDS_LOOP, ModSounds.LAVENDER_FIELDS_ADDITIONS, 0.004);
    }

    private static Biome strawberryMeadow(HolderGetter<PlacedFeature> placed, HolderGetter<ConfiguredWorldCarver<?>> carvers) {
        BiomeGenerationSettings.Builder gen = baseGeneration(placed, carvers);
        ores(gen);
        vegetation(gen, ModPlacedFeatures.HUGE_FAIRY_MUSHROOM, ModPlacedFeatures.TREES_STRAWBERRY_MEADOW, ModPlacedFeatures.FAIRY_RING,
                ModPlacedFeatures.FLOWERS_STRAWBERRY_MEADOW, ModPlacedFeatures.PATCH_STRAWBERRY_BUSH, ModPlacedFeatures.PATCH_HYDRANGEA,
                ModPlacedFeatures.PATCH_FAIRY_MUSHROOM, ModPlacedFeatures.CARPET_MEADOW);
        gen.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.PATCH_TALL_GRASS_2);
        gen.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.PATCH_GRASS_PLAIN);
        vanillaFinish(gen);

        MobSpawnSettings.Builder spawns = baseSpawns();
        spawns.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.RABBIT, 6, 2, 4));
        spawns.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.FOX, 2, 1, 2));
        butterflies(spawns, 14);

        return biome(0.75F, 0.7F, spawns, gen, new BiomeSpecialEffects.Builder()
                .skyColor(0xA6D8FF).fogColor(0xFFF3E6).waterColor(0x86E6CF).waterFogColor(0x3E9C87)
                .grassColorOverride(0x92D873).foliageColorOverride(0x7FCF6A), null, ModSounds.STRAWBERRY_MEADOW_ADDITIONS, 0.008);
    }

    private static Biome moonlightForest(HolderGetter<PlacedFeature> placed, HolderGetter<ConfiguredWorldCarver<?>> carvers) {
        BiomeGenerationSettings.Builder gen = baseGeneration(placed, carvers);
        ores(gen);
        vegetation(gen, ModPlacedFeatures.HUGE_GLOWCAP, ModPlacedFeatures.TREES_MOONLIGHT_FOREST, ModPlacedFeatures.GLOWCAP_RING,
                ModPlacedFeatures.FLOWERS_MOONLIGHT_FOREST, ModPlacedFeatures.PATCH_CRYSTAL_FLOWER_RARE, ModPlacedFeatures.PATCH_GLOWCAP,
                ModPlacedFeatures.PATCH_MOONLIT_GRASS, ModPlacedFeatures.CARPET_MOONLIGHT);
        gen.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.PATCH_GRASS_FOREST);
        vanillaFinish(gen);

        MobSpawnSettings.Builder spawns = baseSpawns();
        spawns.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.FOX, 6, 2, 4));
        spawns.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.RABBIT, 4, 2, 3));
        spawns.addSpawn(MobCategory.AMBIENT, new MobSpawnSettings.SpawnerData(ModEntities.MOON_WISP.get(), 10, 1, 2));
        butterflies(spawns, 4);

        return biome(0.6F, 0.8F, spawns, gen, new BiomeSpecialEffects.Builder()
                .skyColor(0x7D8FE8).fogColor(0x9AA6E0).waterColor(0x5370D8).waterFogColor(0x24306E)
                .grassColorOverride(0x7486D6).foliageColorOverride(0x8A74D6),
                ModSounds.MOONLIGHT_FOREST_LOOP, ModSounds.MOONLIGHT_FOREST_ADDITIONS, 0.006);
    }

    private static Biome fairyGlade(HolderGetter<PlacedFeature> placed, HolderGetter<ConfiguredWorldCarver<?>> carvers) {
        BiomeGenerationSettings.Builder gen = baseGeneration(placed, carvers);
        gen.addFeature(GenerationStep.Decoration.LAKES, ModPlacedFeatures.ENCHANTED_POND);
        ores(gen);
        vegetation(gen, ModPlacedFeatures.CRYSTAL_FORMATION, ModPlacedFeatures.GIANT_FLOWER, ModPlacedFeatures.HUGE_FAIRY_MUSHROOM,
                ModPlacedFeatures.HUGE_GLOWCAP, ModPlacedFeatures.TREES_FAIRY_GLADE, ModPlacedFeatures.FAIRY_RING,
                ModPlacedFeatures.FLOWERS_FAIRY_GLADE, ModPlacedFeatures.PATCH_FAIRY_FOXGLOVE, ModPlacedFeatures.PATCH_CRYSTAL_FLOWER,
                ModPlacedFeatures.PATCH_FAIRY_MUSHROOM, ModPlacedFeatures.PATCH_GLOWCAP, ModPlacedFeatures.CARPET_GLADE);
        gen.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.PATCH_GRASS_PLAIN);
        vanillaFinish(gen);

        MobSpawnSettings.Builder spawns = baseSpawns();
        spawns.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.RABBIT, 6, 2, 4));
        spawns.addSpawn(MobCategory.AMBIENT, new MobSpawnSettings.SpawnerData(ModEntities.MOON_WISP.get(), 8, 1, 2));
        butterflies(spawns, 16);

        return biome(0.7F, 0.9F, spawns, gen, new BiomeSpecialEffects.Builder()
                .skyColor(0xC8B9FF).fogColor(0xF3EAFF).waterColor(0xB38CF5).waterFogColor(0x5B3E9C)
                .grassColorOverride(0x8EE0B8).foliageColorOverride(0x9FE3C4),
                ModSounds.FAIRY_GLADE_LOOP, ModSounds.FAIRY_GLADE_ADDITIONS, 0.006);
    }

    // ---------------------------------------------------------------------------------------------- shared

    /** Everything vanilla's private {@code globalOverworldGeneration} adds, in the same order. */
    private static BiomeGenerationSettings.Builder baseGeneration(HolderGetter<PlacedFeature> placed,
                                                                  HolderGetter<ConfiguredWorldCarver<?>> carvers) {
        BiomeGenerationSettings.Builder gen = new BiomeGenerationSettings.Builder(placed, carvers);
        BiomeDefaultFeatures.addDefaultCarversAndLakes(gen);
        BiomeDefaultFeatures.addDefaultCrystalFormations(gen);
        BiomeDefaultFeatures.addDefaultMonsterRoom(gen);
        BiomeDefaultFeatures.addDefaultUndergroundVariety(gen);
        BiomeDefaultFeatures.addDefaultSprings(gen);
        BiomeDefaultFeatures.addSurfaceFreezing(gen);
        return gen;
    }

    private static void ores(BiomeGenerationSettings.Builder gen) {
        BiomeDefaultFeatures.addDefaultOres(gen);
        BiomeDefaultFeatures.addDefaultSoftDisks(gen);
    }

    /**
     * Adds the given Fairy Garden vegetation in the global order defined by {@link #VEGETATION_ORDER}.
     * Every biome also gets the rare buried fairy cache.
     */
    @SafeVarargs
    private static void vegetation(BiomeGenerationSettings.Builder gen, ResourceKey<PlacedFeature>... features) {
        List<ResourceKey<PlacedFeature>> wanted = new java.util.ArrayList<>(List.of(features));
        wanted.add(ModPlacedFeatures.FAIRY_CACHE);
        for (ResourceKey<PlacedFeature> feature : VEGETATION_ORDER) {
            if (wanted.contains(feature)) {
                gen.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, feature);
            }
        }
        for (ResourceKey<PlacedFeature> feature : wanted) {
            if (!VEGETATION_ORDER.contains(feature)) {
                throw new IllegalStateException("Vegetation feature missing from the global order: " + feature.location());
            }
        }
    }

    private static final List<ResourceKey<PlacedFeature>> VEGETATION_ORDER = List.of(
            ModPlacedFeatures.FAIRY_CACHE,
            ModPlacedFeatures.CRYSTAL_FORMATION,
            ModPlacedFeatures.GIANT_FLOWER,
            ModPlacedFeatures.HUGE_FAIRY_MUSHROOM,
            ModPlacedFeatures.HUGE_GLOWCAP,
            ModPlacedFeatures.LAVENDER_FIELD,
            ModPlacedFeatures.TREES_BLOSSOM_VALLEY,
            ModPlacedFeatures.TREES_LAVENDER_FIELDS,
            ModPlacedFeatures.TREES_STRAWBERRY_MEADOW,
            ModPlacedFeatures.TREES_MOONLIGHT_FOREST,
            ModPlacedFeatures.TREES_FAIRY_GLADE,
            ModPlacedFeatures.FAIRY_RING,
            ModPlacedFeatures.GLOWCAP_RING,
            // Flower patches come before the petal patches: grass bone meal grows a biome's first flower feature.
            ModPlacedFeatures.FLOWERS_BLOSSOM_VALLEY,
            ModPlacedFeatures.FLOWERS_LAVENDER_FIELDS,
            ModPlacedFeatures.FLOWERS_STRAWBERRY_MEADOW,
            ModPlacedFeatures.FLOWERS_MOONLIGHT_FOREST,
            ModPlacedFeatures.FLOWERS_FAIRY_GLADE,
            ModPlacedFeatures.PATCH_BLOSSOM_PETALS,
            ModPlacedFeatures.PATCH_SNOW_BLOSSOM_PETALS,
            ModPlacedFeatures.PATCH_LAVENDER,
            ModPlacedFeatures.PATCH_FAIRY_FOXGLOVE,
            ModPlacedFeatures.PATCH_STRAWBERRY_BUSH,
            ModPlacedFeatures.PATCH_HYDRANGEA,
            ModPlacedFeatures.PATCH_CRYSTAL_FLOWER,
            ModPlacedFeatures.PATCH_CRYSTAL_FLOWER_RARE,
            ModPlacedFeatures.PATCH_FAIRY_MUSHROOM,
            ModPlacedFeatures.PATCH_GLOWCAP,
            ModPlacedFeatures.PATCH_BLUSH_GRASS,
            ModPlacedFeatures.PATCH_MOONLIT_GRASS,
            ModPlacedFeatures.CARPET_MEADOW,
            ModPlacedFeatures.CARPET_LAVENDER,
            ModPlacedFeatures.CARPET_MOONLIGHT,
            ModPlacedFeatures.CARPET_GLADE);

    /** Vanilla's closing vegetation, in vanilla order. */
    private static void vanillaFinish(BiomeGenerationSettings.Builder gen) {
        BiomeDefaultFeatures.addDefaultMushrooms(gen);
        BiomeDefaultFeatures.addDefaultExtraVegetation(gen);
    }

    private static MobSpawnSettings.Builder baseSpawns() {
        MobSpawnSettings.Builder spawns = new MobSpawnSettings.Builder();
        BiomeDefaultFeatures.farmAnimals(spawns);
        BiomeDefaultFeatures.commonSpawns(spawns);
        return spawns;
    }

    private static void butterflies(MobSpawnSettings.Builder spawns, int weight) {
        spawns.addSpawn(MobCategory.AMBIENT, new MobSpawnSettings.SpawnerData(ModEntities.BUTTERFLY.get(), weight, 2, 4));
    }

    private static Biome biome(float temperature, float downfall, MobSpawnSettings.Builder spawns, BiomeGenerationSettings.Builder gen,
                               BiomeSpecialEffects.Builder effects, @Nullable RegistryEntry<SoundEvent> loop,
                               RegistryEntry<SoundEvent> additions, double additionsChance) {
        effects.ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS)
                .ambientAdditionsSound(new AmbientAdditionsSettings(holder(additions), additionsChance))
                .backgroundMusic(Musics.createGameMusic(holder(ModSounds.MUSIC_FAIRY_GARDEN)));
        if (loop != null) {
            effects.ambientLoopSound(holder(loop));
        }
        return new Biome.BiomeBuilder()
                .hasPrecipitation(true)
                .temperature(temperature)
                .downfall(downfall)
                .specialEffects(effects.build())
                .mobSpawnSettings(spawns.build())
                .generationSettings(gen.build())
                .build();
    }

    private static Holder<SoundEvent> holder(RegistryEntry<SoundEvent> sound) {
        return BuiltInRegistries.SOUND_EVENT.wrapAsHolder(sound.get());
    }

    private static ResourceKey<Biome> key(String name) {
        return ResourceKey.create(Registries.BIOME, FairyGarden.id(name));
    }

    private ModBiomes() {
    }
}
