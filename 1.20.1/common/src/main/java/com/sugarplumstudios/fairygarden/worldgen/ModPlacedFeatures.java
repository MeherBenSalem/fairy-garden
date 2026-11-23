package com.sugarplumstudios.fairygarden.worldgen;

import com.sugarplumstudios.fairygarden.FairyGarden;
import com.sugarplumstudios.fairygarden.registry.ModBlocks;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.data.worldgen.placement.VegetationPlacements;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.placement.BiomeFilter;
import net.minecraft.world.level.levelgen.placement.CountPlacement;
import net.minecraft.world.level.levelgen.placement.InSquarePlacement;
import net.minecraft.world.level.levelgen.placement.NoiseThresholdCountPlacement;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.world.level.levelgen.placement.RarityFilter;
import net.minecraft.world.level.levelgen.placement.SurfaceWaterDepthFilter;

import java.util.ArrayList;
import java.util.List;

public final class ModPlacedFeatures {
    // Sapling-checked trees, used inside the per-biome tree mixes
    public static final ResourceKey<PlacedFeature> BLOSSOM_CHECKED = key("blossom_checked");
    public static final ResourceKey<PlacedFeature> BLOSSOM_BEES_CHECKED = key("blossom_bees_checked");
    public static final ResourceKey<PlacedFeature> SNOW_BLOSSOM_CHECKED = key("snow_blossom_checked");
    public static final ResourceKey<PlacedFeature> SMALL_BLOSSOM_CHECKED = key("small_blossom_checked");
    public static final ResourceKey<PlacedFeature> MOONWOOD_CHECKED = key("moonwood_checked");
    public static final ResourceKey<PlacedFeature> TALL_MOONWOOD_CHECKED = key("tall_moonwood_checked");

    // Lakes step
    public static final ResourceKey<PlacedFeature> ENCHANTED_POND = key("enchanted_pond");
    public static final ResourceKey<PlacedFeature> STREAM = key("stream");

    // Vegetal decoration step, listed in the one global order every biome follows
    public static final ResourceKey<PlacedFeature> FAIRY_CACHE = key("fairy_cache");
    public static final ResourceKey<PlacedFeature> CRYSTAL_FORMATION = key("crystal_formation");
    public static final ResourceKey<PlacedFeature> GIANT_FLOWER = key("giant_flower");
    public static final ResourceKey<PlacedFeature> HUGE_FAIRY_MUSHROOM = key("huge_fairy_mushroom");
    public static final ResourceKey<PlacedFeature> HUGE_GLOWCAP = key("huge_glowcap");
    public static final ResourceKey<PlacedFeature> LAVENDER_FIELD = key("lavender_field");
    public static final ResourceKey<PlacedFeature> TREES_BLOSSOM_VALLEY = key("trees_blossom_valley");
    public static final ResourceKey<PlacedFeature> TREES_LAVENDER_FIELDS = key("trees_lavender_fields");
    public static final ResourceKey<PlacedFeature> TREES_STRAWBERRY_MEADOW = key("trees_strawberry_meadow");
    public static final ResourceKey<PlacedFeature> TREES_MOONLIGHT_FOREST = key("trees_moonlight_forest");
    public static final ResourceKey<PlacedFeature> TREES_FAIRY_GLADE = key("trees_fairy_glade");
    public static final ResourceKey<PlacedFeature> FAIRY_RING = key("fairy_ring");
    public static final ResourceKey<PlacedFeature> GLOWCAP_RING = key("glowcap_ring");
    public static final ResourceKey<PlacedFeature> PATCH_BLOSSOM_PETALS = key("patch_blossom_petals");
    public static final ResourceKey<PlacedFeature> PATCH_SNOW_BLOSSOM_PETALS = key("patch_snow_blossom_petals");
    public static final ResourceKey<PlacedFeature> FLOWERS_BLOSSOM_VALLEY = key("flowers_blossom_valley");
    public static final ResourceKey<PlacedFeature> FLOWERS_LAVENDER_FIELDS = key("flowers_lavender_fields");
    public static final ResourceKey<PlacedFeature> FLOWERS_STRAWBERRY_MEADOW = key("flowers_strawberry_meadow");
    public static final ResourceKey<PlacedFeature> FLOWERS_MOONLIGHT_FOREST = key("flowers_moonlight_forest");
    public static final ResourceKey<PlacedFeature> FLOWERS_FAIRY_GLADE = key("flowers_fairy_glade");
    public static final ResourceKey<PlacedFeature> PATCH_LAVENDER = key("patch_lavender");
    public static final ResourceKey<PlacedFeature> PATCH_FAIRY_FOXGLOVE = key("patch_fairy_foxglove");
    public static final ResourceKey<PlacedFeature> PATCH_STRAWBERRY_BUSH = key("patch_strawberry_bush");
    public static final ResourceKey<PlacedFeature> PATCH_HYDRANGEA = key("patch_hydrangea");
    public static final ResourceKey<PlacedFeature> PATCH_CRYSTAL_FLOWER = key("patch_crystal_flower");
    public static final ResourceKey<PlacedFeature> PATCH_CRYSTAL_FLOWER_RARE = key("patch_crystal_flower_rare");
    public static final ResourceKey<PlacedFeature> PATCH_FAIRY_MUSHROOM = key("patch_fairy_mushroom");
    public static final ResourceKey<PlacedFeature> PATCH_GLOWCAP = key("patch_glowcap");
    public static final ResourceKey<PlacedFeature> PATCH_BLUSH_GRASS = key("patch_blush_grass");
    public static final ResourceKey<PlacedFeature> PATCH_MOONLIT_GRASS = key("patch_moonlit_grass");
    public static final ResourceKey<PlacedFeature> CARPET_MEADOW = key("flower_carpet_meadow");
    public static final ResourceKey<PlacedFeature> CARPET_LAVENDER = key("flower_carpet_lavender");
    public static final ResourceKey<PlacedFeature> CARPET_MOONLIGHT = key("flower_carpet_moonlight");
    public static final ResourceKey<PlacedFeature> CARPET_GLADE = key("flower_carpet_glade");

    public static void bootstrap(BootstapContext<PlacedFeature> context) {
        HolderGetter<ConfiguredFeature<?, ?>> configured = context.lookup(Registries.CONFIGURED_FEATURE);

        PlacementUtils.register(context, BLOSSOM_CHECKED, configured.getOrThrow(ModConfiguredFeatures.BLOSSOM_TREE),
                PlacementUtils.filteredByBlockSurvival(ModBlocks.BLOSSOM_SAPLING.get()));
        PlacementUtils.register(context, BLOSSOM_BEES_CHECKED, configured.getOrThrow(ModConfiguredFeatures.BLOSSOM_TREE_BEES),
                PlacementUtils.filteredByBlockSurvival(ModBlocks.BLOSSOM_SAPLING.get()));
        PlacementUtils.register(context, SNOW_BLOSSOM_CHECKED, configured.getOrThrow(ModConfiguredFeatures.SNOW_BLOSSOM_TREE),
                PlacementUtils.filteredByBlockSurvival(ModBlocks.SNOW_BLOSSOM_SAPLING.get()));
        PlacementUtils.register(context, SMALL_BLOSSOM_CHECKED, configured.getOrThrow(ModConfiguredFeatures.SMALL_BLOSSOM_TREE),
                PlacementUtils.filteredByBlockSurvival(ModBlocks.BLOSSOM_SAPLING.get()));
        PlacementUtils.register(context, MOONWOOD_CHECKED, configured.getOrThrow(ModConfiguredFeatures.MOONWOOD_TREE),
                PlacementUtils.filteredByBlockSurvival(ModBlocks.MOONWOOD_SAPLING.get()));
        PlacementUtils.register(context, TALL_MOONWOOD_CHECKED, configured.getOrThrow(ModConfiguredFeatures.TALL_MOONWOOD_TREE),
                PlacementUtils.filteredByBlockSurvival(ModBlocks.MOONWOOD_SAPLING.get()));

        PlacementUtils.register(context, ENCHANTED_POND, configured.getOrThrow(ModConfiguredFeatures.ENCHANTED_POND),
                dryLand(RarityFilter.onAverageOnceEvery(4)));
        PlacementUtils.register(context, STREAM, configured.getOrThrow(ModConfiguredFeatures.STREAM),
                dryLand(RarityFilter.onAverageOnceEvery(3)));

        PlacementUtils.register(context, FAIRY_CACHE, configured.getOrThrow(ModConfiguredFeatures.FAIRY_CACHE),
                dryLand(RarityFilter.onAverageOnceEvery(160)));
        PlacementUtils.register(context, CRYSTAL_FORMATION, configured.getOrThrow(ModConfiguredFeatures.CRYSTAL_FORMATION),
                surface(RarityFilter.onAverageOnceEvery(3)));
        PlacementUtils.register(context, GIANT_FLOWER, configured.getOrThrow(ModConfiguredFeatures.GIANT_FLOWER),
                surface(CountPlacement.of(UniformInt.of(1, 3))));
        PlacementUtils.register(context, HUGE_FAIRY_MUSHROOM, configured.getOrThrow(ModConfiguredFeatures.HUGE_FAIRY_MUSHROOM),
                surface(RarityFilter.onAverageOnceEvery(5)));
        PlacementUtils.register(context, HUGE_GLOWCAP, configured.getOrThrow(ModConfiguredFeatures.HUGE_GLOWCAP),
                surface(RarityFilter.onAverageOnceEvery(4)));
        // The lavender field covers the whole chunk and checks the biome column by column itself.
        PlacementUtils.register(context, LAVENDER_FIELD, configured.getOrThrow(ModConfiguredFeatures.LAVENDER_FIELD),
                PlacementUtils.HEIGHTMAP_WORLD_SURFACE);

        PlacementUtils.register(context, TREES_BLOSSOM_VALLEY, configured.getOrThrow(ModConfiguredFeatures.TREES_BLOSSOM_VALLEY),
                VegetationPlacements.treePlacement(PlacementUtils.countExtra(1, 0.5F, 2)));
        PlacementUtils.register(context, TREES_LAVENDER_FIELDS, configured.getOrThrow(ModConfiguredFeatures.TREES_LAVENDER_FIELDS),
                VegetationPlacements.treePlacement(RarityFilter.onAverageOnceEvery(6)));
        PlacementUtils.register(context, TREES_STRAWBERRY_MEADOW, configured.getOrThrow(ModConfiguredFeatures.TREES_STRAWBERRY_MEADOW),
                VegetationPlacements.treePlacement(PlacementUtils.countExtra(0, 0.5F, 1)));
        PlacementUtils.register(context, TREES_MOONLIGHT_FOREST, configured.getOrThrow(ModConfiguredFeatures.TREES_MOONLIGHT_FOREST),
                VegetationPlacements.treePlacement(PlacementUtils.countExtra(8, 0.1F, 1)));
        PlacementUtils.register(context, TREES_FAIRY_GLADE, configured.getOrThrow(ModConfiguredFeatures.TREES_FAIRY_GLADE),
                VegetationPlacements.treePlacement(PlacementUtils.countExtra(1, 0.25F, 1)));

        PlacementUtils.register(context, FAIRY_RING, configured.getOrThrow(ModConfiguredFeatures.FAIRY_RING),
                surface(RarityFilter.onAverageOnceEvery(6)));
        PlacementUtils.register(context, GLOWCAP_RING, configured.getOrThrow(ModConfiguredFeatures.GLOWCAP_RING),
                surface(RarityFilter.onAverageOnceEvery(7)));
        PlacementUtils.register(context, PATCH_BLOSSOM_PETALS, configured.getOrThrow(ModConfiguredFeatures.PATCH_BLOSSOM_PETALS),
                List.of(NoiseThresholdCountPlacement.of(-0.8, 2, 5), InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP, BiomeFilter.biome()));
        PlacementUtils.register(context, PATCH_SNOW_BLOSSOM_PETALS, configured.getOrThrow(ModConfiguredFeatures.PATCH_SNOW_BLOSSOM_PETALS),
                surface(RarityFilter.onAverageOnceEvery(2)));
        PlacementUtils.register(context, FLOWERS_BLOSSOM_VALLEY, configured.getOrThrow(ModConfiguredFeatures.FLOWERS_BLOSSOM_VALLEY),
                surface(CountPlacement.of(3)));
        PlacementUtils.register(context, FLOWERS_LAVENDER_FIELDS, configured.getOrThrow(ModConfiguredFeatures.FLOWERS_LAVENDER_FIELDS),
                surface(RarityFilter.onAverageOnceEvery(2)));
        PlacementUtils.register(context, FLOWERS_STRAWBERRY_MEADOW, configured.getOrThrow(ModConfiguredFeatures.FLOWERS_STRAWBERRY_MEADOW),
                surface(CountPlacement.of(4)));
        PlacementUtils.register(context, FLOWERS_MOONLIGHT_FOREST, configured.getOrThrow(ModConfiguredFeatures.FLOWERS_MOONLIGHT_FOREST),
                surface(CountPlacement.of(2)));
        PlacementUtils.register(context, FLOWERS_FAIRY_GLADE, configured.getOrThrow(ModConfiguredFeatures.FLOWERS_FAIRY_GLADE),
                surface(CountPlacement.of(4)));
        PlacementUtils.register(context, PATCH_LAVENDER, configured.getOrThrow(ModConfiguredFeatures.PATCH_LAVENDER),
                surface(CountPlacement.of(2)));
        PlacementUtils.register(context, PATCH_FAIRY_FOXGLOVE, configured.getOrThrow(ModConfiguredFeatures.PATCH_FAIRY_FOXGLOVE),
                surface(RarityFilter.onAverageOnceEvery(3)));
        PlacementUtils.register(context, PATCH_STRAWBERRY_BUSH, configured.getOrThrow(ModConfiguredFeatures.PATCH_STRAWBERRY_BUSH),
                List.of(CountPlacement.of(2), InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP_WORLD_SURFACE, BiomeFilter.biome()));
        PlacementUtils.register(context, PATCH_HYDRANGEA, configured.getOrThrow(ModConfiguredFeatures.PATCH_HYDRANGEA),
                surface(RarityFilter.onAverageOnceEvery(4)));
        PlacementUtils.register(context, PATCH_CRYSTAL_FLOWER, configured.getOrThrow(ModConfiguredFeatures.PATCH_CRYSTAL_FLOWER),
                surface(RarityFilter.onAverageOnceEvery(2)));
        PlacementUtils.register(context, PATCH_CRYSTAL_FLOWER_RARE, configured.getOrThrow(ModConfiguredFeatures.PATCH_CRYSTAL_FLOWER),
                surface(RarityFilter.onAverageOnceEvery(8)));
        PlacementUtils.register(context, PATCH_FAIRY_MUSHROOM, configured.getOrThrow(ModConfiguredFeatures.PATCH_FAIRY_MUSHROOM),
                surface(RarityFilter.onAverageOnceEvery(2)));
        PlacementUtils.register(context, PATCH_GLOWCAP, configured.getOrThrow(ModConfiguredFeatures.PATCH_GLOWCAP),
                surface(RarityFilter.onAverageOnceEvery(2)));
        PlacementUtils.register(context, PATCH_BLUSH_GRASS, configured.getOrThrow(ModConfiguredFeatures.PATCH_BLUSH_GRASS),
                surface(CountPlacement.of(4)));
        PlacementUtils.register(context, PATCH_MOONLIT_GRASS, configured.getOrThrow(ModConfiguredFeatures.PATCH_MOONLIT_GRASS),
                surface(CountPlacement.of(5)));
        PlacementUtils.register(context, CARPET_MEADOW, configured.getOrThrow(ModConfiguredFeatures.CARPET_MEADOW),
                surface(RarityFilter.onAverageOnceEvery(2)));
        PlacementUtils.register(context, CARPET_LAVENDER, configured.getOrThrow(ModConfiguredFeatures.CARPET_LAVENDER),
                surface(RarityFilter.onAverageOnceEvery(3)));
        PlacementUtils.register(context, CARPET_MOONLIGHT, configured.getOrThrow(ModConfiguredFeatures.CARPET_MOONLIGHT),
                surface(RarityFilter.onAverageOnceEvery(3)));
        PlacementUtils.register(context, CARPET_GLADE, configured.getOrThrow(ModConfiguredFeatures.CARPET_GLADE),
                surface(RarityFilter.onAverageOnceEvery(2)));
    }

    /** Spread within the chunk, snap to the surface, keep to the biome. */
    private static List<PlacementModifier> surface(PlacementModifier frequency) {
        List<PlacementModifier> list = new ArrayList<>();
        list.add(frequency);
        list.add(InSquarePlacement.spread());
        list.add(PlacementUtils.HEIGHTMAP);
        list.add(BiomeFilter.biome());
        return list;
    }

    /** Like {@link #surface} but never in or under water. */
    private static List<PlacementModifier> dryLand(PlacementModifier frequency) {
        return List.of(frequency, InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP_WORLD_SURFACE,
                SurfaceWaterDepthFilter.forMaxDepth(0), BiomeFilter.biome());
    }

    private static ResourceKey<PlacedFeature> key(String name) {
        return ResourceKey.create(Registries.PLACED_FEATURE, FairyGarden.id(name));
    }

    private ModPlacedFeatures() {
    }
}
