package com.sugarplumstudios.fairygarden.registry;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.registries.Registries;
import com.sugarplumstudios.fairygarden.platform.Services;
import com.sugarplumstudios.fairygarden.worldgen.feature.CrystalFormationFeature;
import com.sugarplumstudios.fairygarden.worldgen.feature.EnchantedPondFeature;
import com.sugarplumstudios.fairygarden.worldgen.feature.FairyCacheFeature;
import com.sugarplumstudios.fairygarden.worldgen.feature.FairyRingFeature;
import com.sugarplumstudios.fairygarden.worldgen.feature.GiantFlowerFeature;
import com.sugarplumstudios.fairygarden.worldgen.feature.LavenderFieldFeature;
import com.sugarplumstudios.fairygarden.worldgen.feature.StreamFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import com.sugarplumstudios.fairygarden.platform.RegistryEntry;

public final class ModFeatures {
    // 26.3: a feature type is the codec of a feature, registered in Registries.FEATURE_TYPE.
    public static final RegistryEntry<MapCodec<? extends Feature>> GIANT_FLOWER = type("giant_flower", GiantFlowerFeature.CODEC);
    public static final RegistryEntry<MapCodec<? extends Feature>> LAVENDER_FIELD = type("lavender_field", LavenderFieldFeature.CODEC);
    public static final RegistryEntry<MapCodec<? extends Feature>> CRYSTAL_FORMATION = type("crystal_formation", CrystalFormationFeature.CODEC);
    public static final RegistryEntry<MapCodec<? extends Feature>> ENCHANTED_POND = type("enchanted_pond", EnchantedPondFeature.CODEC);
    public static final RegistryEntry<MapCodec<? extends Feature>> STREAM = type("stream", StreamFeature.CODEC);
    public static final RegistryEntry<MapCodec<? extends Feature>> FAIRY_RING = type("fairy_ring", FairyRingFeature.CODEC);
    public static final RegistryEntry<MapCodec<? extends Feature>> FAIRY_CACHE = type("fairy_cache", FairyCacheFeature.CODEC);

    private static RegistryEntry<MapCodec<? extends Feature>> type(String name, MapCodec<? extends Feature> codec) {
        return Services.PLATFORM.register(Registries.FEATURE_TYPE, name, () -> codec);
    }

    /** Loads the class, which registers everything above. */
    public static void init() {
    }

    private ModFeatures() {
    }
}
