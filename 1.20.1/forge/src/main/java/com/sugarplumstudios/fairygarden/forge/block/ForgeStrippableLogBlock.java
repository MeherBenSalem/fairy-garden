package com.sugarplumstudios.fairygarden.forge.block;

import com.sugarplumstudios.fairygarden.block.StrippableLogBlock;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ToolAction;
import net.minecraftforge.common.ToolActions;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

/** Forge strips logs through the block's tool-modified state. */
public class ForgeStrippableLogBlock extends StrippableLogBlock {
    public ForgeStrippableLogBlock(@Nullable Supplier<? extends Block> stripped, Properties properties) {
        super(stripped, properties);
    }

    @Override
    public @Nullable BlockState getToolModifiedState(BlockState state, UseOnContext context, ToolAction toolAction, boolean simulate) {
        if (toolAction == ToolActions.AXE_STRIP) {
            BlockState stripped = strippedState(state);
            if (stripped != null) {
                return stripped;
            }
        }
        return super.getToolModifiedState(state, context, toolAction, simulate);
    }
}
