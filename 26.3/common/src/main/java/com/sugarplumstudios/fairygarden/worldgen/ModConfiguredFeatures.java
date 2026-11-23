package com.sugarplumstudios.fairygarden.worldgen;

import com.sugarplumstudios.fairygarden.FairyGarden;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.feature.Feature;

/**
 * The features (26.3 folds configured features into Registries.FEATURE) that code grows (saplings and huge mushrooms). Their JSON comes from the 1.20.1 data
 * generators, converted by tools/port_data.py.
 */
public final class ModConfiguredFeatures {
    public static final ResourceKey<Feature> BLOSSOM_TREE = key("blossom_tree");
    public static final ResourceKey<Feature> BLOSSOM_TREE_BEES = key("blossom_tree_bees");
    public static final ResourceKey<Feature> SNOW_BLOSSOM_TREE = key("snow_blossom_tree");
    public static final ResourceKey<Feature> MOONWOOD_TREE = key("moonwood_tree");
    public static final ResourceKey<Feature> HUGE_FAIRY_MUSHROOM = key("huge_fairy_mushroom");
    public static final ResourceKey<Feature> HUGE_GLOWCAP = key("huge_glowcap");

    private static ResourceKey<Feature> key(String name) {
        return ResourceKey.create(Registries.FEATURE, FairyGarden.id(name));
    }

    private ModConfiguredFeatures() {
    }
}
