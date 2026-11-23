package com.sugarplumstudios.fairygarden.fabric;

import com.sugarplumstudios.fairygarden.fabric.mixin.WoodTypeInvoker;
import com.sugarplumstudios.fairygarden.fabric.mixin.BlockSetTypeInvoker;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import com.sugarplumstudios.fairygarden.FairyGarden;
import com.sugarplumstudios.fairygarden.block.StrippableLogBlock;
import com.sugarplumstudios.fairygarden.platform.IPlatform;
import com.sugarplumstudios.fairygarden.platform.RegistryEntry;
import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab;
import com.sugarplumstudios.fairygarden.registry.Keys;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FlowerPotBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Fabric registers into the vanilla registries directly. The common registry classes reference each other's entries
 * while they are being initialised (blocks register their items, items reference blocks and entity types), so, like
 * Forge's deferred registers, every entry is queued first and created in {@link #registerAll()}: registry by registry
 * in the order below, and in declaration order inside each registry.
 */
public final class FabricPlatform implements IPlatform {
    private static final List<ResourceKey<? extends Registry<?>>> ORDER = List.of(
            Registries.SOUND_EVENT, Registries.PARTICLE_TYPE, Registries.BLOCK, Registries.ENTITY_TYPE, Registries.ITEM,
            Registries.FEATURE, Registries.STRUCTURE_TYPE, Registries.CREATIVE_MODE_TAB);
    private static final Map<ResourceKey<? extends Registry<?>>, List<Pending<?>>> PENDING = new LinkedHashMap<>();
    private static boolean registered;

    private static final class Pending<T> {
        final Registry<? super T> registry;
        final Identifier id;
        final Supplier<T> factory;
        T value;

        Pending(Registry<? super T> registry, Identifier id, Supplier<T> factory) {
            this.registry = registry;
            this.id = id;
            this.factory = factory;
        }

        void register() {
            value = Registry.register(registry, id, factory.get());
        }
    }

    /** Creates and registers every queued entry. Called once by the main entrypoint after {@link FairyGarden#init()}. */
    public static void registerAll() {
        if (registered) {
            return;
        }
        registered = true;
        for (ResourceKey<? extends Registry<?>> key : ORDER) {
            List<Pending<?>> entries = PENDING.remove(key);
            if (entries != null) {
                entries.forEach(Pending::register);
            }
        }
        PENDING.values().forEach(entries -> entries.forEach(Pending::register));
        PENDING.clear();
    }

    @Override
    public String name() {
        return "Fabric";
    }

    @Override
    public Path configDir() {
        return FabricLoader.getInstance().getConfigDir();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <R, T extends R> RegistryEntry<T> register(ResourceKey<? extends Registry<R>> registry, String name, Supplier<T> factory) {
        Registry<R> target = (Registry<R>) BuiltInRegistries.REGISTRY.getValue(registry.identifier());
        if (target == null) {
            throw new IllegalArgumentException("Unknown registry " + registry);
        }
        Identifier id = FairyGarden.id(name);
        Pending<T> pending = new Pending<>(target, id, Keys.building(name, factory));
        if (registered) {
            pending.register();
        } else {
            PENDING.computeIfAbsent(registry, key -> new ArrayList<>()).add(pending);
        }
        return new RegistryEntry<>(id, () -> pending.value);
    }

    @Override
    public BlockSetType registerBlockSetType(BlockSetType type) {
        return BlockSetTypeInvoker.fairygarden$register(type);
    }

    @Override
    public WoodType registerWoodType(WoodType type) {
        return WoodTypeInvoker.fairygarden$register(type);
    }

    @Override
    public CreativeModeTab.Builder creativeTab() {
        // Fabric lists modded tabs in registration order.
        return FabricCreativeModeTab.builder();
    }

    @Override
    public FlowerPotBlock flowerPot(Supplier<? extends Block> plant, BlockBehaviour.Properties properties) {
        // The plant is registered before its pot (declaration order), and the vanilla constructor adds it to the
        // empty flower pot's plant map.
        return new FlowerPotBlock(plant.get(), properties);
    }

    @Override
    public RotatedPillarBlock strippableLog(@Nullable Supplier<? extends Block> stripped, BlockBehaviour.Properties properties) {
        // Axe stripping is registered with Fabric's StrippableBlockRegistry in the main entrypoint.
        return new StrippableLogBlock(stripped, properties);
    }
}
