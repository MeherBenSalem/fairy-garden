package com.sugarplumstudios.fairygarden.block.sign;

import com.sugarplumstudios.fairygarden.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.HangingSignBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/** Hanging-sign block entity bound to Fairy Garden's own type (vanilla's only accepts vanilla hanging signs). */
public class ModHangingSignBlockEntity extends HangingSignBlockEntity {
    public ModHangingSignBlockEntity(BlockPos pos, BlockState state) {
        // Vanilla's constructor fixes the type; getType() below reports Fairy Garden's own.
        super(pos, state);
    }

    @Override
    public BlockEntityType<?> getType() {
        return ModBlockEntities.HANGING_SIGN.get();
    }
}
