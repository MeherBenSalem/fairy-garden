package com.sugarplumstudios.fairygarden.fabric.mixin;

import net.minecraft.world.level.block.state.properties.WoodType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/** WoodType.register is private in vanilla (Forge makes it public). */
@Mixin(WoodType.class)
public interface WoodTypeInvoker {
    @Invoker("register")
    static WoodType fairygarden$register(WoodType type) {
        throw new AssertionError();
    }
}
