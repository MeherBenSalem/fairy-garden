package com.sugarplumstudios.fairygarden.worldgen.feature;

import net.minecraft.world.level.chunk.ChunkGenerator;
import com.mojang.serialization.MapCodec;
import com.sugarplumstudios.fairygarden.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.AmethystClusterBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;

/** A little outcrop of pastel fairy-crystal spires rooted in the ground, tipped and studded with glowing clusters. */
public class CrystalFormationFeature implements Feature {
    // 26.3: a feature is its own configured instance; this one has no settings.
    public static final MapCodec<CrystalFormationFeature> CODEC = MapCodec.unit(CrystalFormationFeature::new);

    @Override
    public MapCodec<CrystalFormationFeature> codec() {
        return CODEC;
    }

    @Override
    public boolean place(WorldGenLevel level, ChunkGenerator generator, RandomSource random, BlockPos origin) {
        if (!level.getBlockState(origin.below()).isSolid()) {
            return false;
        }
        BlockState crystal = ModBlocks.FAIRY_CRYSTAL_BLOCK.get().defaultBlockState();
        BlockState cluster = ModBlocks.FAIRY_CRYSTAL_CLUSTER.get().defaultBlockState();

        int spires = 1 + random.nextInt(3);
        for (int i = 0; i < spires; i++) {
            int dx = i == 0 ? 0 : random.nextInt(5) - 2;
            int dz = i == 0 ? 0 : random.nextInt(5) - 2;
            BlockPos base = FeatureHelper.surface(level, origin.getX() + dx, origin.getZ() + dz);
            if (Math.abs(base.getY() - origin.getY()) > 2 || !level.getBlockState(base.below()).isSolid()) {
                continue;
            }
            int height = i == 0 ? 3 + random.nextInt(3) : 1 + random.nextInt(3);
            level.setBlock(base.below(), crystal, Block.UPDATE_CLIENTS);
            for (int y = 0; y < height; y++) {
                BlockPos at = base.above(y);
                if (!FeatureHelper.placeIfFree(level, at, crystal)) {
                    height = y;
                    break;
                }
                if (random.nextInt(4) == 0) {
                    Direction side = Direction.Plane.HORIZONTAL.getRandomDirection(random);
                    FeatureHelper.placeIfFree(level, at.relative(side), cluster.setValue(AmethystClusterBlock.FACING, side));
                }
            }
            if (height > 0) {
                FeatureHelper.placeIfFree(level, base.above(height), cluster.setValue(AmethystClusterBlock.FACING, Direction.UP));
            }
            if (i == 0 && height >= 4) {
                for (Direction side : Direction.Plane.HORIZONTAL) {
                    if (random.nextFloat() < 0.6F) {
                        FeatureHelper.placeIfFree(level, base.relative(side), crystal);
                    }
                }
            }
        }

        int scattered = 2 + random.nextInt(3);
        for (int i = 0; i < scattered; i++) {
            BlockPos at = FeatureHelper.surface(level, origin.getX() + random.nextInt(7) - 3, origin.getZ() + random.nextInt(7) - 3);
            if (level.getBlockState(at.below()).isSolid()) {
                FeatureHelper.placeIfFree(level, at, cluster.setValue(AmethystClusterBlock.FACING, Direction.UP));
            }
        }
        return true;
    }
}
