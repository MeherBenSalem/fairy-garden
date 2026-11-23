package com.sugarplumstudios.fairygarden.registry;

import com.sugarplumstudios.fairygarden.FairyGarden;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.level.block.SoundType;
import net.minecraftforge.common.util.ForgeSoundType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.ArrayList;
import java.util.List;

public final class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, FairyGarden.MOD_ID);
    public static final List<RegistryObject<SoundEvent>> ALL = new ArrayList<>();

    // Biome ambience
    public static final RegistryObject<SoundEvent> BLOSSOM_VALLEY_ADDITIONS = register("ambient.blossom_valley.additions");
    public static final RegistryObject<SoundEvent> LAVENDER_FIELDS_LOOP = register("ambient.lavender_fields.loop");
    public static final RegistryObject<SoundEvent> LAVENDER_FIELDS_ADDITIONS = register("ambient.lavender_fields.additions");
    public static final RegistryObject<SoundEvent> STRAWBERRY_MEADOW_ADDITIONS = register("ambient.strawberry_meadow.additions");
    public static final RegistryObject<SoundEvent> MOONLIGHT_FOREST_LOOP = register("ambient.moonlight_forest.loop");
    public static final RegistryObject<SoundEvent> MOONLIGHT_FOREST_ADDITIONS = register("ambient.moonlight_forest.additions");
    public static final RegistryObject<SoundEvent> FAIRY_GLADE_LOOP = register("ambient.fairy_glade.loop");
    public static final RegistryObject<SoundEvent> FAIRY_GLADE_ADDITIONS = register("ambient.fairy_glade.additions");
    public static final RegistryObject<SoundEvent> MUSIC_FAIRY_GARDEN = register("music.fairy_garden");

    // Blocks, items & creatures
    public static final RegistryObject<SoundEvent> FAIRY_CRYSTAL_BREAK = register("block.fairy_crystal.break");
    public static final RegistryObject<SoundEvent> FAIRY_CRYSTAL_STEP = register("block.fairy_crystal.step");
    public static final RegistryObject<SoundEvent> FAIRY_CRYSTAL_PLACE = register("block.fairy_crystal.place");
    public static final RegistryObject<SoundEvent> FAIRY_CRYSTAL_HIT = register("block.fairy_crystal.hit");
    public static final RegistryObject<SoundEvent> FAIRY_CRYSTAL_FALL = register("block.fairy_crystal.fall");
    public static final RegistryObject<SoundEvent> FAIRY_CRYSTAL_CHIME = register("block.fairy_crystal.chime");
    public static final RegistryObject<SoundEvent> MOONFLOWER_BLOOM = register("block.moonflower.bloom");
    public static final RegistryObject<SoundEvent> FAIRY_DUST_USE = register("item.fairy_dust.use");
    public static final RegistryObject<SoundEvent> MOON_WISP_AMBIENT = register("entity.moon_wisp.ambient");
    public static final RegistryObject<SoundEvent> MOON_WISP_HURT = register("entity.moon_wisp.hurt");

    public static final SoundType FAIRY_CRYSTAL = new ForgeSoundType(1.0F, 1.0F, FAIRY_CRYSTAL_BREAK, FAIRY_CRYSTAL_STEP,
            FAIRY_CRYSTAL_PLACE, FAIRY_CRYSTAL_HIT, FAIRY_CRYSTAL_FALL);

    private static RegistryObject<SoundEvent> register(String name) {
        RegistryObject<SoundEvent> sound = SOUNDS.register(name, () -> SoundEvent.createVariableRangeEvent(FairyGarden.id(name)));
        ALL.add(sound);
        return sound;
    }

    private ModSounds() {
    }
}
