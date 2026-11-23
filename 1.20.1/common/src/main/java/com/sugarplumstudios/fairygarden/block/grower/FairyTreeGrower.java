package com.sugarplumstudios.fairygarden.block.grower;

import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.grower.AbstractTreeGrower;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import org.jetbrains.annotations.Nullable;

/** Grows a sapling into one of our configured trees; saplings next to flowers may get a bee nest. */
public class FairyTreeGrower extends AbstractTreeGrower {
    private final ResourceKey<ConfiguredFeature<?, ?>> tree;
    @Nullable
    private final ResourceKey<ConfiguredFeature<?, ?>> treeWithBees;

    public FairyTreeGrower(ResourceKey<ConfiguredFeature<?, ?>> tree, @Nullable ResourceKey<ConfiguredFeature<?, ?>> treeWithBees) {
        this.tree = tree;
        this.treeWithBees = treeWithBees;
    }

    @Override
    protected ResourceKey<ConfiguredFeature<?, ?>> getConfiguredFeature(RandomSource random, boolean hasFlowers) {
        return hasFlowers && treeWithBees != null && random.nextInt(10) == 0 ? treeWithBees : tree;
    }
}
