package com.sugarplumstudios.fairygarden.block;

import com.sugarplumstudios.fairygarden.registry.ModParticles;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.WaterlilyBlock;
import net.minecraft.world.level.block.state.BlockState;

/** A lily pad crowned with a softly glowing bloom. Found on enchanted ponds. */
public class GlimmerLilyBlock extends WaterlilyBlock {
    public GlimmerLilyBlock(Properties properties) {
        super(properties);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextInt(5) == 0) {
            level.addParticle(ModParticles.FAIRY_SPARKLE.get(), pos.getX() + 0.3 + random.nextDouble() * 0.4, pos.getY() + 0.3,
                    pos.getZ() + 0.3 + random.nextDouble() * 0.4, 0.0, 0.02, 0.0);
        }
    }
}
