package com.sugarplumstudios.fairygarden.block.grower;

import com.sugarplumstudios.fairygarden.FairyGarden;
import com.sugarplumstudios.fairygarden.worldgen.ModConfiguredFeatures;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.grower.TreeGrower;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/**
 * The tree growers of the Fairy Garden saplings. A sapling next to flowers gets the bee-nest tree one time in ten
 * (TreeGrower's secondary chance), as in 1.20.1.
 */
public final class FairyTreeGrower {
    public static final TreeGrower BLOSSOM = create("blossom", ModConfiguredFeatures.BLOSSOM_TREE, ModConfiguredFeatures.BLOSSOM_TREE_BEES);
    public static final TreeGrower SNOW_BLOSSOM = create("snow_blossom", ModConfiguredFeatures.SNOW_BLOSSOM_TREE, null);
    public static final TreeGrower MOONWOOD = create("moonwood", ModConfiguredFeatures.MOONWOOD_TREE, null);

    private static TreeGrower create(String name, ResourceKey<ConfiguredFeature<?, ?>> tree,
                                     @Nullable ResourceKey<ConfiguredFeature<?, ?>> treeWithBees) {
        return new TreeGrower(FairyGarden.MOD_ID + ":" + name, 0.1F, Optional.empty(), Optional.empty(), Optional.of(tree),
                Optional.empty(), Optional.empty(), Optional.ofNullable(treeWithBees));
    }

    private FairyTreeGrower() {
    }
}
