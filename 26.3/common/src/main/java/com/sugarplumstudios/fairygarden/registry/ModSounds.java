package com.sugarplumstudios.fairygarden.registry;

import net.minecraft.core.registries.Registries;
import com.sugarplumstudios.fairygarden.platform.Services;
import com.sugarplumstudios.fairygarden.FairyGarden;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.level.block.SoundType;
import com.sugarplumstudios.fairygarden.platform.RegistryEntry;

import java.util.ArrayList;
import java.util.List;

public final class ModSounds {
    public static final List<RegistryEntry<SoundEvent>> ALL = new ArrayList<>();

    // Biome ambience
    public static final RegistryEntry<SoundEvent> BLOSSOM_VALLEY_ADDITIONS = register("ambient.blossom_valley.additions");
    public static final RegistryEntry<SoundEvent> LAVENDER_FIELDS_LOOP = register("ambient.lavender_fields.loop");
    public static final RegistryEntry<SoundEvent> LAVENDER_FIELDS_ADDITIONS = register("ambient.lavender_fields.additions");
    public static final RegistryEntry<SoundEvent> STRAWBERRY_MEADOW_ADDITIONS = register("ambient.strawberry_meadow.additions");
    public static final RegistryEntry<SoundEvent> MOONLIGHT_FOREST_LOOP = register("ambient.moonlight_forest.loop");
    public static final RegistryEntry<SoundEvent> MOONLIGHT_FOREST_ADDITIONS = register("ambient.moonlight_forest.additions");
    public static final RegistryEntry<SoundEvent> FAIRY_GLADE_LOOP = register("ambient.fairy_glade.loop");
    public static final RegistryEntry<SoundEvent> FAIRY_GLADE_ADDITIONS = register("ambient.fairy_glade.additions");
    public static final RegistryEntry<SoundEvent> MUSIC_FAIRY_GARDEN = register("music.fairy_garden");

    // Blocks, items & creatures
    public static final RegistryEntry<SoundEvent> FAIRY_CRYSTAL_BREAK = register("block.fairy_crystal.break");
    public static final RegistryEntry<SoundEvent> FAIRY_CRYSTAL_STEP = register("block.fairy_crystal.step");
    public static final RegistryEntry<SoundEvent> FAIRY_CRYSTAL_PLACE = register("block.fairy_crystal.place");
    public static final RegistryEntry<SoundEvent> FAIRY_CRYSTAL_HIT = register("block.fairy_crystal.hit");
    public static final RegistryEntry<SoundEvent> FAIRY_CRYSTAL_FALL = register("block.fairy_crystal.fall");
    public static final RegistryEntry<SoundEvent> FAIRY_CRYSTAL_CHIME = register("block.fairy_crystal.chime");
    public static final RegistryEntry<SoundEvent> MOONFLOWER_BLOOM = register("block.moonflower.bloom");
    public static final RegistryEntry<SoundEvent> FAIRY_DUST_USE = register("item.fairy_dust.use");
    public static final RegistryEntry<SoundEvent> MOON_WISP_AMBIENT = register("entity.moon_wisp.ambient");
    public static final RegistryEntry<SoundEvent> MOON_WISP_HURT = register("entity.moon_wisp.hurt");

    private static SoundType fairyCrystal;

    /**
     * The fairy crystal blocks' sound type. Made on first use, inside the block factories, which both loaders run
     * after the sound events are registered.
     */
    public static SoundType fairyCrystal() {
        if (fairyCrystal == null) {
            fairyCrystal = new SoundType(1.0F, 1.0F, FAIRY_CRYSTAL_BREAK.get(), FAIRY_CRYSTAL_STEP.get(),
                    FAIRY_CRYSTAL_PLACE.get(), FAIRY_CRYSTAL_HIT.get(), FAIRY_CRYSTAL_FALL.get());
        }
        return fairyCrystal;
    }

    private static RegistryEntry<SoundEvent> register(String name) {
        RegistryEntry<SoundEvent> sound = Services.PLATFORM.register(Registries.SOUND_EVENT, name, () -> SoundEvent.createVariableRangeEvent(FairyGarden.id(name)));
        ALL.add(sound);
        return sound;
    }

    /** Loads the class, which registers everything above. */
    public static void init() {
    }

    private ModSounds() {
    }
}
