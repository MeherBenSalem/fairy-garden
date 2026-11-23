package com.sugarplumstudios.fairygarden.forge;

import com.sugarplumstudios.fairygarden.FairyGarden;
import com.sugarplumstudios.fairygarden.config.FairyConfig;
import com.sugarplumstudios.fairygarden.entity.Butterfly;
import com.sugarplumstudios.fairygarden.entity.MoonWisp;
import com.sugarplumstudios.fairygarden.event.CommonSetup;
import com.sugarplumstudios.fairygarden.registry.ModBlocks;
import com.sugarplumstudios.fairygarden.registry.ModEntities;
import com.sugarplumstudios.fairygarden.worldgen.FairyGardenRegion;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FireBlock;
import net.minecraft.world.level.block.FlowerPotBlock;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.event.entity.SpawnPlacementRegisterEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import terrablender.api.Regions;

import java.lang.reflect.Method;

@Mod(FairyGarden.MOD_ID)
public final class FairyGardenForge {
    public FairyGardenForge() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        FairyGarden.init();
        ForgePlatform.registerAll(modBus);

        modBus.addListener(FairyGardenForge::onCommonSetup);
        modBus.addListener(FairyGardenForge::onEntityAttributes);
        modBus.addListener(FairyGardenForge::onSpawnPlacements);
        if (FMLEnvironment.dist == Dist.CLIENT) {
            FairyConfig.loadClient();
        }
    }

    private static void onCommonSetup(FMLCommonSetupEvent event) {
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

            CommonSetup.registerFlammability(new Flammables()::setFlammable);
            CommonSetup.registerCompostables();
        });
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

    private static void onEntityAttributes(EntityAttributeCreationEvent event) {
        event.put(ModEntities.BUTTERFLY.get(), Butterfly.createAttributes().build());
        event.put(ModEntities.MOON_WISP.get(), MoonWisp.createAttributes().build());
    }

    private static void onSpawnPlacements(SpawnPlacementRegisterEvent event) {
        event.register(ModEntities.BUTTERFLY.get(), SpawnPlacements.Type.NO_RESTRICTIONS, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Butterfly::checkSpawnRules, SpawnPlacementRegisterEvent.Operation.REPLACE);
        event.register(ModEntities.MOON_WISP.get(), SpawnPlacements.Type.NO_RESTRICTIONS, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                MoonWisp::checkSpawnRules, SpawnPlacementRegisterEvent.Operation.REPLACE);
    }
}
