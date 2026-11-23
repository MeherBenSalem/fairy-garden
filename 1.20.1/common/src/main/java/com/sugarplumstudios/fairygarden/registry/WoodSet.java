package com.sugarplumstudios.fairygarden.registry;

import com.sugarplumstudios.fairygarden.platform.Services;
import com.sugarplumstudios.fairygarden.block.sign.ModCeilingHangingSignBlock;
import com.sugarplumstudios.fairygarden.block.sign.ModStandingSignBlock;
import com.sugarplumstudios.fairygarden.block.sign.ModWallHangingSignBlock;
import com.sugarplumstudios.fairygarden.block.sign.ModWallSignBlock;
import com.sugarplumstudios.fairygarden.entity.BoatWood;
import com.sugarplumstudios.fairygarden.item.ModBoatItem;
import net.minecraft.core.Direction;
import net.minecraft.world.item.DoubleHighBlockItem;
import net.minecraft.world.item.HangingSignItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SignItem;
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
 * pressure plate, button and the four sign blocks. Items (including boats) are added in {@link ModItems}.
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
    public final RegistryEntry<ModStandingSignBlock> sign;
    public final RegistryEntry<ModWallSignBlock> wallSign;
    public final RegistryEntry<ModCeilingHangingSignBlock> hangingSign;
    public final RegistryEntry<ModWallHangingSignBlock> wallHangingSign;

    public final RegistryEntry<SignItem> signItem;
    public final RegistryEntry<HangingSignItem> hangingSignItem;
    public final RegistryEntry<ModBoatItem> boat;
    public final RegistryEntry<ModBoatItem> chestBoat;

    WoodSet(String name, WoodType type, BoatWood boatWood, MapColor planksColor, MapColor barkColor, SoundType sound, SoundType hangingSound) {
        this.name = name;
        this.type = type;

        strippedLog = ModBlocks.register("stripped_" + name + "_log", () -> Services.PLATFORM.strippableLog(null, log(planksColor, planksColor, sound)));
        strippedWood = ModBlocks.register("stripped_" + name + "_wood", () -> Services.PLATFORM.strippableLog(null, log(planksColor, planksColor, sound)));
        log = ModBlocks.register(name + "_log", () -> Services.PLATFORM.strippableLog(strippedLog, log(planksColor, barkColor, sound)));
        wood = ModBlocks.register(name + "_wood", () -> Services.PLATFORM.strippableLog(strippedWood, log(barkColor, barkColor, sound)));

        planks = ModBlocks.register(name + "_planks", () -> new Block(BlockBehaviour.Properties.of().mapColor(planksColor)
                .instrument(NoteBlockInstrument.BASS).strength(2.0F, 3.0F).sound(sound).ignitedByLava()));
        stairs = ModBlocks.register(name + "_stairs", () -> new StairBlock(planks.get().defaultBlockState(),
                BlockBehaviour.Properties.copy(planks.get())) { });
        slab = ModBlocks.register(name + "_slab", () -> new SlabBlock(BlockBehaviour.Properties.copy(planks.get())));
        fence = ModBlocks.register(name + "_fence", () -> new FenceBlock(BlockBehaviour.Properties.of().mapColor(planksColor)
                .forceSolidOn().instrument(NoteBlockInstrument.BASS).strength(2.0F, 3.0F).sound(sound).ignitedByLava()));
        fenceGate = ModBlocks.register(name + "_fence_gate", () -> new FenceGateBlock(BlockBehaviour.Properties.of().mapColor(planksColor)
                .forceSolidOn().instrument(NoteBlockInstrument.BASS).strength(2.0F, 3.0F).ignitedByLava(), type));
        door = ModBlocks.register(name + "_door", () -> new DoorBlock(BlockBehaviour.Properties.of().mapColor(planksColor)
                .instrument(NoteBlockInstrument.BASS).strength(3.0F).noOcclusion().ignitedByLava()
                .pushReaction(PushReaction.DESTROY), type.setType()) { }, block -> new DoubleHighBlockItem(block, new Item.Properties()));
        trapdoor = ModBlocks.register(name + "_trapdoor", () -> new TrapDoorBlock(BlockBehaviour.Properties.of().mapColor(planksColor)
                .instrument(NoteBlockInstrument.BASS).strength(3.0F).noOcclusion()
                .isValidSpawn((state, level, pos, entity) -> false).ignitedByLava(), type.setType()) { });
        pressurePlate = ModBlocks.register(name + "_pressure_plate", () -> new PressurePlateBlock(PressurePlateBlock.Sensitivity.EVERYTHING,
                BlockBehaviour.Properties.of().mapColor(planksColor).forceSolidOn().instrument(NoteBlockInstrument.BASS)
                        .noCollission().strength(0.5F).ignitedByLava().pushReaction(PushReaction.DESTROY), type.setType()) { });
        button = ModBlocks.register(name + "_button", () -> new ButtonBlock(BlockBehaviour.Properties.of().noCollission()
                .strength(0.5F).pushReaction(PushReaction.DESTROY), type.setType(), 30, true) { });

        sign = ModBlocks.registerNoItem(name + "_sign", () -> new ModStandingSignBlock(signProps(planksColor), type));
        wallSign = ModBlocks.registerNoItem(name + "_wall_sign", () -> new ModWallSignBlock(signProps(planksColor).dropsLike(sign.get()), type));
        hangingSign = ModBlocks.registerNoItem(name + "_hanging_sign", () -> new ModCeilingHangingSignBlock(signProps(barkColor).sound(hangingSound), type));
        wallHangingSign = ModBlocks.registerNoItem(name + "_wall_hanging_sign", () -> new ModWallHangingSignBlock(
                signProps(barkColor).sound(hangingSound).dropsLike(hangingSign.get()), type));

        signItem = ModItems.registerBlockItem(name + "_sign",
                () -> new SignItem(new Item.Properties().stacksTo(16), sign.get(), wallSign.get()));
        hangingSignItem = ModItems.registerBlockItem(name + "_hanging_sign",
                () -> new HangingSignItem(hangingSign.get(), wallHangingSign.get(), new Item.Properties().stacksTo(16)));
        boat = ModItems.registerBlockItem(name + "_boat", () -> new ModBoatItem(false, boatWood, new Item.Properties().stacksTo(1)));
        chestBoat = ModItems.registerBlockItem(name + "_chest_boat", () -> new ModBoatItem(true, boatWood, new Item.Properties().stacksTo(1)));
    }

    private static BlockBehaviour.Properties log(MapColor top, MapColor side, SoundType sound) {
        return BlockBehaviour.Properties.of()
                .mapColor(state -> state.getValue(RotatedPillarBlock.AXIS) == Direction.Axis.Y ? top : side)
                .instrument(NoteBlockInstrument.BASS).strength(2.0F).sound(sound).ignitedByLava();
    }

    private static BlockBehaviour.Properties signProps(MapColor color) {
        return BlockBehaviour.Properties.of().mapColor(color).forceSolidOn().instrument(NoteBlockInstrument.BASS)
                .noCollission().strength(1.0F).ignitedByLava();
    }
}
