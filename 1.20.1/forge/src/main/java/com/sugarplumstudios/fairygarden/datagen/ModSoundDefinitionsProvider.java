package com.sugarplumstudios.fairygarden.datagen;

import com.sugarplumstudios.fairygarden.FairyGarden;
import com.sugarplumstudios.fairygarden.registry.ModSounds;
import net.minecraft.data.PackOutput;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.common.data.SoundDefinition;
import net.minecraftforge.common.data.SoundDefinitionsProvider;
import com.sugarplumstudios.fairygarden.platform.RegistryEntry;

/** Maps every Fairy Garden sound event to the synthesised .ogg files in assets/fairygarden/sounds. */
public class ModSoundDefinitionsProvider extends SoundDefinitionsProvider {
    public ModSoundDefinitionsProvider(PackOutput output, ExistingFileHelper files) {
        super(output, FairyGarden.MOD_ID, files);
    }

    @Override
    public void registerSounds() {
        add(ModSounds.BLOSSOM_VALLEY_ADDITIONS, "ambient.birds", variants("ambient/birds", 4, 0.5F));
        add(ModSounds.STRAWBERRY_MEADOW_ADDITIONS, "ambient.birds", variants("ambient/birds", 4, 0.45F));
        add(ModSounds.LAVENDER_FIELDS_LOOP, null, file("ambient/lavender_loop", 0.45F));
        add(ModSounds.LAVENDER_FIELDS_ADDITIONS, "ambient.bees", variants("ambient/bees", 2, 0.35F));
        add(ModSounds.MOONLIGHT_FOREST_LOOP, null, file("ambient/moonlight_loop", 0.5F));
        add(ModSounds.MOONLIGHT_FOREST_ADDITIONS, "ambient.night", variants("ambient/night", 3, 0.5F));
        add(ModSounds.FAIRY_GLADE_LOOP, null, file("ambient/glade_loop", 0.45F));
        add(ModSounds.FAIRY_GLADE_ADDITIONS, "ambient.chimes", variants("ambient/chimes", 4, 0.45F));
        add(ModSounds.MUSIC_FAIRY_GARDEN.get(), SoundDefinition.definition()
                .with(sound(FairyGarden.id("music/fairy_garden")).stream(true).volume(0.7F)));

        add(ModSounds.FAIRY_CRYSTAL_CHIME, "block.fairy_crystal.chime", variants("block/crystal_chime", 3, 1.0F));
        add(ModSounds.FAIRY_CRYSTAL_BREAK, "block.fairy_crystal.break", variants("block/crystal_break", 2, 1.0F));
        add(ModSounds.FAIRY_CRYSTAL_PLACE, "block.fairy_crystal.place", variants("block/crystal_chime", 3, 0.7F));
        add(ModSounds.FAIRY_CRYSTAL_HIT, null, variants("block/crystal_step", 2, 0.5F));
        add(ModSounds.FAIRY_CRYSTAL_STEP, null, variants("block/crystal_step", 2, 0.5F));
        add(ModSounds.FAIRY_CRYSTAL_FALL, null, variants("block/crystal_step", 2, 0.6F));
        add(ModSounds.MOONFLOWER_BLOOM, "block.moonflower.bloom", file("block/moonflower_bloom", 0.8F));
        add(ModSounds.FAIRY_DUST_USE, "item.fairy_dust.use", file("item/fairy_dust", 0.9F));
        add(ModSounds.MOON_WISP_AMBIENT, "entity.moon_wisp.ambient", variants("entity/wisp_ambient", 2, 0.8F));
        add(ModSounds.MOON_WISP_HURT, "entity.moon_wisp.hurt", file("entity/wisp_hurt", 0.9F));
    }

    private void add(RegistryEntry<SoundEvent> event, String subtitle, SoundDefinition.Sound... sounds) {
        SoundDefinition definition = SoundDefinition.definition().with(sounds);
        if (subtitle != null) {
            definition.subtitle("subtitles.fairygarden." + subtitle);
        }
        add(event.get(), definition);
    }

    private static SoundDefinition.Sound file(String path, float volume) {
        return sound(FairyGarden.id(path)).volume(volume);
    }

    private static SoundDefinition.Sound[] variants(String path, int count, float volume) {
        SoundDefinition.Sound[] sounds = new SoundDefinition.Sound[count];
        for (int i = 0; i < count; i++) {
            sounds[i] = file(path + (i + 1), volume);
        }
        return sounds;
    }
}
