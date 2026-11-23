package com.sugarplumstudios.fairygarden.registry;

import net.minecraft.core.registries.Registries;
import com.sugarplumstudios.fairygarden.platform.Services;
import com.sugarplumstudios.fairygarden.FairyGarden;
import com.sugarplumstudios.fairygarden.worldgen.feature.CrystalFormationFeature;
import com.sugarplumstudios.fairygarden.worldgen.feature.EnchantedPondFeature;
import com.sugarplumstudios.fairygarden.worldgen.feature.FairyCacheFeature;
import com.sugarplumstudios.fairygarden.worldgen.feature.FairyRingFeature;
import com.sugarplumstudios.fairygarden.worldgen.feature.GiantFlowerConfiguration;
import com.sugarplumstudios.fairygarden.worldgen.feature.GiantFlowerFeature;
import com.sugarplumstudios.fairygarden.worldgen.feature.LavenderFieldFeature;
import com.sugarplumstudios.fairygarden.worldgen.feature.StreamFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.SimpleBlockConfiguration;
import com.sugarplumstudios.fairygarden.platform.RegistryEntry;

public final class ModFeatures {

    public static final RegistryEntry<GiantFlowerFeature> GIANT_FLOWER = Services.PLATFORM.register(Registries.FEATURE, "giant_flower",
            () -> new GiantFlowerFeature(GiantFlowerConfiguration.CODEC));
    public static final RegistryEntry<LavenderFieldFeature> LAVENDER_FIELD = Services.PLATFORM.register(Registries.FEATURE, "lavender_field",
            () -> new LavenderFieldFeature(NoneFeatureConfiguration.CODEC));
    public static final RegistryEntry<CrystalFormationFeature> CRYSTAL_FORMATION = Services.PLATFORM.register(Registries.FEATURE, "crystal_formation",
            () -> new CrystalFormationFeature(NoneFeatureConfiguration.CODEC));
    public static final RegistryEntry<EnchantedPondFeature> ENCHANTED_POND = Services.PLATFORM.register(Registries.FEATURE, "enchanted_pond",
            () -> new EnchantedPondFeature(NoneFeatureConfiguration.CODEC));
    public static final RegistryEntry<StreamFeature> STREAM = Services.PLATFORM.register(Registries.FEATURE, "stream",
            () -> new StreamFeature(NoneFeatureConfiguration.CODEC));
    public static final RegistryEntry<FairyRingFeature> FAIRY_RING = Services.PLATFORM.register(Registries.FEATURE, "fairy_ring",
            () -> new FairyRingFeature(SimpleBlockConfiguration.CODEC));
    public static final RegistryEntry<FairyCacheFeature> FAIRY_CACHE = Services.PLATFORM.register(Registries.FEATURE, "fairy_cache",
            () -> new FairyCacheFeature(NoneFeatureConfiguration.CODEC));

    /** Loads the class, which registers everything above. */
    public static void init() {
    }

    private ModFeatures() {
    }
}
