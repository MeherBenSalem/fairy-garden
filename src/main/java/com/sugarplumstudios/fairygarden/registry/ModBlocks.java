package com.sugarplumstudios.fairygarden.registry;

import com.sugarplumstudios.fairygarden.FairyGarden;
import com.sugarplumstudios.fairygarden.block.AmbientParticleBlock;
import com.sugarplumstudios.fairygarden.block.FairyCrystalBlock;
import com.sugarplumstudios.fairygarden.block.FairyCrystalClusterBlock;
import com.sugarplumstudios.fairygarden.block.FairyFlowerBlock;
import com.sugarplumstudios.fairygarden.block.FairyLanternBlock;
import com.sugarplumstudios.fairygarden.block.FairyLeavesBlock;
import com.sugarplumstudios.fairygarden.block.FairyMushroomBlock;
import com.sugarplumstudios.fairygarden.block.FloweringBushBlock;
import com.sugarplumstudios.fairygarden.block.GiantPetalBlock;
import com.sugarplumstudios.fairygarden.block.GlimmerLilyBlock;
import com.sugarplumstudios.fairygarden.block.MoonbellBlock;
import com.sugarplumstudios.fairygarden.block.MoonflowerBlock;
import com.sugarplumstudios.fairygarden.block.ShortPlantBlock;
import com.sugarplumstudios.fairygarden.block.StarbloomCropBlock;
import com.sugarplumstudios.fairygarden.block.StrawberryBushBlock;
import com.sugarplumstudios.fairygarden.block.grower.FairyTreeGrower;
import com.sugarplumstudios.fairygarden.entity.BoatWood;
import com.sugarplumstudios.fairygarden.worldgen.ModConfiguredFeatures;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CarpetBlock;
import net.minecraft.world.level.block.FlowerPotBlock;
import net.minecraft.world.level.block.HayBlock;
import net.minecraft.world.level.block.HugeMushroomBlock;
import net.minecraft.world.level.block.PinkPetalsBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.TallFlowerBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.item.DoubleHighBlockItem;
import net.minecraft.world.item.PlaceOnWaterBlockItem;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

public final class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, FairyGarden.MOD_ID);

    /** Plant block to its potted form, in registration order. */
    public static final Map<RegistryObject<? extends Block>, RegistryObject<FlowerPotBlock>> POTTED = new LinkedHashMap<>();
    public static final List<RegistryObject<? extends Block>> SMALL_FLOWERS = new ArrayList<>();
    public static final List<RegistryObject<? extends Block>> TALL_FLOWERS = new ArrayList<>();

    // ------------------------------------------------------------------ woods

    public static final WoodSet BLOSSOM = new WoodSet("blossom", ModWoodTypes.BLOSSOM, BoatWood.BLOSSOM,
            MapColor.COLOR_PINK, MapColor.TERRACOTTA_BROWN, SoundType.CHERRY_WOOD, SoundType.CHERRY_WOOD_HANGING_SIGN);
    public static final WoodSet MOONWOOD = new WoodSet("moonwood", ModWoodTypes.MOONWOOD, BoatWood.MOONWOOD,
            MapColor.COLOR_LIGHT_BLUE, MapColor.COLOR_BLUE, SoundType.WOOD, SoundType.HANGING_SIGN);
    public static final List<WoodSet> WOOD_SETS = List.of(BLOSSOM, MOONWOOD);

    public static final RegistryObject<FairyLeavesBlock> BLOSSOM_LEAVES = register("blossom_leaves",
            () -> new FairyLeavesBlock(ModParticles.BLOSSOM_PETAL, 10, false, leaves(MapColor.COLOR_PINK, SoundType.CHERRY_LEAVES)));
    public static final RegistryObject<FairyLeavesBlock> SNOW_BLOSSOM_LEAVES = register("snow_blossom_leaves",
            () -> new FairyLeavesBlock(ModParticles.SNOW_PETAL, 10, false, leaves(MapColor.SNOW, SoundType.CHERRY_LEAVES)));
    public static final RegistryObject<FairyLeavesBlock> MOONWOOD_LEAVES = register("moonwood_leaves",
            () -> new FairyLeavesBlock(ModParticles.MOON_GLIMMER, 45, true, leaves(MapColor.COLOR_PURPLE, SoundType.AZALEA_LEAVES)));

    public static final RegistryObject<SaplingBlock> BLOSSOM_SAPLING = register("blossom_sapling",
            () -> new SaplingBlock(new FairyTreeGrower(ModConfiguredFeatures.BLOSSOM_TREE, ModConfiguredFeatures.BLOSSOM_TREE_BEES),
                    sapling(MapColor.COLOR_PINK, SoundType.CHERRY_SAPLING)));
    public static final RegistryObject<SaplingBlock> SNOW_BLOSSOM_SAPLING = register("snow_blossom_sapling",
            () -> new SaplingBlock(new FairyTreeGrower(ModConfiguredFeatures.SNOW_BLOSSOM_TREE, null),
                    sapling(MapColor.SNOW, SoundType.CHERRY_SAPLING)));
    public static final RegistryObject<SaplingBlock> MOONWOOD_SAPLING = register("moonwood_sapling",
            () -> new SaplingBlock(new FairyTreeGrower(ModConfiguredFeatures.MOONWOOD_TREE, null),
                    sapling(MapColor.COLOR_PURPLE, SoundType.GRASS).lightLevel(state -> 4)));

    // ------------------------------------------------------------------ flowers

    public static final RegistryObject<FairyFlowerBlock> ROSY_DAISY = smallFlower("rosy_daisy", () -> MobEffects.REGENERATION, 8, MapColor.COLOR_PINK, 0);
    public static final RegistryObject<FairyFlowerBlock> LAVENDER = smallFlower("lavender", () -> MobEffects.SLOW_FALLING, 6, MapColor.COLOR_PURPLE, 0);
    public static final RegistryObject<FairyFlowerBlock> CREAM_BUTTERCUP = smallFlower("cream_buttercup", () -> MobEffects.SATURATION, 7, MapColor.SAND, 0);
    public static final RegistryObject<FairyFlowerBlock> MINT_POSY = smallFlower("mint_posy", () -> MobEffects.MOVEMENT_SPEED, 6, MapColor.COLOR_LIGHT_GREEN, 0);
    public static final RegistryObject<FairyFlowerBlock> STARFLOWER = smallFlower("starflower", () -> MobEffects.NIGHT_VISION, 5, MapColor.SNOW, 0);
    public static final RegistryObject<FairyFlowerBlock> BLUEBELL = smallFlower("bluebell", () -> MobEffects.WATER_BREATHING, 8, MapColor.COLOR_LIGHT_BLUE, 0);

    public static final RegistryObject<MoonflowerBlock> MOONFLOWER = register("moonflower", () -> new MoonflowerBlock(() -> MobEffects.NIGHT_VISION, 10,
            flowerProps(MapColor.COLOR_LIGHT_BLUE).lightLevel(state -> state.getValue(MoonflowerBlock.OPEN) ? 11 : 1)));

    public static final RegistryObject<FairyFlowerBlock> CRYSTAL_FLOWER = register("crystal_flower", () -> new FairyFlowerBlock(() -> MobEffects.GLOWING, 8,
            ModParticles.FAIRY_SPARKLE, 8, flowerProps(MapColor.COLOR_MAGENTA).sound(SoundType.SMALL_AMETHYST_BUD).lightLevel(state -> 7)));

    static {
        SMALL_FLOWERS.add(MOONFLOWER);
        SMALL_FLOWERS.add(CRYSTAL_FLOWER);
    }

    public static final RegistryObject<TallFlowerBlock> TALL_LAVENDER = tallFlower("tall_lavender", MapColor.COLOR_PURPLE);
    public static final RegistryObject<TallFlowerBlock> FAIRY_FOXGLOVE = tallFlower("fairy_foxglove", MapColor.COLOR_PINK);

    // ------------------------------------------------------------------ ground plants & bushes

    public static final RegistryObject<ShortPlantBlock> BLUSH_GRASS = register("blush_grass", () -> new ShortPlantBlock(groundPlant(MapColor.COLOR_PINK)));
    public static final RegistryObject<ShortPlantBlock> MOONLIT_GRASS = register("moonlit_grass", () -> new ShortPlantBlock(groundPlant(MapColor.COLOR_BLUE)));

    public static final RegistryObject<FloweringBushBlock> PINK_HYDRANGEA = bush("pink_hydrangea", MapColor.COLOR_PINK);
    public static final RegistryObject<FloweringBushBlock> BLUE_HYDRANGEA = bush("blue_hydrangea", MapColor.COLOR_LIGHT_BLUE);
    public static final RegistryObject<FloweringBushBlock> LAVENDER_HYDRANGEA = bush("lavender_hydrangea", MapColor.COLOR_PURPLE);

    public static final RegistryObject<StrawberryBushBlock> STRAWBERRY_BUSH = registerNoItem("strawberry_bush",
            () -> new StrawberryBushBlock(BlockBehaviour.Properties.of().mapColor(MapColor.PLANT).randomTicks().noCollission()
                    .sound(SoundType.SWEET_BERRY_BUSH).pushReaction(PushReaction.DESTROY)));

    public static final RegistryObject<StarbloomCropBlock> STARBLOOM = registerNoItem("starbloom",
            () -> new StarbloomCropBlock(BlockBehaviour.Properties.of().mapColor(MapColor.PLANT).noCollission().randomTicks().instabreak()
                    .sound(SoundType.CROP).pushReaction(PushReaction.DESTROY)
                    .lightLevel(state -> switch (state.getValue(StarbloomCropBlock.AGE)) {
                        case 3 -> 10;
                        case 2 -> 4;
                        default -> 0;
                    })));

    // ------------------------------------------------------------------ mushrooms

    public static final RegistryObject<FairyMushroomBlock> FAIRY_MUSHROOM = register("fairy_mushroom",
            () -> new FairyMushroomBlock(ModConfiguredFeatures.HUGE_FAIRY_MUSHROOM, null, mushroom(MapColor.COLOR_PINK, 0)));
    public static final RegistryObject<FairyMushroomBlock> GLOWCAP = register("glowcap",
            () -> new FairyMushroomBlock(ModConfiguredFeatures.HUGE_GLOWCAP, ModParticles.MOON_GLIMMER, mushroom(MapColor.COLOR_LIGHT_BLUE, 8)));
    public static final RegistryObject<HugeMushroomBlock> FAIRY_MUSHROOM_BLOCK = register("fairy_mushroom_block",
            () -> new HugeMushroomBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_PINK).instrument(NoteBlockInstrument.BASS)
                    .strength(0.2F).sound(SoundType.WOOD).ignitedByLava()));
    public static final RegistryObject<HugeMushroomBlock> GLOWCAP_BLOCK = register("glowcap_block",
            () -> new HugeMushroomBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_LIGHT_BLUE).instrument(NoteBlockInstrument.BASS)
                    .strength(0.2F).sound(SoundType.WOOD).ignitedByLava().lightLevel(state -> 11)));

    // ------------------------------------------------------------------ magical plants

    public static final RegistryObject<MoonbellBlock> MOONBELL = register("moonbell",
            () -> new MoonbellBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_LIGHT_BLUE).noCollission().instabreak()
                    .sound(SoundType.SPORE_BLOSSOM).lightLevel(state -> 12).pushReaction(PushReaction.DESTROY)));
    public static final RegistryObject<GlimmerLilyBlock> GLIMMER_LILY = register("glimmer_lily",
            () -> new GlimmerLilyBlock(BlockBehaviour.Properties.of().mapColor(MapColor.PLANT).instabreak().sound(SoundType.LILY_PAD)
                    .noOcclusion().lightLevel(state -> 9).pushReaction(PushReaction.DESTROY)),
            block -> new PlaceOnWaterBlockItem(block, new Item.Properties()));

    // ------------------------------------------------------------------ ground cover

    public static final RegistryObject<PinkPetalsBlock> BLOSSOM_PETALS = petals("blossom_petals", MapColor.COLOR_PINK);
    public static final RegistryObject<PinkPetalsBlock> SNOW_BLOSSOM_PETALS = petals("snow_blossom_petals", MapColor.SNOW);

    public static final RegistryObject<CarpetBlock> PINK_FLOWER_CARPET = flowerCarpet("pink_flower_carpet", MapColor.COLOR_PINK);
    public static final RegistryObject<CarpetBlock> LAVENDER_FLOWER_CARPET = flowerCarpet("lavender_flower_carpet", MapColor.COLOR_PURPLE);
    public static final RegistryObject<CarpetBlock> CREAM_FLOWER_CARPET = flowerCarpet("cream_flower_carpet", MapColor.SAND);
    public static final RegistryObject<CarpetBlock> MINT_FLOWER_CARPET = flowerCarpet("mint_flower_carpet", MapColor.COLOR_LIGHT_GREEN);
    public static final RegistryObject<CarpetBlock> BLUE_FLOWER_CARPET = flowerCarpet("blue_flower_carpet", MapColor.COLOR_LIGHT_BLUE);
    public static final List<RegistryObject<CarpetBlock>> FLOWER_CARPETS = List.of(PINK_FLOWER_CARPET, LAVENDER_FLOWER_CARPET,
            CREAM_FLOWER_CARPET, MINT_FLOWER_CARPET, BLUE_FLOWER_CARPET);

    // ------------------------------------------------------------------ giant flowers

    public static final RegistryObject<RotatedPillarBlock> GIANT_FLOWER_STEM = register("giant_flower_stem",
            () -> new RotatedPillarBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_LIGHT_GREEN).strength(1.0F)
                    .sound(SoundType.STEM).ignitedByLava()));
    public static final RegistryObject<GiantPetalBlock> PINK_GIANT_PETALS = giantPetals("pink_giant_petals", MapColor.COLOR_PINK);
    public static final RegistryObject<GiantPetalBlock> LAVENDER_GIANT_PETALS = giantPetals("lavender_giant_petals", MapColor.COLOR_PURPLE);
    public static final RegistryObject<GiantPetalBlock> BLUE_GIANT_PETALS = giantPetals("blue_giant_petals", MapColor.COLOR_LIGHT_BLUE);
    public static final RegistryObject<GiantPetalBlock> CREAM_GIANT_PETALS = giantPetals("cream_giant_petals", MapColor.SAND);
    public static final List<RegistryObject<GiantPetalBlock>> GIANT_PETALS = List.of(PINK_GIANT_PETALS, LAVENDER_GIANT_PETALS,
            BLUE_GIANT_PETALS, CREAM_GIANT_PETALS);
    public static final RegistryObject<AmbientParticleBlock> GIANT_FLOWER_PISTIL = register("giant_flower_pistil",
            () -> new AmbientParticleBlock(ModParticles.POLLEN, 4, BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_YELLOW)
                    .strength(0.5F).sound(SoundType.SHROOMLIGHT).lightLevel(state -> 13)));

    // ------------------------------------------------------------------ crystals & lights

    public static final RegistryObject<FairyCrystalBlock> FAIRY_CRYSTAL_BLOCK = register("fairy_crystal_block",
            () -> new FairyCrystalBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_PINK).strength(1.5F)
                    .sound(ModSounds.FAIRY_CRYSTAL).requiresCorrectToolForDrops().lightLevel(state -> 6)));
    public static final RegistryObject<FairyCrystalClusterBlock> FAIRY_CRYSTAL_CLUSTER = register("fairy_crystal_cluster",
            () -> new FairyCrystalClusterBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_PINK).forceSolidOn().noOcclusion()
                    .sound(ModSounds.FAIRY_CRYSTAL).strength(1.5F).lightLevel(state -> 8).pushReaction(PushReaction.DESTROY)));

    public static final RegistryObject<FairyLanternBlock> FAIRY_LANTERN = register("fairy_lantern",
            () -> new FairyLanternBlock(ModParticles.FAIRY_SPARKLE, lantern(15)));
    public static final RegistryObject<FairyLanternBlock> MOON_LANTERN = register("moon_lantern",
            () -> new FairyLanternBlock(ModParticles.MOON_GLIMMER, lantern(14)));

    public static final RegistryObject<HayBlock> LAVENDER_BALE = register("lavender_bale",
            () -> new HayBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_PURPLE).instrument(NoteBlockInstrument.BANJO)
                    .strength(0.5F).sound(SoundType.GRASS)));

    // ------------------------------------------------------------------ potted plants

    static {
        for (RegistryObject<? extends Block> flower : List.copyOf(SMALL_FLOWERS)) {
            int light = flower == MOONFLOWER ? 9 : flower == CRYSTAL_FLOWER ? 7 : 0;
            potted(flower, light);
        }
        potted(BLOSSOM_SAPLING, 0);
        potted(SNOW_BLOSSOM_SAPLING, 0);
        potted(MOONWOOD_SAPLING, 4);
        potted(FAIRY_MUSHROOM, 0);
        potted(GLOWCAP, 8);
    }

    // ------------------------------------------------------------------ helpers

    static <T extends Block> RegistryObject<T> register(String name, Supplier<T> block) {
        return register(name, block, b -> new BlockItem(b, new Item.Properties()));
    }

    static <T extends Block> RegistryObject<T> register(String name, Supplier<T> block, Function<T, Item> item) {
        RegistryObject<T> registered = BLOCKS.register(name, block);
        ModItems.registerBlockItem(name, () -> item.apply(registered.get()));
        return registered;
    }

    static <T extends Block> RegistryObject<T> registerNoItem(String name, Supplier<T> block) {
        return BLOCKS.register(name, block);
    }

    private static RegistryObject<FairyFlowerBlock> smallFlower(String name, Supplier<MobEffect> effect, int duration, MapColor color, int light) {
        RegistryObject<FairyFlowerBlock> flower = register(name, () -> new FairyFlowerBlock(effect, duration, null, 1,
                flowerProps(color).lightLevel(state -> light)));
        SMALL_FLOWERS.add(flower);
        return flower;
    }

    private static RegistryObject<TallFlowerBlock> tallFlower(String name, MapColor color) {
        RegistryObject<TallFlowerBlock> flower = register(name, () -> new TallFlowerBlock(flowerProps(color).ignitedByLava()),
                block -> new DoubleHighBlockItem(block, new Item.Properties()));
        TALL_FLOWERS.add(flower);
        return flower;
    }

    private static RegistryObject<FloweringBushBlock> bush(String name, MapColor color) {
        return register(name, () -> new FloweringBushBlock(BlockBehaviour.Properties.of().mapColor(color).strength(0.1F)
                .sound(SoundType.AZALEA).noOcclusion().ignitedByLava().pushReaction(PushReaction.DESTROY)));
    }

    private static RegistryObject<PinkPetalsBlock> petals(String name, MapColor color) {
        return register(name, () -> new PinkPetalsBlock(BlockBehaviour.Properties.of().mapColor(color).noCollission()
                .sound(SoundType.PINK_PETALS).pushReaction(PushReaction.DESTROY)));
    }

    private static RegistryObject<CarpetBlock> flowerCarpet(String name, MapColor color) {
        // See-through carpets must not occlude the top face of the block they lie on.
        return register(name, () -> new CarpetBlock(BlockBehaviour.Properties.of().mapColor(color).strength(0.1F)
                .sound(SoundType.PINK_PETALS).noOcclusion().ignitedByLava().pushReaction(PushReaction.DESTROY)));
    }

    private static RegistryObject<GiantPetalBlock> giantPetals(String name, MapColor color) {
        return register(name, () -> new GiantPetalBlock(BlockBehaviour.Properties.of().mapColor(color).strength(0.5F)
                .sound(SoundType.AZALEA_LEAVES).ignitedByLava()));
    }

    private static void potted(RegistryObject<? extends Block> plant, int light) {
        RegistryObject<FlowerPotBlock> pot = registerNoItem("potted_" + plant.getId().getPath(),
                () -> new FlowerPotBlock(() -> (FlowerPotBlock) Blocks.FLOWER_POT, plant, BlockBehaviour.Properties.of()
                        .instabreak().noOcclusion().pushReaction(PushReaction.DESTROY).lightLevel(state -> light)));
        POTTED.put(plant, pot);
    }

    static BlockBehaviour.Properties flowerProps(MapColor color) {
        return BlockBehaviour.Properties.of().mapColor(color).noCollission().instabreak().sound(SoundType.GRASS)
                .offsetType(BlockBehaviour.OffsetType.XZ).pushReaction(PushReaction.DESTROY);
    }

    private static BlockBehaviour.Properties groundPlant(MapColor color) {
        return BlockBehaviour.Properties.of().mapColor(color).replaceable().noCollission().instabreak().sound(SoundType.GRASS)
                .offsetType(BlockBehaviour.OffsetType.XYZ).ignitedByLava().pushReaction(PushReaction.DESTROY);
    }

    private static BlockBehaviour.Properties mushroom(MapColor color, int light) {
        return BlockBehaviour.Properties.of().mapColor(color).noCollission().instabreak().sound(SoundType.GRASS)
                .lightLevel(state -> light).pushReaction(PushReaction.DESTROY);
    }

    private static BlockBehaviour.Properties leaves(MapColor color, SoundType sound) {
        return BlockBehaviour.Properties.of().mapColor(color).strength(0.2F).randomTicks().sound(sound).noOcclusion()
                .isValidSpawn((state, level, pos, type) -> false)
                .isSuffocating((state, level, pos) -> false).isViewBlocking((state, level, pos) -> false)
                .ignitedByLava().pushReaction(PushReaction.DESTROY).isRedstoneConductor((state, level, pos) -> false);
    }

    private static BlockBehaviour.Properties sapling(MapColor color, SoundType sound) {
        return BlockBehaviour.Properties.of().mapColor(color).noCollission().randomTicks().instabreak().sound(sound)
                .pushReaction(PushReaction.DESTROY);
    }

    private static BlockBehaviour.Properties lantern(int light) {
        return BlockBehaviour.Properties.of().mapColor(MapColor.METAL).forceSolidOn().requiresCorrectToolForDrops().strength(3.5F)
                .sound(SoundType.LANTERN).lightLevel(state -> light).noOcclusion().pushReaction(PushReaction.DESTROY);
    }

    private ModBlocks() {
    }
}
