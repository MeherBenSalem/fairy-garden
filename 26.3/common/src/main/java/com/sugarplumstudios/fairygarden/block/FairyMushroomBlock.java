package com.sugarplumstudios.fairygarden.block;

import net.minecraft.world.level.block.BonemealSource;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.VegetationBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.Supplier;

/**
 * Fairy mushrooms and glowcaps. They grow on any soil (no darkness needed) and bone meal can raise them
 * into huge mushrooms.
 */
public class FairyMushroomBlock extends VegetationBlock implements BonemealableBlock {
    private static final VoxelShape SHAPE = Block.box(4.0, 0.0, 4.0, 12.0, 9.0, 12.0);
    private final ResourceKey<Feature> hugeFeature;
    @Nullable
    private final Supplier<? extends ParticleOptions> spores;

    public FairyMushroomBlock(ResourceKey<Feature> hugeFeature, @Nullable Supplier<? extends ParticleOptions> spores,
                              Properties properties) {
        super(properties);
        this.hugeFeature = hugeFeature;
        this.spores = spores;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE.move(state.getOffset(pos).x, 0.0, state.getOffset(pos).z);
    }

    @Override
    public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state, BonemealSource source) {
        return true;
    }

    @Override
    public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state, BonemealSource source) {
        return random.nextFloat() < 0.4F;
    }

    @Override
    public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state, BonemealSource source) {
        Optional<? extends Holder<Feature>> feature = level.registryAccess()
                .lookupOrThrow(Registries.FEATURE).get(hugeFeature);
        if (feature.isEmpty()) {
            return;
        }
        level.removeBlock(pos, false);
        if (!feature.get().value().place(level, level.getChunkSource().getGenerator(), random, pos)) {
            level.setBlock(pos, state, Block.UPDATE_ALL);
        }
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (spores != null && random.nextInt(6) == 0) {
            level.addParticle(spores.get(), pos.getX() + 0.3 + random.nextDouble() * 0.4, pos.getY() + 0.5,
                    pos.getZ() + 0.3 + random.nextDouble() * 0.4, 0.0, 0.012, 0.0);
        }
    }
}
