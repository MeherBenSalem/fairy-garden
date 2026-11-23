package com.sugarplumstudios.fairygarden.block.grower;

import com.sugarplumstudios.fairygarden.FairyGarden;
import com.sugarplumstudios.fairygarden.worldgen.ModConfiguredFeatures;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.grower.TreeGrower;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.util.random.WeightedList;
import org.jetbrains.annotations.Nullable;

/**
 * The tree growers of the Fairy Garden saplings. A sapling next to flowers gets the bee-nest tree one time in ten
 * (as in 1.20.1).
 */
public final class FairyTreeGrower {
    public static final TreeGrower BLOSSOM = create("blossom", ModConfiguredFeatures.BLOSSOM_TREE, ModConfiguredFeatures.BLOSSOM_TREE_BEES);
    public static final TreeGrower SNOW_BLOSSOM = create("snow_blossom", ModConfiguredFeatures.SNOW_BLOSSOM_TREE, null);
    public static final TreeGrower MOONWOOD = create("moonwood", ModConfiguredFeatures.MOONWOOD_TREE, null);

    private static TreeGrower create(String name, ResourceKey<Feature> tree,
                                     @Nullable ResourceKey<Feature> treeWithBees) {
        // Near flowers, the bee-nest tree one time in ten (the old secondary chance of 0.1).
        WeightedList<ResourceKey<Feature>> nearFlowers = treeWithBees == null ? WeightedList.of()
                : WeightedList.<ResourceKey<Feature>>builder().add(tree, 9).add(treeWithBees, 1).build();
        return new TreeGrower(FairyGarden.MOD_ID + ":" + name, WeightedList.of(tree), WeightedList.of(), nearFlowers, tree);
    }

    private FairyTreeGrower() {
    }
}
