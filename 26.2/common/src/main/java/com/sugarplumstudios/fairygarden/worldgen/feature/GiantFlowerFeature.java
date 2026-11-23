package com.sugarplumstudios.fairygarden.worldgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;

/**
 * A giant flower: a tall green stem with a couple of leaf pads, a glowing pistil and a cupped ring of petals.
 * Taller flowers get a wider bloom. Everything stays within 4 blocks of the stem.
 */
public class GiantFlowerFeature extends Feature<GiantFlowerConfiguration> {
    public GiantFlowerFeature(Codec<GiantFlowerConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<GiantFlowerConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();
        RandomSource random = context.random();
        GiantFlowerConfiguration config = context.config();

        if (!FeatureHelper.isSoil(level.getBlockState(origin.below()))) {
            return false;
        }
        int height = config.height().sample(random);
        for (int y = 0; y <= height + 1; y++) {
            if (!FeatureHelper.isFree(level, origin.above(y))) {
                return false;
            }
        }

        BlockState petal = config.petals().getState(level, random, origin);
        for (int y = 0; y < height; y++) {
            BlockPos at = origin.above(y);
            level.setBlock(at, config.stem().getState(level, random, at).trySetValue(RotatedPillarBlock.AXIS, Direction.Axis.Y), Block.UPDATE_CLIENTS);
        }

        // Leaf pads sprouting off the stem.
        BlockState leaf = Blocks.AZALEA_LEAVES.defaultBlockState().setValue(LeavesBlock.PERSISTENT, true);
        int pads = 1 + random.nextInt(2);
        for (int i = 0; i < pads; i++) {
            Direction side = Direction.Plane.HORIZONTAL.getRandomDirection(random);
            FeatureHelper.placeIfFree(level, origin.above(1 + random.nextInt(Math.max(1, height - 2))).relative(side), leaf);
        }

        BlockPos top = origin.above(height);
        level.setBlock(top, config.pistil().getState(level, random, top), Block.UPDATE_CLIENTS);

        boolean large = height >= 6;
        int inner = large ? 10 : 5;     // flat disc radius^2
        int outerMin = large ? 11 : 6;  // raised rim radius^2 range
        int outerMax = large ? 17 : 10;
        int reach = large ? 4 : 3;
        for (int dx = -reach; dx <= reach; dx++) {
            for (int dz = -reach; dz <= reach; dz++) {
                int d2 = dx * dx + dz * dz;
                if (d2 == 0) {
                    continue;
                }
                if (d2 <= inner) {
                    FeatureHelper.placeIfFree(level, top.offset(dx, 0, dz), petal);
                } else if (d2 >= outerMin && d2 <= outerMax) {
                    FeatureHelper.placeIfFree(level, top.offset(dx, 1, dz), petal);
                }
            }
        }
        if (large) {
            FeatureHelper.placeIfFree(level, top.above(), config.pistil().getState(level, random, top.above()));
        }
        return true;
    }
}
