package com.sugarplumstudios.fairygarden.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.Supplier;

/** A plain block that drifts a particle off its surface now and then (giant flower pistils puff pollen). */
public class AmbientParticleBlock extends Block {
    private final Supplier<? extends ParticleOptions> particle;
    private final int chance;

    public AmbientParticleBlock(Supplier<? extends ParticleOptions> particle, int chance, Properties properties) {
        super(properties);
        this.particle = particle;
        this.chance = chance;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextInt(chance) == 0 && level.getBlockState(pos.above()).isAir()) {
            level.addParticle(particle.get(), pos.getX() + random.nextDouble(), pos.getY() + 1.05, pos.getZ() + random.nextDouble(),
                    (random.nextDouble() - 0.5) * 0.02, 0.02, (random.nextDouble() - 0.5) * 0.02);
        }
    }
}
