package com.sugarplumstudios.fairygarden.fabric.mixin;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.level.levelgen.Heightmap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/** {@code SpawnPlacements.register} is private; Forge has an event for it, Fabric calls it through this invoker. */
@Mixin(SpawnPlacements.class)
public interface SpawnPlacementsInvoker {
    @Invoker("register")
    static <T extends Mob> void fairygarden$register(EntityType<T> type, SpawnPlacements.Type placement, Heightmap.Types heightmap,
                                                     SpawnPlacements.SpawnPredicate<T> predicate) {
        throw new AssertionError();
    }
}
