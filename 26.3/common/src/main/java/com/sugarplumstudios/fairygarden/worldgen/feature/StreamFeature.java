package com.sugarplumstudios.fairygarden.worldgen.feature;

import net.minecraft.world.level.chunk.ChunkGenerator;
import com.mojang.serialization.MapCodec;
import com.sugarplumstudios.fairygarden.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * A small meandering stream. The channel holds one water level from end to end: where the ground rises a
 * little it cuts a shallow gully, where it would have to fall it ends. Every water block is fully enclosed,
 * so the stream sits still and never floods the valley. Banks get bluebells, blush grass and the odd lily pad.
 */
public class StreamFeature implements Feature {
    // 26.3: a feature is its own configured instance; this one has no settings.
    public static final MapCodec<StreamFeature> CODEC = MapCodec.unit(StreamFeature::new);

    @Override
    public MapCodec<StreamFeature> codec() {
        return CODEC;
    }

    @Override
    public boolean place(WorldGenLevel level, ChunkGenerator generator, RandomSource random, BlockPos origin) {
        int waterY = origin.getY() - 1;
        if (!FeatureHelper.isSoil(level.getBlockState(origin.below()))) {
            return false;
        }

        Set<BlockPos> channel = new LinkedHashSet<>();
        double angle = random.nextDouble() * Math.PI * 2;
        double x = origin.getX() + 0.5;
        double z = origin.getZ() + 0.5;
        int length = 12 + random.nextInt(12);
        for (int step = 0; step < length; step++) {
            int bx = Mth.floor(x);
            int bz = Mth.floor(z);
            if (Math.abs(bx - origin.getX()) > 13 || Math.abs(bz - origin.getZ()) > 13) {
                break;
            }
            int top = FeatureHelper.surface(level, bx, bz).getY() - 1;
            if (top < waterY || top > waterY + 2) {
                break;
            }
            channel.add(new BlockPos(bx, waterY, bz));
            if (random.nextInt(3) == 0) {
                // Occasionally widen the channel sideways for a natural, uneven width.
                double side = angle + Math.PI / 2;
                int wx = Mth.floor(x + Math.cos(side));
                int wz = Mth.floor(z + Math.sin(side));
                int wTop = FeatureHelper.surface(level, wx, wz).getY() - 1;
                if (wTop >= waterY && wTop <= waterY + 2) {
                    channel.add(new BlockPos(wx, waterY, wz));
                }
            }
            angle += (random.nextDouble() - 0.5) * 0.7;
            x += Math.cos(angle);
            z += Math.sin(angle);
        }
        if (channel.size() < 6) {
            return false;
        }

        // Make sure the bed and walls can hold water before changing anything.
        for (BlockPos cell : channel) {
            if (!level.getBlockState(cell.below()).isSolid()) {
                return false;
            }
        }

        BlockState water = Blocks.WATER.defaultBlockState();
        for (BlockPos cell : channel) {
            level.setBlock(cell, water, Block.UPDATE_CLIENTS);
            for (int up = 1; up <= 3; up++) {
                BlockPos above = cell.above(up);
                if (!level.getBlockState(above).isAir()) {
                    level.setBlock(above, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
                }
            }
            if (random.nextInt(6) == 0) {
                level.setBlock(cell.below(), random.nextBoolean() ? Blocks.GRAVEL.defaultBlockState() : Blocks.CLAY.defaultBlockState(),
                        Block.UPDATE_CLIENTS);
            }
        }

        for (BlockPos cell : channel) {
            for (Direction side : Direction.Plane.HORIZONTAL) {
                BlockPos bank = cell.relative(side);
                if (channel.contains(bank)) {
                    continue;
                }
                BlockState bankState = level.getBlockState(bank);
                if (!bankState.isSolid()) {
                    level.setBlock(bank, Blocks.GRASS_BLOCK.defaultBlockState(), Block.UPDATE_CLIENTS);
                }
                BlockPos plant = bank.above();
                if (level.isEmptyBlock(plant) && level.getBlockState(bank).is(Blocks.GRASS_BLOCK) && random.nextInt(4) == 0) {
                    BlockState decoration = switch (random.nextInt(4)) {
                        case 0 -> ModBlocks.BLUEBELL.get().defaultBlockState();
                        case 1 -> ModBlocks.STARFLOWER.get().defaultBlockState();
                        default -> ModBlocks.BLUSH_GRASS.get().defaultBlockState();
                    };
                    level.setBlock(plant, decoration, Block.UPDATE_CLIENTS);
                }
            }
            if (random.nextInt(12) == 0) {
                level.setBlock(cell.above(), Blocks.LILY_PAD.defaultBlockState(), Block.UPDATE_CLIENTS);
            }
        }
        return true;
    }
}
