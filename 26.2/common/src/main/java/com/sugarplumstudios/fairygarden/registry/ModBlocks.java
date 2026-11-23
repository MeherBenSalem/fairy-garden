package com.sugarplumstudios.fairygarden.registry;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import com.sugarplumstudios.fairygarden.platform.Services;
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
import net.minecraft.world.level.block.FlowerBedBlock;
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
import com.sugarplumstudios.fairygarden.platform.RegistryEntry;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

public final class ModBlocks {

    /** Every Fairy Garden block, in registration order. */
    public static final List<RegistryEntry<? extends Block>> ALL = new ArrayList<>();
    /** Plant block to its potted form, in registration order. */
    public static final Map<RegistryEntry<? extends Block>, RegistryEntry<FlowerPotBlock>> POTTED = new LinkedHashMap<>();
    public static final List<RegistryEntry<? extends Block>> SMALL_FLOWERS = new ArrayList<>();
    public static final List<RegistryEntry<? extends Block>> TALL_FLOWERS = new ArrayList<>();

    // ------------------------------------------------------------------ woods

    public static final WoodSet BLOSSOM = new WoodSet("blossom", ModWoodTypes.BLOSSOM,
            MapColor.COLOR_PINK, MapColor.TERRACOTTA_BROWN, SoundType.CHERRY_WOOD, SoundType.CHERRY_WOOD_HANGING_SIGN);
    public static final WoodSet MOONWOOD = new WoodSet("moonwood", ModWoodTypes.MOONWOOD,
            MapColor.COLOR_LIGHT_BLUE, MapColor.COLOR_BLUE, SoundType.WOOD, SoundType.HANGING_SIGN);
    public static final List<WoodSet> WOOD_SETS = List.of(BLOSSOM, MOONWOOD);

    public static final RegistryEntry<FairyLeavesBlock> BLOSSOM_LEAVES = register("blossom_leaves",
            () -> new FairyLeavesBlock(ModParticles.BLOSSOM_PETAL, 10, false, leaves(MapColor.COLOR_PINK, SoundType.CHERRY_LEAVES)));
    public static final RegistryEntry<FairyLeavesBlock> SNOW_BLOSSOM_LEAVES = register("snow_blossom_leaves",
            () -> new FairyLeavesBlock(ModParticles.SNOW_PETAL, 10, false, leaves(MapColor.SNOW, SoundType.CHERRY_LEAVES)));
    public static final RegistryEntry<FairyLeavesBlock> MOONWOOD_LEAVES = register("moonwood_leaves",
            () -> new FairyLeavesBlock(ModParticles.MOON_GLIMMER, 45, true, leaves(MapColor.COLOR_PURPLE, SoundType.AZALEA_LEAVES)));

    public static final RegistryEntry<SaplingBlock> BLOSSOM_SAPLING = register("blossom_sapling",
            () -> new SaplingBlock(FairyTreeGrower.BLOSSOM,
                    sapling(MapColor.COLOR_PINK, SoundType.CHERRY_SAPLING)) { });
    public static final RegistryEntry<SaplingBlock> SNOW_BLOSSOM_SAPLING = register("snow_blossom_sapling",
            () -> new SaplingBlock(FairyTreeGrower.SNOW_BLOSSOM,
                    sapling(MapColor.SNOW, SoundType.CHERRY_SAPLING)) { });
    public static final RegistryEntry<SaplingBlock> MOONWOOD_SAPLING = register("moonwood_sapling",
            () -> new SaplingBlock(FairyTreeGrower.MOONWOOD,
                    sapling(MapColor.COLOR_PURPLE, SoundType.GRASS).lightLevel(state -> 4)) { });

    // ------------------------------------------------------------------ flowers

    public static final RegistryEntry<FairyFlowerBlock> ROSY_DAISY = smallFlower("rosy_daisy", MobEffects.REGENERATION, 8, MapColor.COLOR_PINK, 0);
    public static final RegistryEntry<FairyFlowerBlock> LAVENDER = smallFlower("lavender", MobEffects.SLOW_FALLING, 6, MapColor.COLOR_PURPLE, 0);
    public static final RegistryEntry<FairyFlowerBlock> CREAM_BUTTERCUP = smallFlower("cream_buttercup", MobEffects.SATURATION, 7, MapColor.SAND, 0);
    public static final RegistryEntry<FairyFlowerBlock> MINT_POSY = smallFlower("mint_posy", MobEffects.SPEED, 6, MapColor.COLOR_LIGHT_GREEN, 0);
    public static final RegistryEntry<FairyFlowerBlock> STARFLOWER = smallFlower("starflower", MobEffects.NIGHT_VISION, 5, MapColor.SNOW, 0);
    public static final RegistryEntry<FairyFlowerBlock> BLUEBELL = smallFlower("bluebell", MobEffects.WATER_BREATHING, 8, MapColor.COLOR_LIGHT_BLUE, 0);

    public static final RegistryEntry<MoonflowerBlock> MOONFLOWER = register("moonflower", () -> new MoonflowerBlock(MobEffects.NIGHT_VISION, 10,
            flowerProps(MapColor.COLOR_LIGHT_BLUE).lightLevel(state -> state.getValue(MoonflowerBlock.OPEN) ? 11 : 1)));

    public static final RegistryEntry<FairyFlowerBlock> CRYSTAL_FLOWER = register("crystal_flower", () -> new FairyFlowerBlock(MobEffects.GLOWING, 8,
            ModParticles.FAIRY_SPARKLE, 8, flowerProps(MapColor.COLOR_MAGENTA).sound(SoundType.SMALL_AMETHYST_BUD).lightLevel(state -> 7)));

    static {
        SMALL_FLOWERS.add(MOONFLOWER);
        SMALL_FLOWERS.add(CRYSTAL_FLOWER);
    }

    public static final RegistryEntry<TallFlowerBlock> TALL_LAVENDER = tallFlower("tall_lavender", MapColor.COLOR_PURPLE);
    public static final RegistryEntry<TallFlowerBlock> FAIRY_FOXGLOVE = tallFlower("fairy_foxglove", MapColor.COLOR_PINK);

    // ------------------------------------------------------------------ ground plants & bushes

    public static final RegistryEntry<Block> BLUSH_GRASS = register("blush_grass", () -> new ShortPlantBlock(groundPlant(MapColor.COLOR_PINK)));
    public static final RegistryEntry<Block> MOONLIT_GRASS = register("moonlit_grass", () -> new ShortPlantBlock(groundPlant(MapColor.COLOR_BLUE)));

    public static final RegistryEntry<FloweringBushBlock> PINK_HYDRANGEA = bush("pink_hydrangea", MapColor.COLOR_PINK);
    public static final RegistryEntry<FloweringBushBlock> BLUE_HYDRANGEA = bush("blue_hydrangea", MapColor.COLOR_LIGHT_BLUE);
    public static final RegistryEntry<FloweringBushBlock> LAVENDER_HYDRANGEA = bush("lavender_hydrangea", MapColor.COLOR_PURPLE);

    public static final RegistryEntry<StrawberryBushBlock> STRAWBERRY_BUSH = registerNoItem("strawberry_bush",
            () -> new StrawberryBushBlock(Keys.blockProps().mapColor(MapColor.PLANT).randomTicks().noCollision()
                    .sound(SoundType.SWEET_BERRY_BUSH).pushReaction(PushReaction.DESTROY)));

    public static final RegistryEntry<StarbloomCropBlock> STARBLOOM = registerNoItem("starbloom",
            () -> new StarbloomCropBlock(Keys.blockProps().mapColor(MapColor.PLANT).noCollision().randomTicks().instabreak()
                    .sound(SoundType.CROP).pushReaction(PushReaction.DESTROY)
                    .lightLevel(state -> switch (state.getValue(StarbloomCropBlock.AGE)) {
                        case 3 -> 10;
                        case 2 -> 4;
                        default -> 0;
                    })));

    // ------------------------------------------------------------------ mushrooms

    public static final RegistryEntry<FairyMushroomBlock> FAIRY_MUSHROOM = register("fairy_mushroom",
            () -> new FairyMushroomBlock(ModConfiguredFeatures.HUGE_FAIRY_MUSHROOM, null, mushroom(MapColor.COLOR_PINK, 0)));
    public static final RegistryEntry<FairyMushroomBlock> GLOWCAP = register("glowcap",
            () -> new FairyMushroomBlock(ModConfiguredFeatures.HUGE_GLOWCAP, ModParticles.MOON_GLIMMER, mushroom(MapColor.COLOR_LIGHT_BLUE, 8)));
    public static final RegistryEntry<HugeMushroomBlock> FAIRY_MUSHROOM_BLOCK = register("fairy_mushroom_block",
            () -> new HugeMushroomBlock(Keys.blockProps().mapColor(MapColor.COLOR_PINK).instrument(NoteBlockInstrument.BASS)
                    .strength(0.2F).sound(SoundType.WOOD).ignitedByLava()));
    public static final RegistryEntry<HugeMushroomBlock> GLOWCAP_BLOCK = register("glowcap_block",
            () -> new HugeMushroomBlock(Keys.blockProps().mapColor(MapColor.COLOR_LIGHT_BLUE).instrument(NoteBlockInstrument.BASS)
                    .strength(0.2F).sound(SoundType.WOOD).ignitedByLava().lightLevel(state -> 11)));

    // ------------------------------------------------------------------ magical plants

    public static final RegistryEntry<MoonbellBlock> MOONBELL = register("moonbell",
            () -> new MoonbellBlock(Keys.blockProps().mapColor(MapColor.COLOR_LIGHT_BLUE).noCollision().instabreak()
                    .sound(SoundType.SPORE_BLOSSOM).lightLevel(state -> 12).pushReaction(PushReaction.DESTROY)));
    public static final RegistryEntry<GlimmerLilyBlock> GLIMMER_LILY = register("glimmer_lily",
            () -> new GlimmerLilyBlock(Keys.blockProps().mapColor(MapColor.PLANT).instabreak().sound(SoundType.LILY_PAD)
                    .noOcclusion().lightLevel(state -> 9).pushReaction(PushReaction.DESTROY)),
            block -> new PlaceOnWaterBlockItem(block, Keys.itemProps().useBlockDescriptionPrefix()));

    // ------------------------------------------------------------------ ground cover

    public static final RegistryEntry<FlowerBedBlock> BLOSSOM_PETALS = petals("blossom_petals", MapColor.COLOR_PINK);
    public static final RegistryEntry<FlowerBedBlock> SNOW_BLOSSOM_PETALS = petals("snow_blossom_petals", MapColor.SNOW);

    public static final RegistryEntry<CarpetBlock> PINK_FLOWER_CARPET = flowerCarpet("pink_flower_carpet", MapColor.COLOR_PINK);
    public static final RegistryEntry<CarpetBlock> LAVENDER_FLOWER_CARPET = flowerCarpet("lavender_flower_carpet", MapColor.COLOR_PURPLE);
    public static final RegistryEntry<CarpetBlock> CREAM_FLOWER_CARPET = flowerCarpet("cream_flower_carpet", MapColor.SAND);
    public static final RegistryEntry<CarpetBlock> MINT_FLOWER_CARPET = flowerCarpet("mint_flower_carpet", MapColor.COLOR_LIGHT_GREEN);
    public static final RegistryEntry<CarpetBlock> BLUE_FLOWER_CARPET = flowerCarpet("blue_flower_carpet", MapColor.COLOR_LIGHT_BLUE);
    public static final List<RegistryEntry<CarpetBlock>> FLOWER_CARPETS = List.of(PINK_FLOWER_CARPET, LAVENDER_FLOWER_CARPET,
            CREAM_FLOWER_CARPET, MINT_FLOWER_CARPET, BLUE_FLOWER_CARPET);

    // ------------------------------------------------------------------ giant flowers

    public static final RegistryEntry<RotatedPillarBlock> GIANT_FLOWER_STEM = register("giant_flower_stem",
            () -> new RotatedPillarBlock(Keys.blockProps().mapColor(MapColor.COLOR_LIGHT_GREEN).strength(1.0F)
                    .sound(SoundType.STEM).ignitedByLava()));
    public static final RegistryEntry<GiantPetalBlock> PINK_GIANT_PETALS = giantPetals("pink_giant_petals", MapColor.COLOR_PINK);
    public static final RegistryEntry<GiantPetalBlock> LAVENDER_GIANT_PETALS = giantPetals("lavender_giant_petals", MapColor.COLOR_PURPLE);
    public static final RegistryEntry<GiantPetalBlock> BLUE_GIANT_PETALS = giantPetals("blue_giant_petals", MapColor.COLOR_LIGHT_BLUE);
    public static final RegistryEntry<GiantPetalBlock> CREAM_GIANT_PETALS = giantPetals("cream_giant_petals", MapColor.SAND);
    public static final List<RegistryEntry<GiantPetalBlock>> GIANT_PETALS = List.of(PINK_GIANT_PETALS, LAVENDER_GIANT_PETALS,
            BLUE_GIANT_PETALS, CREAM_GIANT_PETALS);
    public static final RegistryEntry<AmbientParticleBlock> GIANT_FLOWER_PISTIL = register("giant_flower_pistil",
            () -> new AmbientParticleBlock(ModParticles.POLLEN, 4, Keys.blockProps().mapColor(MapColor.COLOR_YELLOW)
                    .strength(0.5F).sound(SoundType.SHROOMLIGHT).lightLevel(state -> 13)));

    // ------------------------------------------------------------------ crystals & lights

    public static final RegistryEntry<FairyCrystalBlock> FAIRY_CRYSTAL_BLOCK = register("fairy_crystal_block",
            () -> new FairyCrystalBlock(Keys.blockProps().mapColor(MapColor.COLOR_PINK).strength(1.5F)
                    .sound(ModSounds.fairyCrystal()).requiresCorrectToolForDrops().lightLevel(state -> 6)));
    public static final RegistryEntry<FairyCrystalClusterBlock> FAIRY_CRYSTAL_CLUSTER = register("fairy_crystal_cluster",
            () -> new FairyCrystalClusterBlock(Keys.blockProps().mapColor(MapColor.COLOR_PINK).forceSolidOn().noOcclusion()
                    .sound(ModSounds.fairyCrystal()).strength(1.5F).lightLevel(state -> 8).pushReaction(PushReaction.DESTROY)));

    public static final RegistryEntry<FairyLanternBlock> FAIRY_LANTERN = register("fairy_lantern",
            () -> new FairyLanternBlock(ModParticles.FAIRY_SPARKLE, lantern(15)));
    public static final RegistryEntry<FairyLanternBlock> MOON_LANTERN = register("moon_lantern",
            () -> new FairyLanternBlock(ModParticles.MOON_GLIMMER, lantern(14)));

    public static final RegistryEntry<HayBlock> LAVENDER_BALE = register("lavender_bale",
            () -> new HayBlock(Keys.blockProps().mapColor(MapColor.COLOR_PURPLE).instrument(NoteBlockInstrument.BANJO)
                    .strength(0.5F).sound(SoundType.GRASS)));

    // ------------------------------------------------------------------ potted plants

    static {
        for (RegistryEntry<? extends Block> flower : List.copyOf(SMALL_FLOWERS)) {
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

    static <T extends Block> RegistryEntry<T> register(String name, Supplier<T> block) {
        return register(name, block, b -> new BlockItem(b, Keys.itemProps().useBlockDescriptionPrefix()));
    }

    static <T extends Block> RegistryEntry<T> register(String name, Supplier<T> block, Function<T, Item> item) {
        RegistryEntry<T> registered = registerNoItem(name, block);
        ModItems.registerBlockItem(name, () -> item.apply(registered.get()));
        return registered;
    }

    static <T extends Block> RegistryEntry<T> registerNoItem(String name, Supplier<T> block) {
        RegistryEntry<T> registered = Services.PLATFORM.register(Registries.BLOCK, name, block);
        ALL.add(registered);
        return registered;
    }

    private static RegistryEntry<FairyFlowerBlock> smallFlower(String name, Holder<MobEffect> effect, int duration, MapColor color, int light) {
        RegistryEntry<FairyFlowerBlock> flower = register(name, () -> new FairyFlowerBlock(effect, duration, null, 1,
                flowerProps(color).lightLevel(state -> light)));
        SMALL_FLOWERS.add(flower);
        return flower;
    }

    private static RegistryEntry<TallFlowerBlock> tallFlower(String name, MapColor color) {
        RegistryEntry<TallFlowerBlock> flower = register(name, () -> new TallFlowerBlock(flowerProps(color).ignitedByLava()),
                block -> new DoubleHighBlockItem(block, Keys.itemProps().useBlockDescriptionPrefix()));
        TALL_FLOWERS.add(flower);
        return flower;
    }

    private static RegistryEntry<FloweringBushBlock> bush(String name, MapColor color) {
        return register(name, () -> new FloweringBushBlock(Keys.blockProps().mapColor(color).strength(0.1F)
                .sound(SoundType.AZALEA).noOcclusion().ignitedByLava().pushReaction(PushReaction.DESTROY)));
    }

    private static RegistryEntry<FlowerBedBlock> petals(String name, MapColor color) {
        return register(name, () -> new FlowerBedBlock(Keys.blockProps().mapColor(color).noCollision()
                .sound(SoundType.PINK_PETALS).pushReaction(PushReaction.DESTROY)) { });
    }

    private static RegistryEntry<CarpetBlock> flowerCarpet(String name, MapColor color) {
        // See-through carpets must not occlude the top face of the block they lie on.
        return register(name, () -> new CarpetBlock(Keys.blockProps().mapColor(color).strength(0.1F)
                .sound(SoundType.PINK_PETALS).noOcclusion().ignitedByLava().pushReaction(PushReaction.DESTROY)));
    }

    private static RegistryEntry<GiantPetalBlock> giantPetals(String name, MapColor color) {
        return register(name, () -> new GiantPetalBlock(Keys.blockProps().mapColor(color).strength(0.5F)
                .sound(SoundType.AZALEA_LEAVES).ignitedByLava()));
    }

    private static void potted(RegistryEntry<? extends Block> plant, int light) {
        RegistryEntry<FlowerPotBlock> pot = registerNoItem("potted_" + plant.getId().getPath(),
                () -> Services.PLATFORM.flowerPot(plant, Keys.blockProps()
                        .instabreak().noOcclusion().pushReaction(PushReaction.DESTROY).lightLevel(state -> light)));
        POTTED.put(plant, pot);
    }

    static BlockBehaviour.Properties flowerProps(MapColor color) {
        return Keys.blockProps().mapColor(color).noCollision().instabreak().sound(SoundType.GRASS)
                .offsetType(BlockBehaviour.OffsetType.XZ).pushReaction(PushReaction.DESTROY);
    }

    private static BlockBehaviour.Properties groundPlant(MapColor color) {
        return Keys.blockProps().mapColor(color).replaceable().noCollision().instabreak().sound(SoundType.GRASS)
                .offsetType(BlockBehaviour.OffsetType.XYZ).ignitedByLava().pushReaction(PushReaction.DESTROY);
    }

    private static BlockBehaviour.Properties mushroom(MapColor color, int light) {
        return Keys.blockProps().mapColor(color).noCollision().instabreak().sound(SoundType.GRASS)
                .lightLevel(state -> light).pushReaction(PushReaction.DESTROY);
    }

    private static BlockBehaviour.Properties leaves(MapColor color, SoundType sound) {
        return Keys.blockProps().mapColor(color).strength(0.2F).randomTicks().sound(sound).noOcclusion()
                .isValidSpawn((state, level, pos, type) -> false)
                .isSuffocating((state, level, pos) -> false).isViewBlocking((state, level, pos) -> false)
                .ignitedByLava().pushReaction(PushReaction.DESTROY).isRedstoneConductor((state, level, pos) -> false);
    }

    private static BlockBehaviour.Properties sapling(MapColor color, SoundType sound) {
        return Keys.blockProps().mapColor(color).noCollision().randomTicks().instabreak().sound(sound)
                .pushReaction(PushReaction.DESTROY);
    }

    private static BlockBehaviour.Properties lantern(int light) {
        return Keys.blockProps().mapColor(MapColor.METAL).forceSolidOn().requiresCorrectToolForDrops().strength(3.5F)
                .sound(SoundType.LANTERN).lightLevel(state -> light).noOcclusion().pushReaction(PushReaction.DESTROY);
    }

    /** Loads the class, which registers everything above. */
    public static void init() {
    }

    private ModBlocks() {
    }
}
