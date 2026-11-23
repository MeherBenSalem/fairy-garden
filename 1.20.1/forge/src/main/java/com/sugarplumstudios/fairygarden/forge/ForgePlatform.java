package com.sugarplumstudios.fairygarden.forge;

import java.util.function.BiFunction;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.BlockEntity;
import com.sugarplumstudios.fairygarden.FairyGarden;
import com.sugarplumstudios.fairygarden.forge.block.ForgeShortPlantBlock;
import com.sugarplumstudios.fairygarden.forge.block.ForgeStrippableLogBlock;
import com.sugarplumstudios.fairygarden.platform.IPlatform;
import com.sugarplumstudios.fairygarden.platform.RegistryEntry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FlowerPotBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.common.util.ForgeSoundType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

public final class ForgePlatform implements IPlatform {
    private static final Map<ResourceKey<?>, DeferredRegister<?>> REGISTERS = new LinkedHashMap<>();

    /** Hands every deferred register to the mod event bus. Call after {@link FairyGarden#init()}. */
    public static void registerAll(IEventBus modBus) {
        REGISTERS.values().forEach(register -> register.register(modBus));
    }

    @Override
    public String name() {
        return "Forge";
    }

    @Override
    public Path configDir() {
        return FMLPaths.CONFIGDIR.get();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <R, T extends R> RegistryEntry<T> register(ResourceKey<? extends Registry<R>> registry, String name, Supplier<T> factory) {
        DeferredRegister<R> register = (DeferredRegister<R>) REGISTERS.computeIfAbsent(registry,
                key -> DeferredRegister.create(registry, FairyGarden.MOD_ID));
        RegistryObject<T> object = register.register(name, factory);
        return new RegistryEntry<>(object.getId(), object);
    }

    @Override
    public <T extends BlockEntity> BlockEntityType<T> blockEntityType(BiFunction<BlockPos, BlockState, T> factory, Block... blocks) {
        //noinspection DataFlowIssue
        return BlockEntityType.Builder.of(factory::apply, blocks).build(null);
    }

    @Override
    public BlockSetType registerBlockSetType(BlockSetType type) {
        return BlockSetType.register(type);
    }

    @Override
    public WoodType registerWoodType(WoodType type) {
        return WoodType.register(type);
    }

    @Override
    public CreativeModeTab.Builder creativeTab() {
        return CreativeModeTab.builder();
    }

    @Override
    public Item spawnEgg(Supplier<? extends EntityType<? extends Mob>> type, int background, int highlight, Item.Properties properties) {
        return new ForgeSpawnEggItem(type, background, highlight, properties);
    }

    @Override
    @SuppressWarnings("unchecked")
    public FlowerPotBlock flowerPot(Supplier<? extends Block> plant, BlockBehaviour.Properties properties) {
        return new FlowerPotBlock(() -> (FlowerPotBlock) Blocks.FLOWER_POT, (Supplier<Block>) plant, properties);
    }

    @Override
    public RotatedPillarBlock strippableLog(@Nullable Supplier<? extends Block> stripped, BlockBehaviour.Properties properties) {
        return new ForgeStrippableLogBlock(stripped, properties);
    }

    @Override
    public Block shortPlant(BlockBehaviour.Properties properties) {
        return new ForgeShortPlantBlock(properties);
    }

    @Override
    public SoundType soundType(float volume, float pitch, Supplier<SoundEvent> breakSound, Supplier<SoundEvent> stepSound,
                               Supplier<SoundEvent> placeSound, Supplier<SoundEvent> hitSound, Supplier<SoundEvent> fallSound) {
        return new ForgeSoundType(volume, pitch, breakSound, stepSound, placeSound, hitSound, fallSound);
    }

    @Override
    public boolean cropGrowPre(ServerLevel level, BlockPos pos, BlockState state, boolean defaultResult) {
        return ForgeHooks.onCropsGrowPre(level, pos, state, defaultResult);
    }

    @Override
    public void cropGrowPost(ServerLevel level, BlockPos pos, BlockState state) {
        ForgeHooks.onCropsGrowPost(level, pos, state);
    }
}
