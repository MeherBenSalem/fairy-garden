package com.sugarplumstudios.fairygarden.worldgen;

import com.mojang.datafixers.util.Pair;
import com.sugarplumstudios.fairygarden.FairyGarden;
import com.sugarplumstudios.fairygarden.registry.ModTags;
import com.sugarplumstudios.fairygarden.worldgen.structure.FlatJigsawStructure;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.data.worldgen.Pools;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.heightproviders.ConstantHeight;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.structure.TerrainAdjustment;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadType;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.structures.JigsawStructure;
import net.minecraft.world.level.levelgen.structure.templatesystem.AlwaysTrueTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.ProcessorRule;
import net.minecraft.world.level.levelgen.structure.templatesystem.RandomBlockMatchTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Fairy Garden's discoverable structures. Each is a single-piece jigsaw structure built from a template that
 * datagen writes from code ({@code StructureBlueprints}); all of them hold a rare treasure chest.
 */
public final class ModStructures {
    public static final ResourceKey<Structure> FLOWER_SHRINE = structure("flower_shrine");
    public static final ResourceKey<Structure> HIDDEN_FAIRY_GARDEN = structure("hidden_fairy_garden");
    public static final ResourceKey<Structure> MOONSTONE_CIRCLE = structure("moonstone_circle");
    public static final ResourceKey<Structure> MUSHROOM_COTTAGE = structure("mushroom_cottage");
    public static final List<ResourceKey<Structure>> ALL = List.of(FLOWER_SHRINE, HIDDEN_FAIRY_GARDEN, MOONSTONE_CIRCLE, MUSHROOM_COTTAGE);

    public static final ResourceKey<StructureSet> FLOWER_SHRINE_SET = set("flower_shrine");
    public static final ResourceKey<StructureSet> HIDDEN_FAIRY_GARDEN_SET = set("hidden_fairy_garden");
    public static final ResourceKey<StructureSet> MOONSTONE_CIRCLE_SET = set("moonstone_circle");
    public static final ResourceKey<StructureSet> MUSHROOM_COTTAGE_SET = set("mushroom_cottage");

    public static final ResourceKey<StructureTemplatePool> FLOWER_SHRINE_POOL = pool("flower_shrine");
    public static final ResourceKey<StructureTemplatePool> HIDDEN_FAIRY_GARDEN_POOL = pool("hidden_fairy_garden");
    public static final ResourceKey<StructureTemplatePool> MOONSTONE_CIRCLE_POOL = pool("moonstone_circle");
    public static final ResourceKey<StructureTemplatePool> MUSHROOM_COTTAGE_POOL = pool("mushroom_cottage");

    public static final ResourceKey<StructureProcessorList> WEATHERED_STONE = ResourceKey.create(Registries.PROCESSOR_LIST,
            FairyGarden.id("weathered_stone"));

    public static void bootstrapProcessors(BootstapContext<StructureProcessorList> context) {
        context.register(WEATHERED_STONE, new StructureProcessorList(List.of(new RuleProcessor(List.of(
                new ProcessorRule(new RandomBlockMatchTest(Blocks.STONE_BRICKS, 0.3F), AlwaysTrueTest.INSTANCE,
                        Blocks.MOSSY_STONE_BRICKS.defaultBlockState()),
                new ProcessorRule(new RandomBlockMatchTest(Blocks.STONE_BRICKS, 0.1F), AlwaysTrueTest.INSTANCE,
                        Blocks.CRACKED_STONE_BRICKS.defaultBlockState()),
                new ProcessorRule(new RandomBlockMatchTest(Blocks.COBBLESTONE, 0.4F), AlwaysTrueTest.INSTANCE,
                        Blocks.MOSSY_COBBLESTONE.defaultBlockState()))))));
    }

    public static void bootstrapPools(BootstapContext<StructureTemplatePool> context) {
        HolderGetter<StructureTemplatePool> pools = context.lookup(Registries.TEMPLATE_POOL);
        HolderGetter<StructureProcessorList> processors = context.lookup(Registries.PROCESSOR_LIST);
        Holder<StructureTemplatePool> empty = pools.getOrThrow(Pools.EMPTY);
        Holder<StructureProcessorList> weathered = processors.getOrThrow(WEATHERED_STONE);

        context.register(FLOWER_SHRINE_POOL, new StructureTemplatePool(empty, List.of(
                Pair.of(StructurePoolElement.single(FairyGarden.MOD_ID + ":flower_shrine_blossom", weathered), 1),
                Pair.of(StructurePoolElement.single(FairyGarden.MOD_ID + ":flower_shrine_lavender", weathered), 1)),
                StructureTemplatePool.Projection.RIGID));
        context.register(HIDDEN_FAIRY_GARDEN_POOL, new StructureTemplatePool(empty, List.of(
                Pair.of(StructurePoolElement.single(FairyGarden.MOD_ID + ":hidden_fairy_garden"), 1)),
                StructureTemplatePool.Projection.RIGID));
        context.register(MOONSTONE_CIRCLE_POOL, new StructureTemplatePool(empty, List.of(
                Pair.of(StructurePoolElement.single(FairyGarden.MOD_ID + ":moonstone_circle", weathered), 1)),
                StructureTemplatePool.Projection.RIGID));
        context.register(MUSHROOM_COTTAGE_POOL, new StructureTemplatePool(empty, List.of(
                Pair.of(StructurePoolElement.single(FairyGarden.MOD_ID + ":mushroom_cottage"), 1)),
                StructureTemplatePool.Projection.RIGID));
    }

    public static void bootstrapStructures(BootstapContext<Structure> context) {
        HolderGetter<Biome> biomes = context.lookup(Registries.BIOME);
        HolderGetter<StructureTemplatePool> pools = context.lookup(Registries.TEMPLATE_POOL);
        // The last argument is how uneven the ground under the footprint may be (9x9 buildings: 3, larger ones: 4).
        context.register(FLOWER_SHRINE, jigsaw(biomes, ModTags.Biomes.HAS_FLOWER_SHRINE, pools.getOrThrow(FLOWER_SHRINE_POOL), 0, 3));
        context.register(HIDDEN_FAIRY_GARDEN, jigsaw(biomes, ModTags.Biomes.HAS_HIDDEN_FAIRY_GARDEN, pools.getOrThrow(HIDDEN_FAIRY_GARDEN_POOL), 0, 4));
        // The moonstone circle's bottom layer is underground: its treasure chest is buried beneath the altar.
        context.register(MOONSTONE_CIRCLE, jigsaw(biomes, ModTags.Biomes.HAS_MOONSTONE_CIRCLE, pools.getOrThrow(MOONSTONE_CIRCLE_POOL), -1, 4));
        context.register(MUSHROOM_COTTAGE, jigsaw(biomes, ModTags.Biomes.HAS_MUSHROOM_COTTAGE, pools.getOrThrow(MUSHROOM_COTTAGE_POOL), 0, 3));
    }

    /**
     * Spacing / separation in chunks. {@link FlatJigsawStructure} only accepts 33-47 % of the candidates that land
     * in a fitting biome (the rest are steep or flooded). Each spacing is therefore the original one (shrine 22,
     * hedge garden 28, moonstone circle 24, cottage 26) times the square root of that acceptance rate, which keeps
     * every structure about as common as it was before the ground check. {@code runWorldgenTest} reports the counts.
     */
    public static void bootstrapSets(BootstapContext<StructureSet> context) {
        HolderGetter<Structure> structures = context.lookup(Registries.STRUCTURE);
        context.register(FLOWER_SHRINE_SET, new StructureSet(structures.getOrThrow(FLOWER_SHRINE),
                new RandomSpreadStructurePlacement(15, 5, RandomSpreadType.LINEAR, 61834021)));
        context.register(HIDDEN_FAIRY_GARDEN_SET, new StructureSet(structures.getOrThrow(HIDDEN_FAIRY_GARDEN),
                new RandomSpreadStructurePlacement(17, 6, RandomSpreadType.LINEAR, 71249953)));
        context.register(MOONSTONE_CIRCLE_SET, new StructureSet(structures.getOrThrow(MOONSTONE_CIRCLE),
                new RandomSpreadStructurePlacement(14, 5, RandomSpreadType.LINEAR, 52813317)));
        context.register(MUSHROOM_COTTAGE_SET, new StructureSet(structures.getOrThrow(MUSHROOM_COTTAGE),
                new RandomSpreadStructurePlacement(17, 6, RandomSpreadType.LINEAR, 84720195)));
    }

    /** A single-piece jigsaw structure placed on the surface, only where the ground is dry and level enough. */
    private static Structure jigsaw(HolderGetter<Biome> biomes, TagKey<Biome> biomeTag, Holder<StructureTemplatePool> start, int yOffset,
                                    int maxHeightDifference) {
        Structure.StructureSettings settings = new Structure.StructureSettings(biomes.getOrThrow(biomeTag), Map.of(),
                GenerationStep.Decoration.SURFACE_STRUCTURES, TerrainAdjustment.BEARD_THIN);
        JigsawStructure layout = new JigsawStructure(settings, start, Optional.empty(), 1, ConstantHeight.of(VerticalAnchor.absolute(yOffset)),
                false, Optional.of(Heightmap.Types.WORLD_SURFACE_WG), 32);
        return new FlatJigsawStructure(settings, layout, maxHeightDifference);
    }

    private static ResourceKey<Structure> structure(String name) {
        return ResourceKey.create(Registries.STRUCTURE, FairyGarden.id(name));
    }

    private static ResourceKey<StructureSet> set(String name) {
        return ResourceKey.create(Registries.STRUCTURE_SET, FairyGarden.id(name));
    }

    private static ResourceKey<StructureTemplatePool> pool(String name) {
        return ResourceKey.create(Registries.TEMPLATE_POOL, FairyGarden.id(name));
    }

    private ModStructures() {
    }
}
