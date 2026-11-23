package com.sugarplumstudios.fairygarden.registry;

import com.sugarplumstudios.fairygarden.FairyGarden;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;

import java.util.function.Supplier;

/**
 * Items, blocks and entity types must know their registry key when they are constructed. Both platforms wrap every
 * registration factory in {@link #building}, so the property helpers here can stamp the key of the entry being
 * built, whenever the loader runs the factory.
 */
public final class Keys {
    private static final ThreadLocal<String> BUILDING = new ThreadLocal<>();

    private Keys() {
    }

    /** Runs {@code factory} with {@code name} as the entry being built. */
    public static <T> Supplier<T> building(String name, Supplier<T> factory) {
        return () -> {
            String outer = BUILDING.get();
            BUILDING.set(name);
            try {
                return factory.get();
            } finally {
                BUILDING.set(outer);
            }
        };
    }

    private static String current() {
        String name = BUILDING.get();
        if (name == null) {
            throw new IllegalStateException("No registry entry is being built");
        }
        return name;
    }

    /** Item properties carrying the key of the item being built. */
    public static Item.Properties itemProps() {
        return new Item.Properties().setId(item(current()));
    }

    /** Block properties carrying the key of the block being built. */
    public static BlockBehaviour.Properties blockProps() {
        return BlockBehaviour.Properties.of().setId(block(current()));
    }

    /** A full copy of another block's properties, re-keyed to the block being built. */
    public static BlockBehaviour.Properties copyOf(Block block) {
        return BlockBehaviour.Properties.ofFullCopy(block).setId(block(current()));
    }

    /** The key of the entity type being built. */
    public static ResourceKey<EntityType<?>> entity() {
        return ResourceKey.create(Registries.ENTITY_TYPE, FairyGarden.id(current()));
    }

    public static ResourceKey<Item> item(String name) {
        return ResourceKey.create(Registries.ITEM, FairyGarden.id(name));
    }

    public static ResourceKey<Block> block(String name) {
        return ResourceKey.create(Registries.BLOCK, FairyGarden.id(name));
    }
}
