package com.sugarplumstudios.fairygarden.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.util.ParticleUtils;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.Supplier;

/**
 * Leaves that shed ambient particles: blossom leaves let petals drift down from their undersides,
 * moonwood leaves release slow motes of moonlight that float upward.
 */
public class FairyLeavesBlock extends LeavesBlock {
    private final Supplier<? extends ParticleOptions> particle;
    private final int chance;
    private final boolean rising;

    public FairyLeavesBlock(Supplier<? extends ParticleOptions> particle, int chance, boolean rising, Properties properties) {
        super(properties);
        this.particle = particle;
        this.chance = chance;
        this.rising = rising;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        super.animateTick(state, level, pos, random);
        if (random.nextInt(chance) != 0) {
            return;
        }
        if (rising) {
            if (level.getBlockState(pos.above()).isAir()) {
                level.addParticle(particle.get(), pos.getX() + random.nextDouble(), pos.getY() + 1.05, pos.getZ() + random.nextDouble(),
                        0.0, 0.01, 0.0);
            }
            return;
        }
        BlockPos below = pos.below();
        BlockState belowState = level.getBlockState(below);
        if (!isFaceFull(belowState.getCollisionShape(level, below), Direction.UP)) {
            ParticleUtils.spawnParticleBelow(level, pos, random, particle.get());
        }
    }
}
