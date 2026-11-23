package com.sugarplumstudios.fairygarden.block;

import com.sugarplumstudios.fairygarden.registry.ModParticles;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AmethystClusterBlock;
import net.minecraft.world.level.block.state.BlockState;

/** A glowing cluster of fairy crystal points that grows from any face. */
public class FairyCrystalClusterBlock extends AmethystClusterBlock {
    public FairyCrystalClusterBlock(Properties properties) {
        super(7, 3, properties);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextInt(6) == 0) {
            Direction facing = state.getValue(FACING);
            level.addParticle(ModParticles.FAIRY_SPARKLE.get(),
                    pos.getX() + 0.5 + facing.getStepX() * 0.3 + (random.nextDouble() - 0.5) * 0.5,
                    pos.getY() + 0.5 + facing.getStepY() * 0.3 + (random.nextDouble() - 0.5) * 0.5,
                    pos.getZ() + 0.5 + facing.getStepZ() * 0.3 + (random.nextDouble() - 0.5) * 0.5,
                    0.0, 0.01, 0.0);
        }
    }
}
