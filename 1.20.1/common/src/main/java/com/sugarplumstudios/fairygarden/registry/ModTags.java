package com.sugarplumstudios.fairygarden.registry;

import com.sugarplumstudios.fairygarden.FairyGarden;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;

public final class ModTags {
    public static final class Blocks {
        public static final TagKey<Block> BLOSSOM_LOGS = TagKey.create(Registries.BLOCK, FairyGarden.id("blossom_logs"));
        public static final TagKey<Block> MOONWOOD_LOGS = TagKey.create(Registries.BLOCK, FairyGarden.id("moonwood_logs"));
        /** Ground our small fairy features (rings, ponds, crystals) are willing to sit on. */
        public static final TagKey<Block> FAIRY_GROUND = TagKey.create(Registries.BLOCK, FairyGarden.id("fairy_ground"));
    }

    public static final class Items {
        public static final TagKey<Item> BLOSSOM_LOGS = TagKey.create(Registries.ITEM, FairyGarden.id("blossom_logs"));
        public static final TagKey<Item> MOONWOOD_LOGS = TagKey.create(Registries.ITEM, FairyGarden.id("moonwood_logs"));
        /** Every Fairy Garden flower; used by the flower crown recipe and repairs. */
        public static final TagKey<Item> FAIRY_FLOWERS = TagKey.create(Registries.ITEM, FairyGarden.id("fairy_flowers"));
    }

    public static final class Biomes {
        public static final TagKey<Biome> FAIRY_GARDEN = TagKey.create(Registries.BIOME, FairyGarden.id("fairy_garden_biomes"));
        public static final TagKey<Biome> HAS_FLOWER_SHRINE = TagKey.create(Registries.BIOME, FairyGarden.id("has_structure/flower_shrine"));
        public static final TagKey<Biome> HAS_HIDDEN_FAIRY_GARDEN = TagKey.create(Registries.BIOME, FairyGarden.id("has_structure/hidden_fairy_garden"));
        public static final TagKey<Biome> HAS_MOONSTONE_CIRCLE = TagKey.create(Registries.BIOME, FairyGarden.id("has_structure/moonstone_circle"));
        public static final TagKey<Biome> HAS_MUSHROOM_COTTAGE = TagKey.create(Registries.BIOME, FairyGarden.id("has_structure/mushroom_cottage"));
    }

    private ModTags() {
    }
}
