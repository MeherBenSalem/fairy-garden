package com.sugarplumstudios.fairygarden.block;

import com.sugarplumstudios.fairygarden.registry.ModParticles;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/** Glowing bell-shaped blossoms that hang beneath moonwood leaves (or any ceiling) and drip moonlight. */
public class MoonbellBlock extends Block {
    private static final VoxelShape SHAPE = Block.box(4.0, 4.0, 4.0, 12.0, 16.0, 12.0);

    public MoonbellBlock(Properties properties) {
        super(properties);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        BlockPos above = pos.above();
        BlockState ceiling = level.getBlockState(above);
        return ceiling.is(BlockTags.LEAVES) || ceiling.isFaceSturdy(level, above, Direction.DOWN);
    }

    @Override
    @SuppressWarnings("deprecation")
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighbor, LevelAccessor level, BlockPos pos,
                                  BlockPos neighborPos) {
        return direction == Direction.UP && !canSurvive(state, level, pos) ? Blocks.AIR.defaultBlockState()
                : super.updateShape(state, direction, neighbor, level, pos, neighborPos);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextInt(5) == 0) {
            level.addParticle(ModParticles.MOON_GLIMMER.get(), pos.getX() + 0.35 + random.nextDouble() * 0.3, pos.getY() + 0.2,
                    pos.getZ() + 0.35 + random.nextDouble() * 0.3, 0.0, -0.02, 0.0);
        }
    }
}
