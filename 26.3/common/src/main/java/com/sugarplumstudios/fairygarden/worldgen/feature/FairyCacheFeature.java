package com.sugarplumstudios.fairygarden.worldgen.feature;

import net.minecraft.world.level.chunk.ChunkGenerator;
import com.mojang.serialization.MapCodec;
import net.minecraft.world.RandomizableContainer;
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
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.feature.stateproviders.SimpleStateProvider;

/**
 * A rare fairy treasure cache: a chest buried two blocks under a crystal flower, which stands in the middle of a
 * small ring of fairy mushrooms. Players who learn to read the ring can dig straight down to it.
 */
public class FairyCacheFeature implements Feature {
    // 26.3: a feature is its own configured instance; this one has no settings.
    public static final MapCodec<FairyCacheFeature> CODEC = MapCodec.unit(FairyCacheFeature::new);

    @Override
    public MapCodec<FairyCacheFeature> codec() {
        return CODEC;
    }

    @Override
    public boolean place(WorldGenLevel level, ChunkGenerator generator, RandomSource random, BlockPos origin) {
        BlockPos top = origin.below();
        BlockPos chestPos = origin.below(2);
        if (!FeatureHelper.isSoil(level.getBlockState(top)) || !level.getBlockState(chestPos).isSolid()
                || !level.getBlockState(chestPos.below()).isSolid() || !level.isEmptyBlock(origin)) {
            return false;
        }
        Direction facing = Direction.Plane.HORIZONTAL.getRandomDirection(random);
        level.setBlock(chestPos, Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, facing), Block.UPDATE_CLIENTS);
        RandomizableContainer.setBlockEntityLootTable(level, random, chestPos, ModLootTables.FAIRY_CACHE);

        BlockStateProvider ring = new SimpleStateProvider(
                (random.nextInt(3) == 0 ? ModBlocks.GLOWCAP.get() : ModBlocks.FAIRY_MUSHROOM.get()).defaultBlockState());
        FairyRingFeature.placeRing(level, origin, random, ring, 2, false);
        level.setBlock(origin, ModBlocks.CRYSTAL_FLOWER.get().defaultBlockState(), Block.UPDATE_CLIENTS);
        return true;
    }
}
