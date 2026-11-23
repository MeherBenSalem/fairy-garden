package com.sugarplumstudios.fairygarden.registry;

import com.sugarplumstudios.fairygarden.platform.Services;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.vehicle.boat.Boat;
import net.minecraft.world.entity.vehicle.boat.ChestBoat;
import net.minecraft.world.item.BoatItem;
import net.minecraft.world.level.block.CeilingHangingSignBlock;
import net.minecraft.world.level.block.StandingSignBlock;
import net.minecraft.world.level.block.WallHangingSignBlock;
import net.minecraft.world.level.block.WallSignBlock;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.Direction;
import net.minecraft.world.item.DoubleHighBlockItem;
import net.minecraft.world.item.HangingSignItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.StandingAndWallBlockItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ButtonBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.PressurePlateBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import com.sugarplumstudios.fairygarden.platform.RegistryEntry;

/**
 * Registers a complete wood family: logs, wood, planks, stairs, slab, fence, gate, door, trapdoor,
 * pressure plate, button, the four sign blocks (vanilla sign and hanging sign block entities; each loader adds the
 * blocks to those block entity types), the items, and the boat and chest boat entity types of this wood.
 */
public final class WoodSet {
    public final String name;
    public final WoodType type;

    public final RegistryEntry<RotatedPillarBlock> strippedLog;
    public final RegistryEntry<RotatedPillarBlock> strippedWood;
    public final RegistryEntry<RotatedPillarBlock> log;
    public final RegistryEntry<RotatedPillarBlock> wood;
    public final RegistryEntry<Block> planks;
    public final RegistryEntry<StairBlock> stairs;
    public final RegistryEntry<SlabBlock> slab;
    public final RegistryEntry<FenceBlock> fence;
    public final RegistryEntry<FenceGateBlock> fenceGate;
    public final RegistryEntry<DoorBlock> door;
    public final RegistryEntry<TrapDoorBlock> trapdoor;
    public final RegistryEntry<PressurePlateBlock> pressurePlate;
    public final RegistryEntry<ButtonBlock> button;
    public final RegistryEntry<StandingSignBlock> sign;
    public final RegistryEntry<WallSignBlock> wallSign;
    public final RegistryEntry<CeilingHangingSignBlock> hangingSign;
    public final RegistryEntry<WallHangingSignBlock> wallHangingSign;

    public final RegistryEntry<StandingAndWallBlockItem> signItem;
    public final RegistryEntry<HangingSignItem> hangingSignItem;
    public final RegistryEntry<BoatItem> boat;
    public final RegistryEntry<BoatItem> chestBoat;
    public final RegistryEntry<EntityType<Boat>> boatEntity;
    public final RegistryEntry<EntityType<ChestBoat>> chestBoatEntity;

    WoodSet(String name, WoodType type, MapColor planksColor, MapColor barkColor, SoundType sound, SoundType hangingSound) {
        this.name = name;
        this.type = type;

        strippedLog = ModBlocks.register("stripped_" + name + "_log", () -> Services.PLATFORM.strippableLog(null, log(planksColor, planksColor, sound)));
        strippedWood = ModBlocks.register("stripped_" + name + "_wood", () -> Services.PLATFORM.strippableLog(null, log(planksColor, planksColor, sound)));
        log = ModBlocks.register(name + "_log", () -> Services.PLATFORM.strippableLog(strippedLog, log(planksColor, barkColor, sound)));
        wood = ModBlocks.register(name + "_wood", () -> Services.PLATFORM.strippableLog(strippedWood, log(barkColor, barkColor, sound)));

        planks = ModBlocks.register(name + "_planks", () -> new Block(Keys.blockProps().mapColor(planksColor)
                .instrument(NoteBlockInstrument.BASS).strength(2.0F, 3.0F).sound(sound).ignitedByLava()));
        stairs = ModBlocks.register(name + "_stairs", () -> new StairBlock(planks.get().defaultBlockState(),
                Keys.copyOf(planks.get())) { });
        slab = ModBlocks.register(name + "_slab", () -> new SlabBlock(Keys.copyOf(planks.get())));
        fence = ModBlocks.register(name + "_fence", () -> new FenceBlock(Keys.blockProps().mapColor(planksColor)
                .forceSolidOn().instrument(NoteBlockInstrument.BASS).strength(2.0F, 3.0F).sound(sound).ignitedByLava()));
        fenceGate = ModBlocks.register(name + "_fence_gate", () -> new FenceGateBlock(type, Keys.blockProps().mapColor(planksColor)
                .forceSolidOn().instrument(NoteBlockInstrument.BASS).strength(2.0F, 3.0F).ignitedByLava()));
        door = ModBlocks.register(name + "_door", () -> new DoorBlock(type.setType(), Keys.blockProps().mapColor(planksColor)
                .instrument(NoteBlockInstrument.BASS).strength(3.0F).noOcclusion().ignitedByLava()
                .pushReaction(PushReaction.POPPED)) { }, block -> new DoubleHighBlockItem(block, Keys.itemProps().useBlockDescriptionPrefix()));
        trapdoor = ModBlocks.register(name + "_trapdoor", () -> new TrapDoorBlock(type.setType(), Keys.blockProps().mapColor(planksColor)
                .instrument(NoteBlockInstrument.BASS).strength(3.0F).noOcclusion()
                .isValidSpawn((state, level, pos, entity) -> false).ignitedByLava()) { });
        pressurePlate = ModBlocks.register(name + "_pressure_plate", () -> new PressurePlateBlock(type.setType(),
                Keys.blockProps().mapColor(planksColor).forceSolidOn().instrument(NoteBlockInstrument.BASS)
                        .noCollision().strength(0.5F).ignitedByLava().pushReaction(PushReaction.POPPED)) { });
        button = ModBlocks.register(name + "_button", () -> new ButtonBlock(type.setType(), 30, Keys.blockProps().noCollision()
                .strength(0.5F).pushReaction(PushReaction.POPPED)) { });

        sign = ModBlocks.registerNoItem(name + "_sign", () -> new StandingSignBlock(type, signProps(planksColor)));
        wallSign = ModBlocks.registerNoItem(name + "_wall_sign", () -> new WallSignBlock(type, signProps(planksColor)
                .overrideLootTable(sign.get().getLootTable()).overrideDescription(sign.get().getDescriptionId())));
        hangingSign = ModBlocks.registerNoItem(name + "_hanging_sign", () -> new CeilingHangingSignBlock(type,
                signProps(barkColor).sound(hangingSound)));
        wallHangingSign = ModBlocks.registerNoItem(name + "_wall_hanging_sign", () -> new WallHangingSignBlock(type,
                signProps(barkColor).sound(hangingSound).overrideLootTable(hangingSign.get().getLootTable())
                        .overrideDescription(hangingSign.get().getDescriptionId())));

        signItem = ModItems.registerBlockItem(name + "_sign",
                () -> new StandingAndWallBlockItem(sign.get(), wallSign.get(), Direction.DOWN,
                        Keys.itemProps().useBlockDescriptionPrefix().stacksTo(16).signText()));
        hangingSignItem = ModItems.registerBlockItem(name + "_hanging_sign",
                () -> new HangingSignItem(hangingSign.get(), wallHangingSign.get(), Keys.itemProps().useBlockDescriptionPrefix().stacksTo(16).signText()));

        // Entity types are registered before items on both loaders; the entity reads its item when it is broken.
        boatEntity = Services.PLATFORM.register(Registries.ENTITY_TYPE, name + "_boat", () -> EntityType.Builder.<Boat>of(
                        (entityType, level) -> new Boat(entityType, level, this::boatItem), MobCategory.MISC)
                .noLootTable().sized(1.375F, 0.5625F).eyeHeight(0.5625F).clientTrackingRange(10).build(Keys.entity()));
        chestBoatEntity = Services.PLATFORM.register(Registries.ENTITY_TYPE, name + "_chest_boat", () -> EntityType.Builder.<ChestBoat>of(
                        (entityType, level) -> new ChestBoat(entityType, level, this::chestBoatItem), MobCategory.MISC)
                .noLootTable().sized(1.375F, 0.5625F).eyeHeight(0.5625F).clientTrackingRange(10).build(Keys.entity()));
        boat = ModItems.registerBlockItem(name + "_boat", () -> new BoatItem(boatEntity.get(), Keys.itemProps().stacksTo(1)));
        chestBoat = ModItems.registerBlockItem(name + "_chest_boat", () -> new BoatItem(chestBoatEntity.get(), Keys.itemProps().stacksTo(1)));
    }

    private static BlockBehaviour.Properties log(MapColor top, MapColor side, SoundType sound) {
        return Keys.blockProps()
                .mapColor(state -> state.getValue(RotatedPillarBlock.AXIS) == Direction.Axis.Y ? top : side)
                .instrument(NoteBlockInstrument.BASS).strength(2.0F).sound(sound).ignitedByLava();
    }

    private static BlockBehaviour.Properties signProps(MapColor color) {
        return Keys.blockProps().mapColor(color).forceSolidOn().instrument(NoteBlockInstrument.BASS)
                .noCollision().strength(1.0F).ignitedByLava();
    }

    // Read through methods: the entity factories run long after the constructor, once the items exist.
    private Item boatItem() {
        return boat.get();
    }

    private Item chestBoatItem() {
        return chestBoat.get();
    }
}
