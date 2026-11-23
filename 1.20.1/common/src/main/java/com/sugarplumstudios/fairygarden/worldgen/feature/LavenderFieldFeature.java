package com.sugarplumstudios.fairygarden.worldgen.feature;

import com.mojang.serialization.Codec;
import com.sugarplumstudios.fairygarden.registry.ModBlocks;
import com.sugarplumstudios.fairygarden.worldgen.ModBiomes;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

/**
 * Plants the long, combed rows of a lavender farm across a whole chunk. Rows run north-south on a 4-block
 * rhythm (three planted, one grassy path) so they line up seamlessly from chunk to chunk; a low-frequency
 * noise opens meadow clearings so the field never looks tiled. Runs once per chunk and only writes inside it.
 */
public class LavenderFieldFeature extends Feature<NoneFeatureConfiguration> {
    public LavenderFieldFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        ChunkPos chunk = new ChunkPos(context.origin());
        BlockState lavender = ModBlocks.LAVENDER.get().defaultBlockState();
        BlockState tallLavender = ModBlocks.TALL_LAVENDER.get().defaultBlockState();
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        boolean placedAny = false;

        for (int x = chunk.getMinBlockX(); x <= chunk.getMaxBlockX(); x++) {
            int row = Math.floorMod(x, 4);
            if (row == 3) {
                continue;
            }
            for (int z = chunk.getMinBlockZ(); z <= chunk.getMaxBlockZ(); z++) {
                double clearing = Biome.BIOME_INFO_NOISE.getValue(x / 56.0, z / 56.0, false);
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
