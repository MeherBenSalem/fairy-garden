package com.sugarplumstudios.fairygarden.worldgen;

import com.sugarplumstudios.fairygarden.FairyGarden;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;

import java.util.List;

/**
 * The five Fairy Garden biomes. Their JSON comes from the 1.20.1 data generators, converted by tools/port_data.py.
 */
public final class ModBiomes {
    public static final ResourceKey<Biome> BLOSSOM_VALLEY = key("blossom_valley");
    public static final ResourceKey<Biome> LAVENDER_FIELDS = key("lavender_fields");
    public static final ResourceKey<Biome> STRAWBERRY_MEADOW = key("strawberry_meadow");
    public static final ResourceKey<Biome> MOONLIGHT_FOREST = key("moonlight_forest");
    public static final ResourceKey<Biome> FAIRY_GLADE = key("fairy_glade");
    public static final List<ResourceKey<Biome>> ALL = List.of(BLOSSOM_VALLEY, LAVENDER_FIELDS, STRAWBERRY_MEADOW, MOONLIGHT_FOREST, FAIRY_GLADE);

    private static ResourceKey<Biome> key(String name) {
        return ResourceKey.create(Registries.BIOME, FairyGarden.id(name));
    }

    private ModBiomes() {
    }
}
