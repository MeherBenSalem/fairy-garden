package com.sugarplumstudios.fairygarden.platform;

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
import net.minecraft.world.level.block.FlowerPotBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.function.Supplier;

/** What Fairy Garden needs from the mod loader. One implementation per loader. */
public interface IPlatform {
    String name();

    Path configDir();

    /** Registers an object under fairygarden:name in the given registry. */
    <R, T extends R> RegistryEntry<T> register(ResourceKey<? extends Registry<R>> registry, String name, Supplier<T> factory);

    CreativeModeTab.Builder creativeTab();

    /** A spawn egg for an entity type that may not be registered yet. */
    Item spawnEgg(Supplier<? extends EntityType<? extends Mob>> type, int background, int highlight, Item.Properties properties);

    /** A potted form of {@code plant}, known to the vanilla flower pot. */
    FlowerPotBlock flowerPot(Supplier<? extends Block> plant, BlockBehaviour.Properties properties);

    /** A log or wood block that an axe strips into {@code stripped} (null for blocks that are already stripped). */
    RotatedPillarBlock strippableLog(@Nullable Supplier<? extends Block> stripped, BlockBehaviour.Properties properties);

    /** Short ground plants that shears collect. */
    Block shortPlant(BlockBehaviour.Properties properties);

    /** A sound type whose sounds are registered by this mod (Forge reads them lazily). */
    SoundType soundType(float volume, float pitch, Supplier<SoundEvent> breakSound, Supplier<SoundEvent> stepSound,
                        Supplier<SoundEvent> placeSound, Supplier<SoundEvent> hitSound, Supplier<SoundEvent> fallSound);

    /** Asks the loader whether a crop may grow now ({@code defaultResult} is vanilla's answer). */
    default boolean cropGrowPre(ServerLevel level, BlockPos pos, BlockState state, boolean defaultResult) {
        return defaultResult;
    }

    /** Tells the loader that a crop has grown. */
    default void cropGrowPost(ServerLevel level, BlockPos pos, BlockState state) {
    }
}
