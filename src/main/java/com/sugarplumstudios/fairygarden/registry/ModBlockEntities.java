package com.sugarplumstudios.fairygarden.registry;

import com.sugarplumstudios.fairygarden.FairyGarden;
import com.sugarplumstudios.fairygarden.block.sign.ModHangingSignBlockEntity;
import com.sugarplumstudios.fairygarden.block.sign.ModSignBlockEntity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, FairyGarden.MOD_ID);

    @SuppressWarnings("DataFlowIssue")
    public static final RegistryObject<BlockEntityType<ModSignBlockEntity>> SIGN = BLOCK_ENTITIES.register("sign",
            () -> BlockEntityType.Builder.of(ModSignBlockEntity::new, ModBlocks.WOOD_SETS.stream()
                    .flatMap(set -> java.util.stream.Stream.<Block>of(set.sign.get(), set.wallSign.get()))
                    .toArray(Block[]::new)).build(null));

    @SuppressWarnings("DataFlowIssue")
    public static final RegistryObject<BlockEntityType<ModHangingSignBlockEntity>> HANGING_SIGN = BLOCK_ENTITIES.register("hanging_sign",
            () -> BlockEntityType.Builder.of(ModHangingSignBlockEntity::new, ModBlocks.WOOD_SETS.stream()
                    .flatMap(set -> java.util.stream.Stream.<Block>of(set.hangingSign.get(), set.wallHangingSign.get()))
                    .toArray(Block[]::new)).build(null));

    private ModBlockEntities() {
    }
}
