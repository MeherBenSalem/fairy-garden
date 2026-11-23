package com.sugarplumstudios.fairygarden.worldgen.feature;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;

/** Small shared helpers for Fairy Garden's hand-written features. */
final class FeatureHelper {
    private FeatureHelper() {
    }

    /** Air, plants and other things a feature may overwrite without damaging terrain or trees. */
    static boolean isFree(WorldGenLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return state.isAir() || state.is(BlockTags.REPLACEABLE_BY_TREES) || state.is(BlockTags.SMALL_FLOWERS);
    }

    static boolean placeIfFree(WorldGenLevel level, BlockPos pos, BlockState state) {
        if (isFree(level, pos)) {
            level.setBlock(pos, state, Block.UPDATE_CLIENTS);
            return true;
        }
        return false;
    }

    /** Position of the first air block above the surface at this column. */
    static BlockPos surface(WorldGenLevel level, int x, int z) {
        return new BlockPos(x, level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z), z);
    }

    static boolean isSoil(BlockState state) {
        return state.is(BlockTags.DIRT);
    }
}
