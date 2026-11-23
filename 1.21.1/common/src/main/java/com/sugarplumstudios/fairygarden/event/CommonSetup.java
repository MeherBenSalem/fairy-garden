package com.sugarplumstudios.fairygarden.event;

import com.sugarplumstudios.fairygarden.registry.ModBlocks;
import com.sugarplumstudios.fairygarden.registry.ModItems;
import com.sugarplumstudios.fairygarden.registry.WoodSet;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ComposterBlock;

/**
 * Loader-independent setup that runs once the registries are filled: which blocks burn and what composts.
 * Each loader applies the flammability through its own hook (Forge: FireBlock, Fabric: FlammableBlockRegistry).
 */
public final class CommonSetup {
    private CommonSetup() {
    }

    /** Receives a block with its fire encouragement and flammability, like {@code FireBlock.setFlammable}. */
    @FunctionalInterface
    public interface Flammables {
        void setFlammable(Block block, int encouragement, int flammability);
    }

    public static void registerFlammability(Flammables fire) {
        for (WoodSet set : ModBlocks.WOOD_SETS) {
            for (Block log : new Block[]{set.log.get(), set.wood.get(), set.strippedLog.get(), set.strippedWood.get()}) {
                fire.setFlammable(log, 5, 5);
            }
            for (Block block : new Block[]{set.planks.get(), set.stairs.get(), set.slab.get(), set.fence.get(), set.fenceGate.get()}) {
                fire.setFlammable(block, 5, 20);
            }
        }
        for (Block leaves : new Block[]{ModBlocks.BLOSSOM_LEAVES.get(), ModBlocks.SNOW_BLOSSOM_LEAVES.get(), ModBlocks.MOONWOOD_LEAVES.get()}) {
            fire.setFlammable(leaves, 30, 60);
        }
        ModBlocks.SMALL_FLOWERS.forEach(flower -> fire.setFlammable(flower.get(), 60, 100));
        ModBlocks.TALL_FLOWERS.forEach(flower -> fire.setFlammable(flower.get(), 60, 100));
        ModBlocks.FLOWER_CARPETS.forEach(carpet -> fire.setFlammable(carpet.get(), 60, 20));
        ModBlocks.GIANT_PETALS.forEach(petals -> fire.setFlammable(petals.get(), 30, 60));
        for (Block plant : new Block[]{ModBlocks.BLUSH_GRASS.get(), ModBlocks.MOONLIT_GRASS.get(), ModBlocks.BLOSSOM_PETALS.get(),
                ModBlocks.SNOW_BLOSSOM_PETALS.get(), ModBlocks.PINK_HYDRANGEA.get(), ModBlocks.BLUE_HYDRANGEA.get(),
                ModBlocks.LAVENDER_HYDRANGEA.get(), ModBlocks.LAVENDER_BALE.get(), ModBlocks.GIANT_FLOWER_STEM.get()}) {
            fire.setFlammable(plant, 60, 100);
        }
    }

    public static void registerCompostables() {
        ModBlocks.SMALL_FLOWERS.forEach(flower -> compost(flower.get(), 0.65F));
        ModBlocks.TALL_FLOWERS.forEach(flower -> compost(flower.get(), 0.65F));
        ModBlocks.FLOWER_CARPETS.forEach(carpet -> compost(carpet.get(), 0.3F));
        for (ItemLike item : new ItemLike[]{ModBlocks.BLOSSOM_LEAVES.get(), ModBlocks.SNOW_BLOSSOM_LEAVES.get(), ModBlocks.MOONWOOD_LEAVES.get(),
                ModBlocks.BLOSSOM_SAPLING.get(), ModBlocks.SNOW_BLOSSOM_SAPLING.get(), ModBlocks.MOONWOOD_SAPLING.get(),
                ModBlocks.BLUSH_GRASS.get(), ModBlocks.MOONLIT_GRASS.get(), ModBlocks.BLOSSOM_PETALS.get(), ModBlocks.SNOW_BLOSSOM_PETALS.get(),
                ModItems.STRAWBERRY.get(), ModItems.STARBLOOM_SEEDS.get(), ModItems.MOON_PETAL.get()}) {
            compost(item, 0.3F);
        }
        for (ItemLike item : new ItemLike[]{ModBlocks.FAIRY_MUSHROOM.get(), ModBlocks.GLOWCAP.get(), ModBlocks.PINK_HYDRANGEA.get(),
                ModBlocks.BLUE_HYDRANGEA.get(), ModBlocks.LAVENDER_HYDRANGEA.get(), ModBlocks.GLIMMER_LILY.get(), ModBlocks.MOONBELL.get()}) {
            compost(item, 0.65F);
        }
        for (ItemLike item : new ItemLike[]{ModBlocks.FAIRY_MUSHROOM_BLOCK.get(), ModBlocks.GLOWCAP_BLOCK.get(), ModBlocks.LAVENDER_BALE.get(),
                ModItems.STRAWBERRY_SHORTCAKE.get()}) {
            compost(item, 0.85F);
        }
    }

    private static void compost(ItemLike item, float chance) {
        ComposterBlock.COMPOSTABLES.put(item.asItem(), chance);
    }
}
