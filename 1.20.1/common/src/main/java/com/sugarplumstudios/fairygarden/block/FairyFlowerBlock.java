package com.sugarplumstudios.fairygarden.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.FlowerBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

/** A small flower that can optionally give off a gentle ambient particle (crystal flowers sparkle, for example). */
public class FairyFlowerBlock extends FlowerBlock {
    @Nullable
    private final Supplier<? extends ParticleOptions> particle;
    private final int chance;

    public FairyFlowerBlock(Supplier<MobEffect> stewEffect, int stewDuration, @Nullable Supplier<? extends ParticleOptions> particle,
                            int chance, Properties properties) {
        super(stewEffect.get(), stewDuration, properties);
        this.particle = particle;
        this.chance = chance;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (particle != null && random.nextInt(chance) == 0) {
            Vec3 offset = state.getOffset(level, pos);
            level.addParticle(particle.get(),
                    pos.getX() + 0.5 + offset.x + (random.nextDouble() - 0.5) * 0.5,
                    pos.getY() + 0.4 + random.nextDouble() * 0.5,
                    pos.getZ() + 0.5 + offset.z + (random.nextDouble() - 0.5) * 0.5,
                    0.0, 0.015, 0.0);
        }
    }
}
