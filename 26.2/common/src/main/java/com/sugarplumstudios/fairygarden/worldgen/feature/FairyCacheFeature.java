package com.sugarplumstudios.fairygarden.worldgen.feature;

import net.minecraft.world.RandomizableContainer;
import com.mojang.serialization.Codec;
import com.sugarplumstudios.fairygarden.registry.ModBlocks;
import com.sugarplumstudios.fairygarden.registry.ModLootTables;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.SimpleBlockConfiguration;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;

/**
 * A rare fairy treasure cache: a chest buried two blocks under a crystal flower, which stands in the middle of a
 * small ring of fairy mushrooms. Players who learn to read the ring can dig straight down to it.
 */
public class FairyCacheFeature extends Feature<NoneFeatureConfiguration> {
    public FairyCacheFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        BlockPos origin = context.origin();
        BlockPos top = origin.below();
        BlockPos chestPos = origin.below(2);
        if (!FeatureHelper.isSoil(level.getBlockState(top)) || !level.getBlockState(chestPos).isSolid()
                || !level.getBlockState(chestPos.below()).isSolid() || !level.isEmptyBlock(origin)) {
            return false;
        }
        Direction facing = Direction.Plane.HORIZONTAL.getRandomDirection(random);
        level.setBlock(chestPos, Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, facing), Block.UPDATE_CLIENTS);
        RandomizableContainer.setBlockEntityLootTable(level, random, chestPos, ModLootTables.FAIRY_CACHE);

        SimpleBlockConfiguration ring = new SimpleBlockConfiguration(BlockStateProvider.simple(
                random.nextInt(3) == 0 ? ModBlocks.GLOWCAP.get() : ModBlocks.FAIRY_MUSHROOM.get()));
        FairyRingFeature.placeRing(level, origin, random, ring, 2, false);
        level.setBlock(origin, ModBlocks.CRYSTAL_FLOWER.get().defaultBlockState(), Block.UPDATE_CLIENTS);
        return true;
    }
}
