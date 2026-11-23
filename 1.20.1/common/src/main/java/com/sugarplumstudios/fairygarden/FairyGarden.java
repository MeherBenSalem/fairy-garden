package com.sugarplumstudios.fairygarden;

import com.mojang.logging.LogUtils;
import com.sugarplumstudios.fairygarden.config.FairyConfig;
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
import org.slf4j.Logger;

/**
 * Fairy Garden, a Sugarplum Studios mod: magical biomes and enchanted nature. Loader-independent entry point: each
 * loader calls {@link #init()} from its own mod constructor or initializer, then hands the registered objects to
 * the game.
 */
public final class FairyGarden {
    public static final String MOD_ID = "fairygarden";
    public static final Logger LOGGER = LogUtils.getLogger();

    private FairyGarden() {
    }

    /** Reads the common config and queues every block, item, entity, particle, sound, feature and tab. */
    public static void init() {
        FairyConfig.loadCommon();
        ModSounds.init();
        ModParticles.init();
        ModBlocks.init();
        ModItems.init();
        ModBlockEntities.init();
        ModEntities.init();
        ModFeatures.init();
        ModStructureTypes.init();
        ModCreativeTabs.init();
    }

    @SuppressWarnings("removal")
    public static ResourceLocation id(String path) {
        return new ResourceLocation(MOD_ID, path);
    }
}
