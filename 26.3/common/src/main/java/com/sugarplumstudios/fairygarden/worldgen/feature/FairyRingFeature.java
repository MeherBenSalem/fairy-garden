package com.sugarplumstudios.fairygarden.worldgen.feature;

import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.chunk.ChunkGenerator;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.MapCodec;
import com.sugarplumstudios.fairygarden.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;

/** A fairy ring: a near-perfect circle of little mushrooms, sometimes with a crystal flower at its heart. */
public record FairyRingFeature(BlockStateProvider toPlace) implements Feature {
    // 26.3: a feature is its own configured instance; "to_place" is the old simple_block configuration's field.
    public static final MapCodec<FairyRingFeature> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            BlockStateProvider.DIRECT_CODEC.fieldOf("to_place").forGetter(FairyRingFeature::toPlace)
    ).apply(instance, FairyRingFeature::new));

    @Override
    public MapCodec<FairyRingFeature> codec() {
        return CODEC;
    }

    @Override
    public boolean place(WorldGenLevel level, ChunkGenerator generator, RandomSource random, BlockPos origin) {
        return placeRing(level, origin, random, toPlace, 2 + random.nextInt(3), true);
    }

    static boolean placeRing(WorldGenLevel level, BlockPos center, RandomSource random, BlockStateProvider toPlace, int radius,
                             boolean heart) {
        int points = 8 + radius * 4;
        int placed = 0;
        for (int i = 0; i < points; i++) {
            if (random.nextFloat() > 0.88F) {
                continue;
            }
            double angle = Math.PI * 2 * i / points + (random.nextDouble() - 0.5) * 0.15;
            int x = center.getX() + (int) Math.round(Math.cos(angle) * radius);
            int z = center.getZ() + (int) Math.round(Math.sin(angle) * radius);
            BlockPos at = FeatureHelper.surface(level, x, z);
            if (Math.abs(at.getY() - center.getY()) > 2 || !FeatureHelper.isSoil(level.getBlockState(at.below()))
                    || !level.isEmptyBlock(at)) {
                continue;
            }
            BlockState mushroom = toPlace.getState(level, random, at);
            if (mushroom.canSurvive(level, at)) {
                level.setBlock(at, mushroom, Block.UPDATE_CLIENTS);
                placed++;
            }
        }
        if (heart && random.nextInt(3) == 0 && level.isEmptyBlock(center) && FeatureHelper.isSoil(level.getBlockState(center.below()))) {
            level.setBlock(center, ModBlocks.CRYSTAL_FLOWER.get().defaultBlockState(), Block.UPDATE_CLIENTS);
        }
        return placed > 0;
    }
}
