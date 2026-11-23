package com.sugarplumstudios.fairygarden.neoforge;

import com.sugarplumstudios.fairygarden.FairyGarden;
import com.sugarplumstudios.fairygarden.config.FairyConfig;
import com.sugarplumstudios.fairygarden.entity.Butterfly;
import com.sugarplumstudios.fairygarden.entity.MoonWisp;
import com.sugarplumstudios.fairygarden.block.LogStripping;
import com.sugarplumstudios.fairygarden.event.CommonSetup;
import com.sugarplumstudios.fairygarden.registry.ModBlocks;
import com.sugarplumstudios.fairygarden.registry.WoodSet;
import com.sugarplumstudios.fairygarden.registry.ModEntities;
import com.sugarplumstudios.fairygarden.worldgen.FairyGardenRegion;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FireBlock;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.BlockEntityTypes;
import net.neoforged.neoforge.event.BlockEntityTypeAddBlocksEvent;
import net.minecraft.world.InteractionResult;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;
import terrablender.api.Regions;

import java.lang.reflect.Method;

@Mod(FairyGarden.MOD_ID)
public final class FairyGardenNeoForge {
    public FairyGardenNeoForge(IEventBus modBus) {
        FairyGarden.init();
        NeoForgePlatform.registerAll(modBus);
        modBus.addListener(FairyGardenNeoForge::onCommonSetup);
        modBus.addListener(FairyGardenNeoForge::onEntityAttributes);
        modBus.addListener(FairyGardenNeoForge::onSpawnPlacements);
        modBus.addListener(FairyGardenNeoForge::onBlockEntityTypes);
        NeoForge.EVENT_BUS.addListener(FairyGardenNeoForge::onRightClickBlock);
        if (FMLEnvironment.getDist() == Dist.CLIENT) {
            FairyConfig.loadClient();
            NeoForgeClient.init(modBus);
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
            // NeoForge links the potted plants to the empty pot itself, and composting chances are item components
            // (ModCompostables).
            CommonSetup.registerFlammability(new Flammables()::setFlammable);
        });
    }

    /** {@code FireBlock.setFlammable} is private in vanilla, so it is called reflectively (Mojang names at runtime). */
    private static final class Flammables {
        private final FireBlock fire = (FireBlock) Blocks.FIRE;
        private final Method setFlammable;

        Flammables() {
            try {
                setFlammable = FireBlock.class.getDeclaredMethod("setFlammable", Block.class, int.class, int.class);
                setFlammable.setAccessible(true);
            } catch (NoSuchMethodException e) {
                throw new IllegalStateException("FireBlock.setFlammable is missing", e);
            }
        }

        void setFlammable(Block block, int encouragement, int flammability) {
            try {
                setFlammable.invoke(fire, block, encouragement, flammability);
            } catch (ReflectiveOperationException e) {
                throw new IllegalStateException("Could not make " + block + " flammable", e);
            }
        }
    }

    /** The signs are vanilla sign blocks; the vanilla sign block entity types must accept them. */
    private static void onBlockEntityTypes(BlockEntityTypeAddBlocksEvent event) {
        for (WoodSet set : ModBlocks.WOOD_SETS) {
            event.modify(BlockEntityTypes.SIGN, set.sign.get(), set.wallSign.get());
            event.modify(BlockEntityTypes.HANGING_SIGN, set.hangingSign.get(), set.wallHangingSign.get());
        }
    }

    private static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        // 26.3: stripping is the axe's block transformer; NeoForge's AXE_STRIP tool modification is gone.
        InteractionResult result = LogStripping.onUseBlock(event.getEntity(), event.getLevel(), event.getHand(), event.getHitVec());
        if (result != InteractionResult.PASS) {
            event.setCanceled(true);
            event.setCancellationResult(result);
        }
    }

    private static void onEntityAttributes(EntityAttributeCreationEvent event) {
        event.put(ModEntities.BUTTERFLY.get(), Butterfly.createAttributes().build());
        event.put(ModEntities.MOON_WISP.get(), MoonWisp.createAttributes().build());
    }

    private static void onSpawnPlacements(RegisterSpawnPlacementsEvent event) {
        event.register(ModEntities.BUTTERFLY.get(), SpawnPlacementTypes.NO_RESTRICTIONS, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Butterfly::checkSpawnRules, RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(ModEntities.MOON_WISP.get(), SpawnPlacementTypes.NO_RESTRICTIONS, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                MoonWisp::checkSpawnRules, RegisterSpawnPlacementsEvent.Operation.REPLACE);
    }
}
