package com.sugarplumstudios.fairygarden.block;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

/** A log or wood block that an axe strips into {@code stripped} (null for blocks that are already stripped). */
public class StrippableLogBlock extends RotatedPillarBlock {
    @Nullable
    private final Supplier<? extends Block> stripped;

    public StrippableLogBlock(@Nullable Supplier<? extends Block> stripped, Properties properties) {
        super(properties);
        this.stripped = stripped;
    }

    /** What an axe turns this block into, keeping its axis; null if it is already stripped. */
    public @Nullable BlockState strippedState(BlockState state) {
        return stripped == null ? null : stripped.get().defaultBlockState().setValue(AXIS, state.getValue(AXIS));
    }

    public @Nullable Block stripped() {
        return stripped == null ? null : stripped.get();
    }
}
