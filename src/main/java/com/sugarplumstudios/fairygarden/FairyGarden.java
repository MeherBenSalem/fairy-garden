package com.sugarplumstudios.fairygarden;

import com.mojang.logging.LogUtils;
import com.sugarplumstudios.fairygarden.config.FairyConfig;
import com.sugarplumstudios.fairygarden.event.CommonSetup;
import com.sugarplumstudios.fairygarden.registry.ModBlockEntities;
import com.sugarplumstudios.fairygarden.registry.ModBlocks;
import com.sugarplumstudios.fairygarden.registry.ModCreativeTabs;
import com.sugarplumstudios.fairygarden.registry.ModEntities;
import com.sugarplumstudios.fairygarden.registry.ModFeatures;
import com.sugarplumstudios.fairygarden.registry.ModItems;
import com.sugarplumstudios.fairygarden.registry.ModParticles;
import com.sugarplumstudios.fairygarden.registry.ModSounds;
import com.sugarplumstudios.fairygarden.registry.ModStructureTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(FairyGarden.MOD_ID)
public class FairyGarden {
    public static final String MOD_ID = "fairygarden";
    public static final Logger LOGGER = LogUtils.getLogger();

    public FairyGarden() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();

        ModBlocks.BLOCKS.register(modBus);
        ModItems.ITEMS.register(modBus);
        ModBlockEntities.BLOCK_ENTITIES.register(modBus);
        ModEntities.ENTITIES.register(modBus);
        ModParticles.PARTICLES.register(modBus);
        ModSounds.SOUNDS.register(modBus);
        ModFeatures.FEATURES.register(modBus);
        ModStructureTypes.STRUCTURE_TYPES.register(modBus);
        ModCreativeTabs.TABS.register(modBus);

        modBus.addListener(CommonSetup::onCommonSetup);
        modBus.addListener(CommonSetup::onEntityAttributes);
        modBus.addListener(CommonSetup::onSpawnPlacements);

        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, FairyConfig.COMMON_SPEC);
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, FairyConfig.CLIENT_SPEC);
    }

    @SuppressWarnings("removal")
    public static ResourceLocation id(String path) {
        return new ResourceLocation(MOD_ID, path);
    }
}
