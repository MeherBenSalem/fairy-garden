package com.sugarplumstudios.fairygarden.fabric;

import com.sugarplumstudios.fairygarden.FairyGarden;
import com.sugarplumstudios.fairygarden.block.LogStripping;
import com.sugarplumstudios.fairygarden.entity.Butterfly;
import com.sugarplumstudios.fairygarden.entity.MoonWisp;
import com.sugarplumstudios.fairygarden.event.CommonSetup;
import com.sugarplumstudios.fairygarden.registry.ModBlocks;
import com.sugarplumstudios.fairygarden.registry.ModEntities;
import com.sugarplumstudios.fairygarden.registry.WoodSet;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.registry.FlammableBlockRegistry;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.level.block.entity.BlockEntityTypes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.Heightmap;

public final class FairyGardenFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        FairyGarden.init();
        FabricPlatform.registerAll();

        FabricDefaultAttributeRegistry.register(ModEntities.BUTTERFLY.get(), Butterfly.createAttributes());
        FabricDefaultAttributeRegistry.register(ModEntities.MOON_WISP.get(), MoonWisp.createAttributes());
        SpawnPlacements.register(ModEntities.BUTTERFLY.get(), SpawnPlacementTypes.NO_RESTRICTIONS,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Butterfly::checkSpawnRules);
        SpawnPlacements.register(ModEntities.MOON_WISP.get(), SpawnPlacementTypes.NO_RESTRICTIONS,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, MoonWisp::checkSpawnRules);

        FlammableBlockRegistry flammables = FlammableBlockRegistry.getDefaultInstance();
        CommonSetup.registerFlammability(flammables::add);
        for (WoodSet set : ModBlocks.WOOD_SETS) {
            // The signs are vanilla sign blocks; the vanilla sign block entity types must accept them.
            BlockEntityTypes.SIGN.addValidBlock(set.sign.get());
            BlockEntityTypes.SIGN.addValidBlock(set.wallSign.get());
            BlockEntityTypes.HANGING_SIGN.addValidBlock(set.hangingSign.get());
            BlockEntityTypes.HANGING_SIGN.addValidBlock(set.wallHangingSign.get());
        }
        // 26.3: stripping is the axe's block transformer; Fabric's strippable registry is gone.
        UseBlockCallback.EVENT.register(LogStripping::onUseBlock);
        // Flower pots are linked to their plants by the vanilla FlowerPotBlock constructor (see FabricPlatform).
        // The overworld region is registered by FairyGardenTerraBlender once TerraBlender is ready.
    }
}
