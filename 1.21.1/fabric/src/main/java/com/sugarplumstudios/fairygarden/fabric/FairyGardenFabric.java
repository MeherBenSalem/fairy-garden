package com.sugarplumstudios.fairygarden.fabric;

import com.sugarplumstudios.fairygarden.FairyGarden;
import com.sugarplumstudios.fairygarden.block.StrippableLogBlock;
import com.sugarplumstudios.fairygarden.entity.Butterfly;
import com.sugarplumstudios.fairygarden.entity.MoonWisp;
import com.sugarplumstudios.fairygarden.event.CommonSetup;
import com.sugarplumstudios.fairygarden.fabric.mixin.SpawnPlacementsInvoker;
import com.sugarplumstudios.fairygarden.registry.ModBlocks;
import com.sugarplumstudios.fairygarden.registry.ModEntities;
import com.sugarplumstudios.fairygarden.registry.WoodSet;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.registry.FlammableBlockRegistry;
import net.fabricmc.fabric.api.registry.StrippableBlockRegistry;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.Heightmap;

public final class FairyGardenFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        FairyGarden.init();
        FabricPlatform.registerAll();

        FabricDefaultAttributeRegistry.register(ModEntities.BUTTERFLY.get(), Butterfly.createAttributes());
        FabricDefaultAttributeRegistry.register(ModEntities.MOON_WISP.get(), MoonWisp.createAttributes());
        SpawnPlacementsInvoker.fairygarden$register(ModEntities.BUTTERFLY.get(), SpawnPlacementTypes.NO_RESTRICTIONS,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Butterfly::checkSpawnRules);
        SpawnPlacementsInvoker.fairygarden$register(ModEntities.MOON_WISP.get(), SpawnPlacementTypes.NO_RESTRICTIONS,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, MoonWisp::checkSpawnRules);

        FlammableBlockRegistry flammables = FlammableBlockRegistry.getDefaultInstance();
        CommonSetup.registerFlammability(flammables::add);
        CommonSetup.registerCompostables();
        for (WoodSet set : ModBlocks.WOOD_SETS) {
            for (Block block : new Block[]{set.log.get(), set.wood.get()}) {
                Block stripped = ((StrippableLogBlock) block).stripped();
                if (stripped != null) {
                    StrippableBlockRegistry.register(block, stripped);
                }
            }
        }
        // Flower pots are linked to their plants by the vanilla FlowerPotBlock constructor (see FabricPlatform).
        // The overworld region is registered by FairyGardenTerraBlender once TerraBlender is ready.
    }
}
