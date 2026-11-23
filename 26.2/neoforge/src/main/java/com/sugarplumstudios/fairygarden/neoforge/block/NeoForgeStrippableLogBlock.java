package com.sugarplumstudios.fairygarden.neoforge.block;

import com.sugarplumstudios.fairygarden.block.StrippableLogBlock;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.common.ItemAbility;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

/** NeoForge strips logs through the block's tool-modified state. */
public class NeoForgeStrippableLogBlock extends StrippableLogBlock {
    public NeoForgeStrippableLogBlock(@Nullable Supplier<? extends Block> stripped, Properties properties) {
        super(stripped, properties);
    }

    @Override
    public @Nullable BlockState getToolModifiedState(BlockState state, UseOnContext context, ItemAbility itemAbility, boolean simulate) {
        if (itemAbility == ItemAbilities.AXE_STRIP) {
            BlockState stripped = strippedState(state);
            if (stripped != null) {
                return stripped;
            }
        }
        return super.getToolModifiedState(state, context, itemAbility, simulate);
    }
}
