package com.sugarplumstudios.fairygarden.fabric.mixin;

import net.minecraft.world.level.block.state.properties.BlockSetType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/** BlockSetType.register is private in vanilla (Forge makes it public). */
@Mixin(BlockSetType.class)
public interface BlockSetTypeInvoker {
    @Invoker("register")
    static BlockSetType fairygarden$register(BlockSetType type) {
        throw new AssertionError();
    }
}
