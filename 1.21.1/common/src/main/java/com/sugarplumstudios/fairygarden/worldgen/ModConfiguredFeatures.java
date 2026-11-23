package com.sugarplumstudios.fairygarden.worldgen;

import com.sugarplumstudios.fairygarden.FairyGarden;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;

/**
 * The configured features that code grows (saplings and huge mushrooms). Their JSON comes from the 1.20.1 data
 * generators, converted by tools/port_data.py.
 */
public final class ModConfiguredFeatures {
    public static final ResourceKey<ConfiguredFeature<?, ?>> BLOSSOM_TREE = key("blossom_tree");
    public static final ResourceKey<ConfiguredFeature<?, ?>> BLOSSOM_TREE_BEES = key("blossom_tree_bees");
    public static final ResourceKey<ConfiguredFeature<?, ?>> SNOW_BLOSSOM_TREE = key("snow_blossom_tree");
    public static final ResourceKey<ConfiguredFeature<?, ?>> MOONWOOD_TREE = key("moonwood_tree");
    public static final ResourceKey<ConfiguredFeature<?, ?>> HUGE_FAIRY_MUSHROOM = key("huge_fairy_mushroom");
    public static final ResourceKey<ConfiguredFeature<?, ?>> HUGE_GLOWCAP = key("huge_glowcap");

    private static ResourceKey<ConfiguredFeature<?, ?>> key(String name) {
        return ResourceKey.create(Registries.CONFIGURED_FEATURE, FairyGarden.id(name));
    }

    private ModConfiguredFeatures() {
    }
}
