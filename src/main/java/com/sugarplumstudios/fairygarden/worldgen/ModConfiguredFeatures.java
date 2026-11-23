package com.sugarplumstudios.fairygarden.worldgen;

import com.sugarplumstudios.fairygarden.FairyGarden;
import com.sugarplumstudios.fairygarden.block.StrawberryBushBlock;
import com.sugarplumstudios.fairygarden.registry.ModBlocks;
import com.sugarplumstudios.fairygarden.registry.ModFeatures;
import com.sugarplumstudios.fairygarden.worldgen.feature.GiantFlowerConfiguration;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.data.worldgen.features.FeatureUtils;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.random.SimpleWeightedRandomList;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.util.valueproviders.WeightedListInt;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HugeMushroomBlock;
import net.minecraft.world.level.block.PinkPetalsBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.WeightedPlacedFeature;
import net.minecraft.world.level.levelgen.feature.configurations.HugeMushroomFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.RandomFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.RandomPatchConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.SimpleBlockConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.featuresize.ThreeLayersFeatureSize;
import net.minecraft.world.level.levelgen.feature.featuresize.TwoLayersFeatureSize;
import net.minecraft.world.level.levelgen.feature.foliageplacers.BlobFoliagePlacer;
import net.minecraft.world.level.levelgen.feature.foliageplacers.CherryFoliagePlacer;
import net.minecraft.world.level.levelgen.feature.foliageplacers.DarkOakFoliagePlacer;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FancyFoliagePlacer;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.feature.stateproviders.WeightedStateProvider;
import net.minecraft.world.level.levelgen.feature.treedecorators.AttachedToLeavesDecorator;
import net.minecraft.world.level.levelgen.feature.treedecorators.BeehiveDecorator;
import net.minecraft.world.level.levelgen.feature.trunkplacers.CherryTrunkPlacer;
import net.minecraft.world.level.levelgen.feature.trunkplacers.DarkOakTrunkPlacer;
import net.minecraft.world.level.levelgen.feature.trunkplacers.FancyTrunkPlacer;
import net.minecraft.world.level.levelgen.feature.trunkplacers.StraightTrunkPlacer;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

import java.util.List;
import java.util.OptionalInt;

public final class ModConfiguredFeatures {
    // Trees
    public static final ResourceKey<ConfiguredFeature<?, ?>> BLOSSOM_TREE = key("blossom_tree");
    public static final ResourceKey<ConfiguredFeature<?, ?>> BLOSSOM_TREE_BEES = key("blossom_tree_bees");
    public static final ResourceKey<ConfiguredFeature<?, ?>> SNOW_BLOSSOM_TREE = key("snow_blossom_tree");
    public static final ResourceKey<ConfiguredFeature<?, ?>> SMALL_BLOSSOM_TREE = key("small_blossom_tree");
    public static final ResourceKey<ConfiguredFeature<?, ?>> MOONWOOD_TREE = key("moonwood_tree");
    public static final ResourceKey<ConfiguredFeature<?, ?>> TALL_MOONWOOD_TREE = key("tall_moonwood_tree");
    public static final ResourceKey<ConfiguredFeature<?, ?>> HUGE_FAIRY_MUSHROOM = key("huge_fairy_mushroom");
    public static final ResourceKey<ConfiguredFeature<?, ?>> HUGE_GLOWCAP = key("huge_glowcap");

    // Per-biome tree mixes
    public static final ResourceKey<ConfiguredFeature<?, ?>> TREES_BLOSSOM_VALLEY = key("trees_blossom_valley");
    public static final ResourceKey<ConfiguredFeature<?, ?>> TREES_LAVENDER_FIELDS = key("trees_lavender_fields");
    public static final ResourceKey<ConfiguredFeature<?, ?>> TREES_STRAWBERRY_MEADOW = key("trees_strawberry_meadow");
    public static final ResourceKey<ConfiguredFeature<?, ?>> TREES_MOONLIGHT_FOREST = key("trees_moonlight_forest");
    public static final ResourceKey<ConfiguredFeature<?, ?>> TREES_FAIRY_GLADE = key("trees_fairy_glade");

    // Flowers (Feature.FLOWER, so bone meal on grass in these biomes grows them too)
    public static final ResourceKey<ConfiguredFeature<?, ?>> FLOWERS_BLOSSOM_VALLEY = key("flowers_blossom_valley");
    public static final ResourceKey<ConfiguredFeature<?, ?>> FLOWERS_LAVENDER_FIELDS = key("flowers_lavender_fields");
    public static final ResourceKey<ConfiguredFeature<?, ?>> FLOWERS_STRAWBERRY_MEADOW = key("flowers_strawberry_meadow");
    public static final ResourceKey<ConfiguredFeature<?, ?>> FLOWERS_MOONLIGHT_FOREST = key("flowers_moonlight_forest");
    public static final ResourceKey<ConfiguredFeature<?, ?>> FLOWERS_FAIRY_GLADE = key("flowers_fairy_glade");
    public static final ResourceKey<ConfiguredFeature<?, ?>> PATCH_BLOSSOM_PETALS = key("patch_blossom_petals");
    public static final ResourceKey<ConfiguredFeature<?, ?>> PATCH_SNOW_BLOSSOM_PETALS = key("patch_snow_blossom_petals");

    // Plant patches
    public static final ResourceKey<ConfiguredFeature<?, ?>> PATCH_LAVENDER = key("patch_lavender");
    public static final ResourceKey<ConfiguredFeature<?, ?>> PATCH_FAIRY_FOXGLOVE = key("patch_fairy_foxglove");
    public static final ResourceKey<ConfiguredFeature<?, ?>> PATCH_STRAWBERRY_BUSH = key("patch_strawberry_bush");
    public static final ResourceKey<ConfiguredFeature<?, ?>> PATCH_HYDRANGEA = key("patch_hydrangea");
    public static final ResourceKey<ConfiguredFeature<?, ?>> PATCH_CRYSTAL_FLOWER = key("patch_crystal_flower");
    public static final ResourceKey<ConfiguredFeature<?, ?>> PATCH_FAIRY_MUSHROOM = key("patch_fairy_mushroom");
    public static final ResourceKey<ConfiguredFeature<?, ?>> PATCH_GLOWCAP = key("patch_glowcap");
    public static final ResourceKey<ConfiguredFeature<?, ?>> PATCH_BLUSH_GRASS = key("patch_blush_grass");
    public static final ResourceKey<ConfiguredFeature<?, ?>> PATCH_MOONLIT_GRASS = key("patch_moonlit_grass");
    public static final ResourceKey<ConfiguredFeature<?, ?>> CARPET_MEADOW = key("flower_carpet_meadow");
    public static final ResourceKey<ConfiguredFeature<?, ?>> CARPET_LAVENDER = key("flower_carpet_lavender");
    public static final ResourceKey<ConfiguredFeature<?, ?>> CARPET_MOONLIGHT = key("flower_carpet_moonlight");
    public static final ResourceKey<ConfiguredFeature<?, ?>> CARPET_GLADE = key("flower_carpet_glade");

    // Hand-written features
    public static final ResourceKey<ConfiguredFeature<?, ?>> GIANT_FLOWER = key("giant_flower");
    public static final ResourceKey<ConfiguredFeature<?, ?>> LAVENDER_FIELD = key("lavender_field");
    public static final ResourceKey<ConfiguredFeature<?, ?>> CRYSTAL_FORMATION = key("crystal_formation");
    public static final ResourceKey<ConfiguredFeature<?, ?>> ENCHANTED_POND = key("enchanted_pond");
    public static final ResourceKey<ConfiguredFeature<?, ?>> STREAM = key("stream");
    public static final ResourceKey<ConfiguredFeature<?, ?>> FAIRY_RING = key("fairy_ring");
    public static final ResourceKey<ConfiguredFeature<?, ?>> GLOWCAP_RING = key("glowcap_ring");
    public static final ResourceKey<ConfiguredFeature<?, ?>> FAIRY_CACHE = key("fairy_cache");

    public static void bootstrap(BootstapContext<ConfiguredFeature<?, ?>> context) {
        HolderGetter<PlacedFeature> placed = context.lookup(Registries.PLACED_FEATURE);

        // ---------------------------------------------------------------- trees
        FeatureUtils.register(context, BLOSSOM_TREE, Feature.TREE, cherryShape(ModBlocks.BLOSSOM.log.get(), ModBlocks.BLOSSOM_LEAVES.get()).build());
        FeatureUtils.register(context, BLOSSOM_TREE_BEES, Feature.TREE, cherryShape(ModBlocks.BLOSSOM.log.get(), ModBlocks.BLOSSOM_LEAVES.get())
                .decorators(List.of(new BeehiveDecorator(0.05F))).build());
        FeatureUtils.register(context, SNOW_BLOSSOM_TREE, Feature.TREE, cherryShape(ModBlocks.BLOSSOM.log.get(), ModBlocks.SNOW_BLOSSOM_LEAVES.get()).build());
        FeatureUtils.register(context, SMALL_BLOSSOM_TREE, Feature.TREE, new TreeConfiguration.TreeConfigurationBuilder(
                BlockStateProvider.simple(ModBlocks.BLOSSOM.log.get()), new StraightTrunkPlacer(4, 2, 0),
                BlockStateProvider.simple(ModBlocks.BLOSSOM_LEAVES.get()), new BlobFoliagePlacer(ConstantInt.of(2), ConstantInt.of(0), 3),
                new TwoLayersFeatureSize(1, 0, 1)).ignoreVines().build());
        FeatureUtils.register(context, MOONWOOD_TREE, Feature.TREE, new TreeConfiguration.TreeConfigurationBuilder(
                BlockStateProvider.simple(ModBlocks.MOONWOOD.log.get()), new FancyTrunkPlacer(5, 9, 0),
                BlockStateProvider.simple(ModBlocks.MOONWOOD_LEAVES.get()), new FancyFoliagePlacer(ConstantInt.of(2), ConstantInt.of(4), 4),
                new TwoLayersFeatureSize(0, 0, 0, OptionalInt.of(4))).ignoreVines().decorators(List.of(moonbells(0.12F))).build());
        FeatureUtils.register(context, TALL_MOONWOOD_TREE, Feature.TREE, new TreeConfiguration.TreeConfigurationBuilder(
                BlockStateProvider.simple(ModBlocks.MOONWOOD.log.get()), new DarkOakTrunkPlacer(7, 2, 1),
                BlockStateProvider.simple(ModBlocks.MOONWOOD_LEAVES.get()), new DarkOakFoliagePlacer(ConstantInt.of(0), ConstantInt.of(0)),
                new ThreeLayersFeatureSize(1, 1, 0, 1, 2, OptionalInt.empty())).ignoreVines().decorators(List.of(moonbells(0.18F))).build());

        FeatureUtils.register(context, HUGE_FAIRY_MUSHROOM, Feature.HUGE_RED_MUSHROOM, new HugeMushroomFeatureConfiguration(
                BlockStateProvider.simple(ModBlocks.FAIRY_MUSHROOM_BLOCK.get().defaultBlockState().setValue(HugeMushroomBlock.DOWN, false)),
                BlockStateProvider.simple(stem()), 2));
        FeatureUtils.register(context, HUGE_GLOWCAP, Feature.HUGE_BROWN_MUSHROOM, new HugeMushroomFeatureConfiguration(
                BlockStateProvider.simple(ModBlocks.GLOWCAP_BLOCK.get().defaultBlockState().setValue(HugeMushroomBlock.UP, true)
                        .setValue(HugeMushroomBlock.DOWN, false)),
                BlockStateProvider.simple(stem()), 3));

        FeatureUtils.register(context, TREES_BLOSSOM_VALLEY, Feature.RANDOM_SELECTOR, new RandomFeatureConfiguration(List.of(
                new WeightedPlacedFeature(placed.getOrThrow(ModPlacedFeatures.SNOW_BLOSSOM_CHECKED), 0.25F),
                new WeightedPlacedFeature(placed.getOrThrow(ModPlacedFeatures.BLOSSOM_BEES_CHECKED), 0.06F),
                new WeightedPlacedFeature(placed.getOrThrow(ModPlacedFeatures.SMALL_BLOSSOM_CHECKED), 0.12F)),
                placed.getOrThrow(ModPlacedFeatures.BLOSSOM_CHECKED)));
        FeatureUtils.register(context, TREES_LAVENDER_FIELDS, Feature.RANDOM_SELECTOR, new RandomFeatureConfiguration(List.of(
                new WeightedPlacedFeature(placed.getOrThrow(ModPlacedFeatures.SNOW_BLOSSOM_CHECKED), 0.5F)),
                placed.getOrThrow(ModPlacedFeatures.SMALL_BLOSSOM_CHECKED)));
        FeatureUtils.register(context, TREES_STRAWBERRY_MEADOW, Feature.RANDOM_SELECTOR, new RandomFeatureConfiguration(List.of(
                new WeightedPlacedFeature(placed.getOrThrow(ModPlacedFeatures.BLOSSOM_BEES_CHECKED), 0.25F),
                new WeightedPlacedFeature(placed.getOrThrow(ModPlacedFeatures.SNOW_BLOSSOM_CHECKED), 0.2F)),
                placed.getOrThrow(ModPlacedFeatures.SMALL_BLOSSOM_CHECKED)));
        FeatureUtils.register(context, TREES_MOONLIGHT_FOREST, Feature.RANDOM_SELECTOR, new RandomFeatureConfiguration(List.of(
                new WeightedPlacedFeature(placed.getOrThrow(ModPlacedFeatures.TALL_MOONWOOD_CHECKED), 0.3F)),
                placed.getOrThrow(ModPlacedFeatures.MOONWOOD_CHECKED)));
        FeatureUtils.register(context, TREES_FAIRY_GLADE, Feature.RANDOM_SELECTOR, new RandomFeatureConfiguration(List.of(
                new WeightedPlacedFeature(placed.getOrThrow(ModPlacedFeatures.SNOW_BLOSSOM_CHECKED), 0.3F),
                new WeightedPlacedFeature(placed.getOrThrow(ModPlacedFeatures.MOONWOOD_CHECKED), 0.25F)),
                placed.getOrThrow(ModPlacedFeatures.SMALL_BLOSSOM_CHECKED)));

        // ---------------------------------------------------------------- flowers
        FeatureUtils.register(context, FLOWERS_BLOSSOM_VALLEY, Feature.FLOWER, flowerPatch(64, 6, weighted()
                .add(state(ModBlocks.ROSY_DAISY.get()), 4).add(state(ModBlocks.STARFLOWER.get()), 2)
                .add(state(ModBlocks.BLUEBELL.get()), 2).add(state(ModBlocks.CREAM_BUTTERCUP.get()), 1)
                .add(state(ModBlocks.MINT_POSY.get()), 1)));
        FeatureUtils.register(context, FLOWERS_LAVENDER_FIELDS, Feature.FLOWER, flowerPatch(48, 6, weighted()
                .add(state(ModBlocks.LAVENDER.get()), 3).add(state(ModBlocks.STARFLOWER.get()), 3)
                .add(state(ModBlocks.CREAM_BUTTERCUP.get()), 2).add(state(ModBlocks.BLUEBELL.get()), 2)));
        FeatureUtils.register(context, FLOWERS_STRAWBERRY_MEADOW, Feature.FLOWER, flowerPatch(64, 6, weighted()
                .add(state(ModBlocks.CREAM_BUTTERCUP.get()), 4).add(state(ModBlocks.ROSY_DAISY.get()), 2)
                .add(state(ModBlocks.MINT_POSY.get()), 2).add(state(ModBlocks.STARFLOWER.get()), 2)));
        FeatureUtils.register(context, FLOWERS_MOONLIGHT_FOREST, Feature.FLOWER, flowerPatch(48, 6, weighted()
                .add(state(ModBlocks.MOONFLOWER.get()), 4).add(state(ModBlocks.BLUEBELL.get()), 3)
                .add(state(ModBlocks.STARFLOWER.get()), 1)));
        FeatureUtils.register(context, FLOWERS_FAIRY_GLADE, Feature.FLOWER, flowerPatch(64, 6, weighted()
                .add(state(ModBlocks.MINT_POSY.get()), 4).add(state(ModBlocks.CRYSTAL_FLOWER.get()), 2)
                .add(state(ModBlocks.ROSY_DAISY.get()), 2).add(state(ModBlocks.BLUEBELL.get()), 1)
                .add(state(ModBlocks.STARFLOWER.get()), 1)));
        FeatureUtils.register(context, PATCH_BLOSSOM_PETALS, Feature.FLOWER, flowerPatch(96, 6, petals(ModBlocks.BLOSSOM_PETALS.get())));
        FeatureUtils.register(context, PATCH_SNOW_BLOSSOM_PETALS, Feature.FLOWER, flowerPatch(64, 5, petals(ModBlocks.SNOW_BLOSSOM_PETALS.get())));

        // ---------------------------------------------------------------- patches
        FeatureUtils.register(context, PATCH_LAVENDER, Feature.RANDOM_PATCH, emptyPatch(48, 5, new WeightedStateProvider(weighted()
                .add(state(ModBlocks.LAVENDER.get()), 3).add(state(ModBlocks.TALL_LAVENDER.get()), 2))));
        FeatureUtils.register(context, PATCH_FAIRY_FOXGLOVE, Feature.RANDOM_PATCH, emptyPatch(24, 5,
                BlockStateProvider.simple(ModBlocks.FAIRY_FOXGLOVE.get())));
        FeatureUtils.register(context, PATCH_STRAWBERRY_BUSH, Feature.RANDOM_PATCH, grassPatch(40, 5, new WeightedStateProvider(weighted()
                .add(ModBlocks.STRAWBERRY_BUSH.get().defaultBlockState().setValue(StrawberryBushBlock.AGE, 1), 1)
                .add(ModBlocks.STRAWBERRY_BUSH.get().defaultBlockState().setValue(StrawberryBushBlock.AGE, 2), 2)
                .add(ModBlocks.STRAWBERRY_BUSH.get().defaultBlockState().setValue(StrawberryBushBlock.AGE, 3), 3))));
        FeatureUtils.register(context, PATCH_HYDRANGEA, Feature.RANDOM_PATCH, grassPatch(10, 4, new WeightedStateProvider(weighted()
                .add(state(ModBlocks.PINK_HYDRANGEA.get()), 2).add(state(ModBlocks.BLUE_HYDRANGEA.get()), 2)
                .add(state(ModBlocks.LAVENDER_HYDRANGEA.get()), 1))));
        FeatureUtils.register(context, PATCH_CRYSTAL_FLOWER, Feature.RANDOM_PATCH, emptyPatch(16, 4,
                BlockStateProvider.simple(ModBlocks.CRYSTAL_FLOWER.get())));
        FeatureUtils.register(context, PATCH_FAIRY_MUSHROOM, Feature.RANDOM_PATCH, emptyPatch(20, 5,
                BlockStateProvider.simple(ModBlocks.FAIRY_MUSHROOM.get())));
        FeatureUtils.register(context, PATCH_GLOWCAP, Feature.RANDOM_PATCH, emptyPatch(20, 5,
                BlockStateProvider.simple(ModBlocks.GLOWCAP.get())));
        FeatureUtils.register(context, PATCH_BLUSH_GRASS, Feature.RANDOM_PATCH, emptyPatch(32, 7,
                BlockStateProvider.simple(ModBlocks.BLUSH_GRASS.get())));
        FeatureUtils.register(context, PATCH_MOONLIT_GRASS, Feature.RANDOM_PATCH, emptyPatch(48, 7,
                BlockStateProvider.simple(ModBlocks.MOONLIT_GRASS.get())));
        FeatureUtils.register(context, CARPET_MEADOW, Feature.RANDOM_PATCH, grassPatch(24, 4, new WeightedStateProvider(weighted()
                .add(state(ModBlocks.CREAM_FLOWER_CARPET.get()), 3).add(state(ModBlocks.PINK_FLOWER_CARPET.get()), 2)
                .add(state(ModBlocks.MINT_FLOWER_CARPET.get()), 2))));
        FeatureUtils.register(context, CARPET_LAVENDER, Feature.RANDOM_PATCH, grassPatch(24, 4, new WeightedStateProvider(weighted()
                .add(state(ModBlocks.LAVENDER_FLOWER_CARPET.get()), 3).add(state(ModBlocks.BLUE_FLOWER_CARPET.get()), 1))));
        FeatureUtils.register(context, CARPET_MOONLIGHT, Feature.RANDOM_PATCH, grassPatch(24, 4, new WeightedStateProvider(weighted()
                .add(state(ModBlocks.BLUE_FLOWER_CARPET.get()), 3).add(state(ModBlocks.LAVENDER_FLOWER_CARPET.get()), 2))));
        FeatureUtils.register(context, CARPET_GLADE, Feature.RANDOM_PATCH, grassPatch(24, 4, new WeightedStateProvider(weighted()
                .add(state(ModBlocks.MINT_FLOWER_CARPET.get()), 3).add(state(ModBlocks.BLUE_FLOWER_CARPET.get()), 1)
                .add(state(ModBlocks.PINK_FLOWER_CARPET.get()), 1))));

        // ---------------------------------------------------------------- special features
        FeatureUtils.register(context, GIANT_FLOWER, ModFeatures.GIANT_FLOWER.get(), new GiantFlowerConfiguration(
                BlockStateProvider.simple(ModBlocks.GIANT_FLOWER_STEM.get()),
                new WeightedStateProvider(weighted().add(state(ModBlocks.PINK_GIANT_PETALS.get()), 3)
                        .add(state(ModBlocks.LAVENDER_GIANT_PETALS.get()), 3).add(state(ModBlocks.BLUE_GIANT_PETALS.get()), 2)
                        .add(state(ModBlocks.CREAM_GIANT_PETALS.get()), 2)),
                BlockStateProvider.simple(ModBlocks.GIANT_FLOWER_PISTIL.get()),
                UniformInt.of(4, 8)));
        FeatureUtils.register(context, LAVENDER_FIELD, ModFeatures.LAVENDER_FIELD.get(), NoneFeatureConfiguration.INSTANCE);
        FeatureUtils.register(context, CRYSTAL_FORMATION, ModFeatures.CRYSTAL_FORMATION.get(), NoneFeatureConfiguration.INSTANCE);
        FeatureUtils.register(context, ENCHANTED_POND, ModFeatures.ENCHANTED_POND.get(), NoneFeatureConfiguration.INSTANCE);
        FeatureUtils.register(context, STREAM, ModFeatures.STREAM.get(), NoneFeatureConfiguration.INSTANCE);
        FeatureUtils.register(context, FAIRY_RING, ModFeatures.FAIRY_RING.get(),
                new SimpleBlockConfiguration(BlockStateProvider.simple(ModBlocks.FAIRY_MUSHROOM.get())));
        FeatureUtils.register(context, GLOWCAP_RING, ModFeatures.FAIRY_RING.get(),
                new SimpleBlockConfiguration(BlockStateProvider.simple(ModBlocks.GLOWCAP.get())));
        FeatureUtils.register(context, FAIRY_CACHE, ModFeatures.FAIRY_CACHE.get(), NoneFeatureConfiguration.INSTANCE);
    }

    private static TreeConfiguration.TreeConfigurationBuilder cherryShape(Block log, Block leaves) {
        return new TreeConfiguration.TreeConfigurationBuilder(
                BlockStateProvider.simple(log),
                new CherryTrunkPlacer(7, 1, 0, new WeightedListInt(SimpleWeightedRandomList.<IntProvider>builder()
                        .add(ConstantInt.of(1), 1).add(ConstantInt.of(2), 1).add(ConstantInt.of(3), 1).build()),
                        UniformInt.of(2, 4), UniformInt.of(-4, -3), UniformInt.of(-1, 0)),
                BlockStateProvider.simple(leaves),
                new CherryFoliagePlacer(ConstantInt.of(4), ConstantInt.of(0), ConstantInt.of(5), 0.25F, 0.5F, 0.16666667F, 0.33333334F),
                new TwoLayersFeatureSize(1, 0, 2)).ignoreVines();
    }

    private static AttachedToLeavesDecorator moonbells(float probability) {
        return new AttachedToLeavesDecorator(probability, 1, 0, BlockStateProvider.simple(ModBlocks.MOONBELL.get()), 2, List.of(Direction.DOWN));
    }

    private static BlockState stem() {
        return Blocks.MUSHROOM_STEM.defaultBlockState().setValue(HugeMushroomBlock.UP, false).setValue(HugeMushroomBlock.DOWN, false);
    }

    private static BlockState state(Block block) {
        return block.defaultBlockState();
    }

    private static SimpleWeightedRandomList.Builder<BlockState> weighted() {
        return SimpleWeightedRandomList.builder();
    }

    private static WeightedStateProvider petals(Block block) {
        SimpleWeightedRandomList.Builder<BlockState> builder = SimpleWeightedRandomList.builder();
        for (int amount = 1; amount <= 4; amount++) {
            for (Direction direction : Direction.Plane.HORIZONTAL) {
                builder.add(block.defaultBlockState().setValue(PinkPetalsBlock.AMOUNT, amount).setValue(PinkPetalsBlock.FACING, direction), 1);
            }
        }
        return new WeightedStateProvider(builder);
    }

    private static RandomPatchConfiguration flowerPatch(int tries, int spread, SimpleWeightedRandomList.Builder<BlockState> flowers) {
        return flowerPatch(tries, spread, new WeightedStateProvider(flowers));
    }

    private static RandomPatchConfiguration flowerPatch(int tries, int spread, BlockStateProvider provider) {
        return new RandomPatchConfiguration(tries, spread, 2, PlacementUtils.onlyWhenEmpty(Feature.SIMPLE_BLOCK, new SimpleBlockConfiguration(provider)));
    }

    private static RandomPatchConfiguration emptyPatch(int tries, int spread, BlockStateProvider provider) {
        return new RandomPatchConfiguration(tries, spread, 2, PlacementUtils.onlyWhenEmpty(Feature.SIMPLE_BLOCK, new SimpleBlockConfiguration(provider)));
    }

    /** A patch that may only grow on top of grass blocks. */
    private static RandomPatchConfiguration grassPatch(int tries, int spread, BlockStateProvider provider) {
        return new RandomPatchConfiguration(tries, spread, 1, PlacementUtils.filtered(Feature.SIMPLE_BLOCK, new SimpleBlockConfiguration(provider),
                BlockPredicate.allOf(BlockPredicate.ONLY_IN_AIR_PREDICATE,
                        BlockPredicate.matchesBlocks(Direction.DOWN.getNormal(), Blocks.GRASS_BLOCK))));
    }

    private static ResourceKey<ConfiguredFeature<?, ?>> key(String name) {
        return ResourceKey.create(Registries.CONFIGURED_FEATURE, FairyGarden.id(name));
    }

    private ModConfiguredFeatures() {
    }
}
