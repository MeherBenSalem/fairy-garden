package com.sugarplumstudios.fairygarden.block;

import net.minecraft.world.level.block.FarmlandBlock;
import net.minecraft.world.level.block.Blocks;
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
    protected IntegerProperty getAgeProperty() {
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
        return level instanceof Level world && !world.isBrightOutside() && world.canSeeSky(pos);
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (isMaxAge(state) || !canGrowNow(level, pos)) {
            return;
        }
        float speed = growthSpeed(this, level, pos);
        int chance = (int) (25.0F / speed) + 1;
        if (Math.floorMod(level.getOverworldClockTime() / 24000L, 8L) == 0) { // full moon
            chance = chance / 2 + 1;
        }
        if (Services.PLATFORM.cropGrowPre(level, pos, state, random.nextInt(chance) == 0)) {
            level.setBlock(pos, getStateForAge(getAge(state) + 1), Block.UPDATE_CLIENTS);
            Services.PLATFORM.cropGrowPost(level, pos, state);
        }
    }

    /**
     * Vanilla's crop growth speed (wet farmland around speeds it up, crowded rows slow it down). A copy, because
     * NeoForge changes the signature of CropBlock#getGrowthSpeed.
     */
    private static float growthSpeed(Block block, BlockGetter level, BlockPos pos) {
        float speed = 1.0F;
        BlockPos below = pos.below();
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                float bonus = 0.0F;
                BlockState soil = level.getBlockState(below.offset(dx, 0, dz));
                if (soil.is(Blocks.FARMLAND)) {
                    bonus = soil.getValue(FarmlandBlock.MOISTURE) > 0 ? 3.0F : 1.0F;
                }
                if (dx != 0 || dz != 0) {
                    bonus /= 4.0F;
                }
                speed += bonus;
            }
        }
        BlockPos north = pos.north();
        BlockPos south = pos.south();
        BlockPos west = pos.west();
        BlockPos east = pos.east();
        boolean rowX = level.getBlockState(west).is(block) || level.getBlockState(east).is(block);
        boolean rowZ = level.getBlockState(north).is(block) || level.getBlockState(south).is(block);
        if (rowX && rowZ) {
            speed /= 2.0F;
        } else if (level.getBlockState(west.north()).is(block) || level.getBlockState(east.north()).is(block)
                || level.getBlockState(east.south()).is(block) || level.getBlockState(west.south()).is(block)) {
            speed /= 2.0F;
        }
        return speed;
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
