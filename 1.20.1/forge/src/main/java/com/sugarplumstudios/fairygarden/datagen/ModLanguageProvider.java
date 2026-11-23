package com.sugarplumstudios.fairygarden.datagen;

import com.sugarplumstudios.fairygarden.FairyGarden;
import com.sugarplumstudios.fairygarden.registry.ModBlocks;
import com.sugarplumstudios.fairygarden.registry.ModEntities;
import com.sugarplumstudios.fairygarden.registry.ModItems;
import net.minecraft.data.PackOutput;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.data.LanguageProvider;
import net.minecraftforge.registries.ForgeRegistries;
import com.sugarplumstudios.fairygarden.platform.RegistryEntry;

import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

public class ModLanguageProvider extends LanguageProvider {
    /** Display names that differ from the title-cased registry id. */
    private static final Map<String, String> OVERRIDES = Map.ofEntries(
            Map.entry("pink_giant_petals", "Pink Giant Petals"),
            Map.entry("glowcap_block", "Glowcap Mushroom Block"),
            Map.entry("fairy_mushroom_block", "Fairy Mushroom Block"),
            Map.entry("moonwood_sign", "Moonwood Sign"),
            Map.entry("lavender_tea", "Lavender Tea"));

    public ModLanguageProvider(PackOutput output) {
        super(output, FairyGarden.MOD_ID, "en_us");
    }

    @Override
    protected void addTranslations() {
        add("itemGroup.fairygarden", "Fairy Garden");

        for (RegistryEntry<? extends Item> item : ModItems.ALL) {
            Item value = item.get();
            String id = item.getId().getPath();
            String name = OVERRIDES.getOrDefault(id, titleCase(id));
            if (value instanceof BlockItem blockItem && ForgeRegistries.BLOCKS.getKey(blockItem.getBlock()).getPath().equals(id)) {
                add(blockItem.getBlock(), name);
            } else {
                add(value, name);
            }
        }
        // Blocks without their own item still need names (shown by e.g. Jade/WAILA and the debug screen).
        add(ModBlocks.STRAWBERRY_BUSH.get(), "Strawberry Bush");
        add(ModBlocks.STARBLOOM.get(), "Starbloom");
        ModBlocks.POTTED.forEach((plant, pot) -> add(pot.get(), "Potted " + titleCase(plant.getId().getPath())));
        add("item.fairygarden.fairy_dust.tooltip", "Sprinkle on plants for a burst of magical growth");

        add(ModEntities.BUTTERFLY.get(), "Butterfly");
        add(ModEntities.MOON_WISP.get(), "Moon Wisp");
        add(ModEntities.BOAT.get(), "Boat");
        add(ModEntities.CHEST_BOAT.get(), "Boat with Chest");

        add("biome.fairygarden.blossom_valley", "Blossom Valley");
        add("biome.fairygarden.lavender_fields", "Lavender Fields");
        add("biome.fairygarden.strawberry_meadow", "Strawberry Meadow");
        add("biome.fairygarden.moonlight_forest", "Moonlight Forest");
        add("biome.fairygarden.fairy_glade", "Fairy Glade");

        // Sound subtitles
        add("subtitles.fairygarden.ambient.birds", "Birds sing");
        add("subtitles.fairygarden.ambient.bees", "Bees hum");
        add("subtitles.fairygarden.ambient.night", "Night creatures chirp");
        add("subtitles.fairygarden.ambient.chimes", "Fairy chimes tinkle");
        add("subtitles.fairygarden.block.fairy_crystal.chime", "Fairy crystal chimes");
        add("subtitles.fairygarden.block.fairy_crystal.break", "Fairy crystal shatters");
        add("subtitles.fairygarden.block.fairy_crystal.place", "Fairy crystal placed");
        add("subtitles.fairygarden.block.moonflower.bloom", "Moonflower blooms");
        add("subtitles.fairygarden.item.fairy_dust.use", "Fairy dust sparkles");
        add("subtitles.fairygarden.entity.moon_wisp.ambient", "Moon wisp hums");
        add("subtitles.fairygarden.entity.moon_wisp.hurt", "Moon wisp flickers");

        // Advancements
        advancement("root", "Fairy Garden", "Pick a flower from one of the fairy biomes");
        advancement("blossom_valley", "Petals on the Breeze", "Wander into a Blossom Valley");
        advancement("lavender_fields", "Fields of Purple", "Walk through the Lavender Fields");
        advancement("strawberry_meadow", "Berry Sweet", "Find a Strawberry Meadow");
        advancement("moonlight_forest", "Under the Silver Moon", "Enter a Moonlight Forest");
        advancement("fairy_glade", "Where Fairies Dance", "Discover a Fairy Glade");
        advancement("all_biomes", "Fairy Garden Explorer", "Visit all five Fairy Garden biomes");
        advancement("flower_collector", "Flower Collector", "Collect every Fairy Garden flower");
        advancement("moon_petal", "Moonlit Harvest", "Gather a moon petal from a moonflower that bloomed at night");
        advancement("fairy_dust", "A Pinch of Magic", "Harvest fairy dust from a ripe starbloom");
        advancement("flower_crown", "Crowned in Blossoms", "Weave yourself a flower crown");
        advancement("hidden_fairy_garden", "Secret Garden", "Find a hidden fairy garden");
        advancement("treasure", "Fairy Treasure", "Discover a Fairy Garden shrine, circle or cottage");
    }

    private void advancement(String id, String title, String description) {
        add("advancements.fairygarden." + id + ".title", title);
        add("advancements.fairygarden." + id + ".description", description);
    }

    static String titleCase(String id) {
        return Arrays.stream(id.split("_"))
                .map(word -> word.equals("with") || word.equals("of") ? word
                        : word.substring(0, 1).toUpperCase(Locale.ROOT) + word.substring(1))
                .collect(Collectors.joining(" "));
    }
}
