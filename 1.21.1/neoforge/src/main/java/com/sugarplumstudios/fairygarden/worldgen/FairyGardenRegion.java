package com.sugarplumstudios.fairygarden.worldgen;

import com.mojang.datafixers.util.Pair;
import com.sugarplumstudios.fairygarden.FairyGarden;
import com.sugarplumstudios.fairygarden.config.FairyConfig;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.Climate;
import terrablender.api.Region;
import terrablender.api.RegionType;

import java.util.function.Consumer;

/**
 * A TerraBlender overworld region that looks like vanilla except that some biomes are swapped for Fairy Garden
 * ones. Because terrain shape comes from the climate noise rather than the biome, each fairy biome inherits a
 * fitting landform: Blossom Valley gets meadow hills and plateaus, Moonlight Forest the dense dark-forest slots.
 */
public class FairyGardenRegion extends Region {
    public FairyGardenRegion(int weight) {
        super(FairyGarden.id("overworld"), RegionType.OVERWORLD, weight);
    }

    @Override
    public void addBiomes(Registry<Biome> registry, Consumer<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> mapper) {
        addModifiedVanillaOverworldBiomes(mapper, builder -> {
            if (FairyConfig.BLOSSOM_VALLEY.get()) {
                builder.replaceBiome(Biomes.MEADOW, ModBiomes.BLOSSOM_VALLEY);
                builder.replaceBiome(Biomes.CHERRY_GROVE, ModBiomes.BLOSSOM_VALLEY);
                builder.replaceBiome(Biomes.BIRCH_FOREST, ModBiomes.BLOSSOM_VALLEY);
            }
            if (FairyConfig.LAVENDER_FIELDS.get()) {
                builder.replaceBiome(Biomes.SUNFLOWER_PLAINS, ModBiomes.LAVENDER_FIELDS);
                builder.replaceBiome(Biomes.SAVANNA, ModBiomes.LAVENDER_FIELDS);
            }
            if (FairyConfig.STRAWBERRY_MEADOW.get()) {
                builder.replaceBiome(Biomes.PLAINS, ModBiomes.STRAWBERRY_MEADOW);
            }
            if (FairyConfig.MOONLIGHT_FOREST.get()) {
                builder.replaceBiome(Biomes.DARK_FOREST, ModBiomes.MOONLIGHT_FOREST);
            }
            if (FairyConfig.FAIRY_GLADE.get()) {
                builder.replaceBiome(Biomes.FLOWER_FOREST, ModBiomes.FAIRY_GLADE);
                builder.replaceBiome(Biomes.OLD_GROWTH_BIRCH_FOREST, ModBiomes.FAIRY_GLADE);
            }
        });
    }

    public static boolean anyBiomeEnabled() {
        return FairyConfig.BLOSSOM_VALLEY.get() || FairyConfig.LAVENDER_FIELDS.get() || FairyConfig.STRAWBERRY_MEADOW.get()
                || FairyConfig.MOONLIGHT_FOREST.get() || FairyConfig.FAIRY_GLADE.get();
    }
}
