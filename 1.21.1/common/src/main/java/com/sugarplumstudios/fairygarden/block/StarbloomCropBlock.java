package com.sugarplumstudios.fairygarden.block;

import com.sugarplumstudios.fairygarden.registry.ModItems;
import com.sugarplumstudios.fairygarden.registry.ModParticles;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import com.sugarplumstudios.fairygarden.platform.Services;

/**
 * Starbloom: a magical crop that only grows at night beneath an open sky (twice as fast under a full moon).
 * Stages: sprout, bud, closed star, and the glowing starbloom, which yields fairy dust.
 */
public class StarbloomCropBlock extends CropBlock {
    public static final IntegerProperty AGE = BlockStateProperties.AGE_3;
    public static final int MAX_AGE = 3;
    private static final VoxelShape[] SHAPES = {
            Block.box(0.0, 0.0, 0.0, 16.0, 4.0, 16.0),
            Block.box(0.0, 0.0, 0.0, 16.0, 7.0, 16.0),
            Block.box(0.0, 0.0, 0.0, 16.0, 11.0, 16.0),
            Block.box(0.0, 0.0, 0.0, 16.0, 14.0, 16.0)
    };

    public StarbloomCropBlock(Properties properties) {
        super(properties);
    }

    @Override
    public IntegerProperty getAgeProperty() {
        return AGE;
    }

    @Override
    public int getMaxAge() {
        return MAX_AGE;
    }

    @Override
    protected ItemLike getBaseSeedId() {
        return ModItems.STARBLOOM_SEEDS.get();
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AGE);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPES[state.getValue(AGE)];
    }

    public static boolean canGrowNow(LevelReader level, BlockPos pos) {
        return level instanceof Level world && !world.isDay() && world.canSeeSky(pos);
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!level.hasChunksAt(pos.offset(-1, -1, -1), pos.offset(1, 1, 1)) || isMaxAge(state) || !canGrowNow(level, pos)) {
            return;
        }
        float speed = getGrowthSpeed(this, level, pos);
        int chance = (int) (25.0F / speed) + 1;
        if (level.getMoonPhase() == 0) {
            chance = chance / 2 + 1;
        }
        if (Services.PLATFORM.cropGrowPre(level, pos, state, random.nextInt(chance) == 0)) {
            level.setBlock(pos, getStateForAge(getAge(state) + 1), Block.UPDATE_CLIENTS);
            Services.PLATFORM.cropGrowPost(level, pos, state);
        }
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        // Unlike ordinary crops starbloom draws on moonlight, not daylight, so it never pops off in the dark.
        return mayPlaceOn(level.getBlockState(pos.below()), level, pos.below());
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        int age = state.getValue(AGE);
        if (age == MAX_AGE && random.nextInt(3) == 0) {
            level.addParticle(ModParticles.FAIRY_SPARKLE.get(),
                    pos.getX() + 0.2 + random.nextDouble() * 0.6, pos.getY() + 0.6 + random.nextDouble() * 0.5,
                    pos.getZ() + 0.2 + random.nextDouble() * 0.6, 0.0, 0.01, 0.0);
        } else if (age == MAX_AGE - 1 && random.nextInt(12) == 0) {
            level.addParticle(ModParticles.FAIRY_SPARKLE.get(),
                    pos.getX() + 0.5, pos.getY() + 0.7, pos.getZ() + 0.5, 0.0, 0.01, 0.0);
        }
    }
}
