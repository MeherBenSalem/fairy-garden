package com.sugarplumstudios.fairygarden.registry;

import com.sugarplumstudios.fairygarden.FairyGarden;
import com.sugarplumstudios.fairygarden.platform.Services;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.WoodType;

/** Block-set and wood types for the two Fairy Garden woods. Names are namespaced so sign/boat textures resolve to our assets. */
public final class ModWoodTypes {
    /** Pink blossom wood sounds like cherry: soft and bright. */
    public static final BlockSetType BLOSSOM_SET = Services.PLATFORM.registerBlockSetType(new BlockSetType(FairyGarden.MOD_ID + ":blossom", true, true, true, BlockSetType.PressurePlateSensitivity.EVERYTHING,
            SoundType.CHERRY_WOOD,
            SoundEvents.CHERRY_WOOD_DOOR_CLOSE, SoundEvents.CHERRY_WOOD_DOOR_OPEN,
            SoundEvents.CHERRY_WOOD_TRAPDOOR_CLOSE, SoundEvents.CHERRY_WOOD_TRAPDOOR_OPEN,
            SoundEvents.CHERRY_WOOD_PRESSURE_PLATE_CLICK_OFF, SoundEvents.CHERRY_WOOD_PRESSURE_PLATE_CLICK_ON,
            SoundEvents.CHERRY_WOOD_BUTTON_CLICK_OFF, SoundEvents.CHERRY_WOOD_BUTTON_CLICK_ON));

    public static final WoodType BLOSSOM = Services.PLATFORM.registerWoodType(new WoodType(FairyGarden.MOD_ID + ":blossom", BLOSSOM_SET,
            SoundType.CHERRY_WOOD, SoundType.CHERRY_WOOD_HANGING_SIGN,
            SoundEvents.CHERRY_WOOD_FENCE_GATE_CLOSE, SoundEvents.CHERRY_WOOD_FENCE_GATE_OPEN));

    /** Moonwood uses the classic wood sounds. */
    public static final BlockSetType MOONWOOD_SET = Services.PLATFORM.registerBlockSetType(new BlockSetType(FairyGarden.MOD_ID + ":moonwood"));

    public static final WoodType MOONWOOD = Services.PLATFORM.registerWoodType(new WoodType(FairyGarden.MOD_ID + ":moonwood", MOONWOOD_SET));

    private ModWoodTypes() {
    }
}
