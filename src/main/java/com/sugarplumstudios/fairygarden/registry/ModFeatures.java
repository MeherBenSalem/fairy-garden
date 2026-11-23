package com.sugarplumstudios.fairygarden.registry;

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
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModFeatures {
    public static final DeferredRegister<Feature<?>> FEATURES = DeferredRegister.create(ForgeRegistries.FEATURES, FairyGarden.MOD_ID);

    public static final RegistryObject<GiantFlowerFeature> GIANT_FLOWER = FEATURES.register("giant_flower",
            () -> new GiantFlowerFeature(GiantFlowerConfiguration.CODEC));
    public static final RegistryObject<LavenderFieldFeature> LAVENDER_FIELD = FEATURES.register("lavender_field",
            () -> new LavenderFieldFeature(NoneFeatureConfiguration.CODEC));
    public static final RegistryObject<CrystalFormationFeature> CRYSTAL_FORMATION = FEATURES.register("crystal_formation",
            () -> new CrystalFormationFeature(NoneFeatureConfiguration.CODEC));
    public static final RegistryObject<EnchantedPondFeature> ENCHANTED_POND = FEATURES.register("enchanted_pond",
            () -> new EnchantedPondFeature(NoneFeatureConfiguration.CODEC));
    public static final RegistryObject<StreamFeature> STREAM = FEATURES.register("stream",
            () -> new StreamFeature(NoneFeatureConfiguration.CODEC));
    public static final RegistryObject<FairyRingFeature> FAIRY_RING = FEATURES.register("fairy_ring",
            () -> new FairyRingFeature(SimpleBlockConfiguration.CODEC));
    public static final RegistryObject<FairyCacheFeature> FAIRY_CACHE = FEATURES.register("fairy_cache",
            () -> new FairyCacheFeature(NoneFeatureConfiguration.CODEC));

    private ModFeatures() {
    }
}
