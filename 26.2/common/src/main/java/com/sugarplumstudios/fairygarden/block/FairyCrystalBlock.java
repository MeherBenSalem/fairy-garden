package com.sugarplumstudios.fairygarden.block;

import com.sugarplumstudios.fairygarden.registry.ModParticles;
import com.sugarplumstudios.fairygarden.registry.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/** Pastel fairy crystal. Rings with a soft chime when struck by a projectile and sparkles occasionally. */
public class FairyCrystalBlock extends Block {
    public FairyCrystalBlock(Properties properties) {
        super(properties);
    }

    @Override
    public void onProjectileHit(Level level, BlockState state, BlockHitResult hit, Projectile projectile) {
        if (!level.isClientSide()) {
            BlockPos pos = hit.getBlockPos();
            level.playSound(null, pos, ModSounds.FAIRY_CRYSTAL_CHIME.get(), SoundSource.BLOCKS, 1.0F,
                    0.5F + level.getRandom().nextFloat() * 1.2F);
        }
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextInt(10) == 0) {
            level.addParticle(ModParticles.FAIRY_SPARKLE.get(), pos.getX() - 0.1 + random.nextDouble() * 1.2,
                    pos.getY() - 0.1 + random.nextDouble() * 1.2, pos.getZ() - 0.1 + random.nextDouble() * 1.2, 0.0, 0.01, 0.0);
        }
    }
}
