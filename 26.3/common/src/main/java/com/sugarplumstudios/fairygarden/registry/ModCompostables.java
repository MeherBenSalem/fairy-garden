package com.sugarplumstudios.fairygarden.registry;

import net.minecraft.world.item.Item;
import net.minecraft.world.level.storage.loot.providers.number.ints.ContextIntProviders;

import java.util.HashMap;
import java.util.Map;

/**
 * Composting chances. Since Minecraft 26.3 the chance is the item's own compostable component (the composter's chance
 * table is gone), so every item built through {@link Keys#itemProps()} gets it here by name. The table matches
 * CommonSetup.registerCompostables in the older workspaces: 0.3 is vanilla's low tier, 0.65 medium, 0.85 medium-high.
 */
public final class ModCompostables {
    private static final Map<String, Float> CHANCES = new HashMap<>();

    static {
        for (String name : new String[]{"pink_flower_carpet", "lavender_flower_carpet", "cream_flower_carpet", "mint_flower_carpet",
                "blue_flower_carpet", "blossom_leaves", "snow_blossom_leaves", "moonwood_leaves", "blossom_sapling",
                "snow_blossom_sapling", "moonwood_sapling", "blush_grass", "moonlit_grass", "blossom_petals", "snow_blossom_petals",
                "strawberry", "starbloom_seeds", "moon_petal"}) {
            CHANCES.put(name, 0.3F);
        }
        for (String name : new String[]{"rosy_daisy", "lavender", "cream_buttercup", "mint_posy", "starflower", "bluebell",
                "moonflower", "crystal_flower", "tall_lavender", "fairy_foxglove", "fairy_mushroom", "glowcap", "pink_hydrangea",
                "blue_hydrangea", "lavender_hydrangea", "glimmer_lily", "moonbell"}) {
            CHANCES.put(name, 0.65F);
        }
        for (String name : new String[]{"fairy_mushroom_block", "glowcap_block", "lavender_bale", "strawberry_shortcake"}) {
            CHANCES.put(name, 0.85F);
        }
    }

    private ModCompostables() {
    }

    /** Adds the compostable component to the properties of item {@code name}, if it composts. */
    public static Item.Properties apply(Item.Properties properties, String name) {
        Float chance = CHANCES.get(name);
        if (chance == null) {
            return properties;
        }
        if (chance <= 0.3F) {
            return properties.compostable(ContextIntProviders.COMPOSTABLE_LOW);
        }
        if (chance <= 0.65F) {
            return properties.compostable(ContextIntProviders.COMPOSTABLE_MEDIUM);
        }
        return properties.compostable(ContextIntProviders.COMPOSTABLE_MEDIUM_HIGH);
    }
}
