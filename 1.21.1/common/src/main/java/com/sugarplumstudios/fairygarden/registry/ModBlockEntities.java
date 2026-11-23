package com.sugarplumstudios.fairygarden.registry;

import net.minecraft.core.registries.Registries;
import com.sugarplumstudios.fairygarden.platform.Services;
import com.sugarplumstudios.fairygarden.FairyGarden;
import com.sugarplumstudios.fairygarden.block.sign.ModHangingSignBlockEntity;
import com.sugarplumstudios.fairygarden.block.sign.ModSignBlockEntity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import com.sugarplumstudios.fairygarden.platform.RegistryEntry;

public final class ModBlockEntities {

    public static final RegistryEntry<BlockEntityType<ModSignBlockEntity>> SIGN = Services.PLATFORM.register(Registries.BLOCK_ENTITY_TYPE, "sign",
            () -> Services.PLATFORM.blockEntityType(ModSignBlockEntity::new, ModBlocks.WOOD_SETS.stream()
                    .flatMap(set -> java.util.stream.Stream.<Block>of(set.sign.get(), set.wallSign.get()))
                    .toArray(Block[]::new)));

    public static final RegistryEntry<BlockEntityType<ModHangingSignBlockEntity>> HANGING_SIGN = Services.PLATFORM.register(Registries.BLOCK_ENTITY_TYPE, "hanging_sign",
            () -> Services.PLATFORM.blockEntityType(ModHangingSignBlockEntity::new, ModBlocks.WOOD_SETS.stream()
                    .flatMap(set -> java.util.stream.Stream.<Block>of(set.hangingSign.get(), set.wallHangingSign.get()))
                    .toArray(Block[]::new)));

    /** Loads the class, which registers everything above. */
    public static void init() {
    }

    private ModBlockEntities() {
    }
}
