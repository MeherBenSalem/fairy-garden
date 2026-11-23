package com.sugarplumstudios.fairygarden.block;

import net.minecraft.core.Holder;
import com.sugarplumstudios.fairygarden.registry.ModParticles;
import com.sugarplumstudios.fairygarden.registry.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FlowerBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.Vec3;

import java.util.function.Supplier;

/**
 * Moonflowers keep their petals folded during the day and open one by one after dusk. Open flowers glow,
 * shed moonlight motes and give a moon petal when picked.
 */
public class MoonflowerBlock extends FlowerBlock {
    public static final BooleanProperty OPEN = BooleanProperty.create("open");

    public MoonflowerBlock(Holder<MobEffect> stewEffect, int stewDuration, Properties properties) {
        super(stewEffect, stewDuration, properties);
        registerDefaultState(stateDefinition.any().setValue(OPEN, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(OPEN);
    }

    public static boolean shouldBeOpen(Level level) {
        return level.dimensionType().hasFixedTime() || !level.isBrightOutside();
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(OPEN, shouldBeOpen(context.getLevel()));
    }

    @Override
    public boolean isRandomlyTicking(BlockState state) {
        return true;
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        boolean open = shouldBeOpen(level);
        if (state.getValue(OPEN) != open) {
            level.setBlock(pos, state.setValue(OPEN, open), Block.UPDATE_ALL);
            if (open) {
                level.playSound(null, pos, ModSounds.MOONFLOWER_BLOOM.get(), SoundSource.BLOCKS, 0.35F, 0.9F + random.nextFloat() * 0.3F);
            }
        }
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (state.getValue(OPEN) && random.nextInt(4) == 0) {
            Vec3 offset = state.getOffset(pos);
            level.addParticle(ModParticles.MOON_GLIMMER.get(),
                    pos.getX() + 0.5 + offset.x + (random.nextDouble() - 0.5) * 0.6,
                    pos.getY() + 0.5 + random.nextDouble() * 0.6,
                    pos.getZ() + 0.5 + offset.z + (random.nextDouble() - 0.5) * 0.6,
                    0.0, 0.01, 0.0);
        }
    }
}
