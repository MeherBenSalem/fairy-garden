package com.sugarplumstudios.fairygarden.platform;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FlowerPotBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.WoodType;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.function.Supplier;

/** What Fairy Garden needs from the mod loader. One implementation per loader. */
public interface IPlatform {
    String name();

    Path configDir();

    /**
     * Registers an object under fairygarden:name in the given registry. The factory runs inside
     * {@link com.sugarplumstudios.fairygarden.registry.Keys#building}, so it can stamp its own registry key.
     */
    <R, T extends R> RegistryEntry<T> register(ResourceKey<? extends Registry<R>> registry, String name, Supplier<T> factory);

    CreativeModeTab.Builder creativeTab();

    /** Adds a block set type to vanilla's list (doors, trapdoors, buttons and pressure plates read it). */
    BlockSetType registerBlockSetType(BlockSetType type);

    /** Adds a wood type to vanilla's list (signs and hanging signs read it). */
    WoodType registerWoodType(WoodType type);

    /** A potted form of {@code plant}, known to the vanilla flower pot. */
    FlowerPotBlock flowerPot(Supplier<? extends Block> plant, BlockBehaviour.Properties properties);

    /** A log or wood block that an axe strips into {@code stripped} (null for blocks that are already stripped). */
    RotatedPillarBlock strippableLog(@Nullable Supplier<? extends Block> stripped, BlockBehaviour.Properties properties);

    /** Asks the loader whether a crop may grow now ({@code defaultResult} is vanilla's answer). */
    default boolean cropGrowPre(ServerLevel level, BlockPos pos, BlockState state, boolean defaultResult) {
        return defaultResult;
    }

    /** Tells the loader that a crop has grown. */
    default void cropGrowPost(ServerLevel level, BlockPos pos, BlockState state) {
    }
}
