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
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;

/**
 * An enchanted pond: a still, rounded pool dug into flat ground with a sandy bed, glowing glimmer lilies on the
 * water, a mossy rim planted with mint and crystal flowers, and sometimes a crystal spire rising from the middle.
 */
public class EnchantedPondFeature implements Feature {
    // 26.3: a feature is its own configured instance; this one has no settings.
    public static final MapCodec<EnchantedPondFeature> CODEC = MapCodec.unit(EnchantedPondFeature::new);

    @Override
    public MapCodec<EnchantedPondFeature> codec() {
        return CODEC;
    }

    @Override
    public boolean place(WorldGenLevel level, ChunkGenerator generator, RandomSource random, BlockPos origin) {
        int ground = origin.getY() - 1;
        int rx = 3 + random.nextInt(2);
        int rz = 3 + random.nextInt(2);

        // Ponds only settle on flat, soft ground.
        for (int dx = -rx - 1; dx <= rx + 1; dx += rx + 1) {
            for (int dz = -rz - 1; dz <= rz + 1; dz += rz + 1) {
                BlockPos probe = FeatureHelper.surface(level, origin.getX() + dx, origin.getZ() + dz);
                if (Math.abs(probe.getY() - 1 - ground) > 1 || !FeatureHelper.isSoil(level.getBlockState(probe.below()))) {
                    return false;
                }
            }
        }

        BlockState water = Blocks.WATER.defaultBlockState();
        BlockState rim = Blocks.MOSS_BLOCK.defaultBlockState();
        double wobble = random.nextDouble() * Math.PI * 2;
        for (int dx = -rx - 2; dx <= rx + 2; dx++) {
            for (int dz = -rz - 2; dz <= rz + 2; dz++) {
                double angle = Math.atan2(dz, dx);
                double d = (dx * dx) / (double) (rx * rx) + (dz * dz) / (double) (rz * rz);
                d *= 1.0 + 0.12 * Math.sin(angle * 3 + wobble);
                BlockPos column = new BlockPos(origin.getX() + dx, ground, origin.getZ() + dz);
                if (d <= 1.0) {
                    int depth = d < 0.4 ? 2 : 1;
                    for (int up = 1; up <= 3; up++) {
                        BlockPos above = column.above(up);
                        if (!level.getBlockState(above).isAir() && FeatureHelper.isFree(level, above)) {
                            level.setBlock(above, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
                        }
                    }
                    for (int down = 0; down < depth; down++) {
                        level.setBlock(column.below(down), water, Block.UPDATE_CLIENTS);
                    }
                    BlockPos bed = column.below(depth);
                    level.setBlock(bed, random.nextInt(3) == 0 ? Blocks.CLAY.defaultBlockState() : Blocks.SAND.defaultBlockState(), Block.UPDATE_CLIENTS);
                    if (!level.getBlockState(bed.below()).isSolid()) {
                        level.setBlock(bed.below(), Blocks.DIRT.defaultBlockState(), Block.UPDATE_CLIENTS);
                    }
                    if (d > 0.25 && random.nextInt(9) == 0) {
                        BlockPos surfaceAbove = column.above();
                        if (level.isEmptyBlock(surfaceAbove)) {
                            level.setBlock(surfaceAbove, random.nextInt(3) == 0 ? Blocks.LILY_PAD.defaultBlockState()
                                    : ModBlocks.GLIMMER_LILY.get().defaultBlockState(), Block.UPDATE_CLIENTS);
                        }
                    }
                } else if (d <= 1.7) {
                    BlockState current = level.getBlockState(column);
                    if (current.isAir() || FeatureHelper.isSoil(current)) {
                        level.setBlock(column, rim, Block.UPDATE_CLIENTS);
                    }
                    BlockPos plant = column.above();
                    if (level.isEmptyBlock(plant) || FeatureHelper.isFree(level, plant)) {
                        int roll = random.nextInt(20);
                        BlockState decoration = switch (roll) {
                            case 0, 1, 2 -> ModBlocks.MINT_POSY.get().defaultBlockState();
                            case 3, 4 -> ModBlocks.CRYSTAL_FLOWER.get().defaultBlockState();
                            case 5 -> ModBlocks.FAIRY_CRYSTAL_CLUSTER.get().defaultBlockState()
                                    .setValue(AmethystClusterBlock.FACING, Direction.UP);
                            case 6, 7 -> ModBlocks.MINT_FLOWER_CARPET.get().defaultBlockState();
                            case 8 -> ModBlocks.GLOWCAP.get().defaultBlockState();
                            default -> null;
                        };
                        level.setBlock(plant, decoration != null ? decoration : Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
                    }
                }
            }
        }

        if (random.nextInt(4) == 0) {
            BlockPos spire = new BlockPos(origin.getX(), ground - 1, origin.getZ());
            int height = 3 + random.nextInt(2);
            for (int y = 0; y < height; y++) {
                level.setBlock(spire.above(y), ModBlocks.FAIRY_CRYSTAL_BLOCK.get().defaultBlockState(), Block.UPDATE_CLIENTS);
            }
            level.setBlock(spire.above(height), ModBlocks.FAIRY_CRYSTAL_CLUSTER.get().defaultBlockState()
                    .setValue(AmethystClusterBlock.FACING, Direction.UP), Block.UPDATE_CLIENTS);
        }
        return true;
    }
}
