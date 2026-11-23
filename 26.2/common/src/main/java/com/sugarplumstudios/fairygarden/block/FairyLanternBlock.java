package com.sugarplumstudios.fairygarden.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.Supplier;

/** A lantern with a captured fairy light inside; little motes escape through the glass. */
public class FairyLanternBlock extends LanternBlock {
    private final Supplier<? extends ParticleOptions> particle;

    public FairyLanternBlock(Supplier<? extends ParticleOptions> particle, Properties properties) {
        super(properties);
        this.particle = particle;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextInt(3) == 0) {
            double y = pos.getY() + (state.getValue(HANGING) ? 0.35 : 0.3);
            level.addParticle(particle.get(), pos.getX() + 0.3 + random.nextDouble() * 0.4, y,
                    pos.getZ() + 0.3 + random.nextDouble() * 0.4, 0.0, 0.015, 0.0);
        }
    }
}
