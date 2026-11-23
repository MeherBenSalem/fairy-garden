package com.sugarplumstudios.fairygarden.fabric;

import com.sugarplumstudios.fairygarden.fabric.mixin.WoodTypeInvoker;
import com.sugarplumstudios.fairygarden.fabric.mixin.BlockSetTypeInvoker;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.BlockPos;
import java.util.function.BiFunction;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.BlockEntity;
import com.sugarplumstudios.fairygarden.FairyGarden;
import com.sugarplumstudios.fairygarden.block.ShortPlantBlock;
import com.sugarplumstudios.fairygarden.block.StrippableLogBlock;
import com.sugarplumstudios.fairygarden.platform.IPlatform;
import com.sugarplumstudios.fairygarden.platform.RegistryEntry;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FlowerPotBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SoundType;
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
            Registries.BLOCK_ENTITY_TYPE, Registries.FEATURE, Registries.STRUCTURE_TYPE, Registries.CREATIVE_MODE_TAB);
    private static final Map<ResourceKey<? extends Registry<?>>, List<Pending<?>>> PENDING = new LinkedHashMap<>();
    private static boolean registered;

    private static final class Pending<T> {
        final Registry<? super T> registry;
        final ResourceLocation id;
        final Supplier<T> factory;
        T value;

        Pending(Registry<? super T> registry, ResourceLocation id, Supplier<T> factory) {
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
        Registry<R> target = (Registry<R>) BuiltInRegistries.REGISTRY.get(registry.location());
        if (target == null) {
            throw new IllegalArgumentException("Unknown registry " + registry);
        }
        ResourceLocation id = FairyGarden.id(name);
        Pending<T> pending = new Pending<>(target, id, factory);
        if (registered) {
            pending.register();
        } else {
            PENDING.computeIfAbsent(registry, key -> new ArrayList<>()).add(pending);
        }
        return new RegistryEntry<>(id, () -> pending.value);
    }

    @Override
    public <T extends BlockEntity> BlockEntityType<T> blockEntityType(BiFunction<BlockPos, BlockState, T> factory, Block... blocks) {
        return FabricBlockEntityTypeBuilder.create(factory::apply, blocks).build();
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
        return FabricItemGroup.builder();
    }

    @Override
    public Item spawnEgg(Supplier<? extends EntityType<? extends Mob>> type, int background, int highlight, Item.Properties properties) {
        // Entity types are registered before items (see ORDER), so the type exists here.
        return new SpawnEggItem(type.get(), background, highlight, properties);
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

    @Override
    public Block shortPlant(BlockBehaviour.Properties properties) {
        // Shears collect it through the loot table's shears condition.
        return new ShortPlantBlock(properties);
    }

    @Override
    public SoundType soundType(float volume, float pitch, Supplier<SoundEvent> breakSound, Supplier<SoundEvent> stepSound,
                               Supplier<SoundEvent> placeSound, Supplier<SoundEvent> hitSound, Supplier<SoundEvent> fallSound) {
        return new LazySoundType(volume, pitch, breakSound, stepSound, placeSound, hitSound, fallSound);
    }

    /** A sound type whose sounds are looked up when played, as the sound events are registered after it is made. */
    private static final class LazySoundType extends SoundType {
        private final Supplier<SoundEvent> breakSound;
        private final Supplier<SoundEvent> stepSound;
        private final Supplier<SoundEvent> placeSound;
        private final Supplier<SoundEvent> hitSound;
        private final Supplier<SoundEvent> fallSound;

        LazySoundType(float volume, float pitch, Supplier<SoundEvent> breakSound, Supplier<SoundEvent> stepSound,
                      Supplier<SoundEvent> placeSound, Supplier<SoundEvent> hitSound, Supplier<SoundEvent> fallSound) {
            super(volume, pitch, null, null, null, null, null);
            this.breakSound = breakSound;
            this.stepSound = stepSound;
            this.placeSound = placeSound;
            this.hitSound = hitSound;
            this.fallSound = fallSound;
        }

        @Override
        public SoundEvent getBreakSound() {
            return breakSound.get();
        }

        @Override
        public SoundEvent getStepSound() {
            return stepSound.get();
        }

        @Override
        public SoundEvent getPlaceSound() {
            return placeSound.get();
        }

        @Override
        public SoundEvent getHitSound() {
            return hitSound.get();
        }

        @Override
        public SoundEvent getFallSound() {
            return fallSound.get();
        }
    }
}
