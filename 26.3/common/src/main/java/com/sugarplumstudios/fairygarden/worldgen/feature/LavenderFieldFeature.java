package com.sugarplumstudios.fairygarden.worldgen.feature;

import net.minecraft.world.level.chunk.ChunkGenerator;
import com.mojang.serialization.MapCodec;
import com.sugarplumstudios.fairygarden.registry.ModBlocks;
import com.sugarplumstudios.fairygarden.worldgen.ModBiomes;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.synth.PerlinSimplexNoise;
import java.util.List;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;

/**
 * Plants the long, combed rows of a lavender farm across a whole chunk. Rows run north-south on a 4-block
 * rhythm (three planted, one grassy path) so they line up seamlessly from chunk to chunk; a low-frequency
 * noise opens meadow clearings so the field never looks tiled. Runs once per chunk and only writes inside it.
 */
public class LavenderFieldFeature implements Feature {
    // 26.3: a feature is its own configured instance; this one has no settings.
    public static final MapCodec<LavenderFieldFeature> CODEC = MapCodec.unit(LavenderFieldFeature::new);

    /** Same seed and octave as vanilla's former Biome.BIOME_INFO_NOISE, so the clearings stay where they were. */
    private static final PerlinSimplexNoise CLEARING_NOISE =
            new PerlinSimplexNoise(new WorldgenRandom(new LegacyRandomSource(2345L)), List.of(0));

    @Override
    public MapCodec<LavenderFieldFeature> codec() {
        return CODEC;
    }

    @Override
    public boolean place(WorldGenLevel level, ChunkGenerator generator, RandomSource random, BlockPos origin) {
        int minX = (origin.getX() >> 4) << 4;
        int minZ = (origin.getZ() >> 4) << 4;
        BlockState lavender = ModBlocks.LAVENDER.get().defaultBlockState();
        BlockState tallLavender = ModBlocks.TALL_LAVENDER.get().defaultBlockState();
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        boolean placedAny = false;

        for (int x = minX; x <= minX + 15; x++) {
            int row = Math.floorMod(x, 4);
            if (row == 3) {
                continue;
            }
            for (int z = minZ; z <= minZ + 15; z++) {
                double clearing = CLEARING_NOISE.getValue(x / 56.0, z / 56.0, false);
                if (clearing < -0.45 || random.nextFloat() < 0.08F) {
                    continue;
                }
                int y = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z);
                pos.set(x, y - 1, z);
                if (!level.getBlockState(pos).is(Blocks.GRASS_BLOCK)) {
                    continue;
                }
                pos.set(x, y, z);
                if (!level.getBiome(pos).is(ModBiomes.LAVENDER_FIELDS) || !level.isEmptyBlock(pos)) {
                    continue;
                }
                boolean tall = row == 1 && random.nextFloat() < 0.6F && level.isEmptyBlock(pos.above());
                if (tall) {
                    DoublePlantBlock.placeAt(level, tallLavender, pos.immutable(), Block.UPDATE_CLIENTS);
                } else {
                    level.setBlock(pos, lavender, Block.UPDATE_CLIENTS);
                }
                placedAny = true;
            }
        }
        return placedAny;
    }
}
