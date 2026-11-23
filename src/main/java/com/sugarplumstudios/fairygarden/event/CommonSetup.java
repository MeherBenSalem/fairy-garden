package com.sugarplumstudios.fairygarden.event;

import com.sugarplumstudios.fairygarden.FairyGarden;
import com.sugarplumstudios.fairygarden.config.FairyConfig;
import com.sugarplumstudios.fairygarden.entity.Butterfly;
import com.sugarplumstudios.fairygarden.entity.MoonWisp;
import com.sugarplumstudios.fairygarden.registry.ModBlocks;
import com.sugarplumstudios.fairygarden.registry.ModEntities;
import com.sugarplumstudios.fairygarden.registry.ModItems;
import com.sugarplumstudios.fairygarden.registry.WoodSet;
import com.sugarplumstudios.fairygarden.worldgen.FairyGardenRegion;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ComposterBlock;
import net.minecraft.world.level.block.FireBlock;
import net.minecraft.world.level.block.FlowerPotBlock;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.event.entity.SpawnPlacementRegisterEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import terrablender.api.Regions;

import java.lang.reflect.Method;

public final class CommonSetup {
    private CommonSetup() {
    }

    public static void onCommonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            int weight = FairyConfig.REGION_WEIGHT.get();
            if (weight > 0 && FairyGardenRegion.anyBiomeEnabled()) {
                Regions.register(new FairyGardenRegion(weight));
                FairyGarden.LOGGER.info("Registered the Fairy Garden overworld region with weight {}", weight);
            } else {
                FairyGarden.LOGGER.info("Fairy Garden biomes are disabled in the config; no region registered");
            }

            FlowerPotBlock pot = (FlowerPotBlock) Blocks.FLOWER_POT;
            ModBlocks.POTTED.forEach((plant, potted) -> pot.addPlant(plant.getId(), potted));

            registerFlammability();
            registerCompostables();
        });
    }

    private static void registerFlammability() {
        Flammables fire = new Flammables();
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

    /** {@code FireBlock.setFlammable} is private in 1.20.1, so it is called reflectively (SRG name m_53444_). */
    private static final class Flammables {
        private final FireBlock fire = (FireBlock) Blocks.FIRE;
        private final Method setFlammable = ObfuscationReflectionHelper.findMethod(FireBlock.class, "m_53444_", Block.class, int.class, int.class);

        void setFlammable(Block block, int encouragement, int flammability) {
            try {
                setFlammable.invoke(fire, block, encouragement, flammability);
            } catch (ReflectiveOperationException e) {
                throw new IllegalStateException("Could not make " + block + " flammable", e);
            }
        }
    }

    private static void registerCompostables() {
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

    public static void onEntityAttributes(EntityAttributeCreationEvent event) {
        event.put(ModEntities.BUTTERFLY.get(), Butterfly.createAttributes().build());
        event.put(ModEntities.MOON_WISP.get(), MoonWisp.createAttributes().build());
    }

    public static void onSpawnPlacements(SpawnPlacementRegisterEvent event) {
        event.register(ModEntities.BUTTERFLY.get(), SpawnPlacements.Type.NO_RESTRICTIONS, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Butterfly::checkSpawnRules, SpawnPlacementRegisterEvent.Operation.REPLACE);
        event.register(ModEntities.MOON_WISP.get(), SpawnPlacements.Type.NO_RESTRICTIONS, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                MoonWisp::checkSpawnRules, SpawnPlacementRegisterEvent.Operation.REPLACE);
    }
}
