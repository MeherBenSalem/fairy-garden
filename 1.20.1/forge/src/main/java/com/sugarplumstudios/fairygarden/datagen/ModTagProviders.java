package com.sugarplumstudios.fairygarden.datagen;

import com.sugarplumstudios.fairygarden.FairyGarden;
import com.sugarplumstudios.fairygarden.registry.ModBlocks;
import com.sugarplumstudios.fairygarden.registry.ModItems;
import com.sugarplumstudios.fairygarden.registry.ModTags;
import com.sugarplumstudios.fairygarden.registry.WoodSet;
import com.sugarplumstudios.fairygarden.worldgen.ModBiomes;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.data.BlockTagsProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import com.sugarplumstudios.fairygarden.platform.RegistryEntry;

import java.util.concurrent.CompletableFuture;

public final class ModTagProviders {
    private ModTagProviders() {
    }

    public static class Blocks extends BlockTagsProvider {
        public Blocks(PackOutput output, CompletableFuture<HolderLookup.Provider> lookup, ExistingFileHelper files) {
            super(output, lookup, FairyGarden.MOD_ID, files);
        }

        @Override
        protected void addTags(HolderLookup.Provider provider) {
            for (WoodSet set : ModBlocks.WOOD_SETS) {
                TagKey<Block> logs = set == ModBlocks.BLOSSOM ? ModTags.Blocks.BLOSSOM_LOGS : ModTags.Blocks.MOONWOOD_LOGS;
                tag(logs).add(set.log.get(), set.wood.get(), set.strippedLog.get(), set.strippedWood.get());
                tag(BlockTags.LOGS_THAT_BURN).addTag(logs);
                tag(BlockTags.PLANKS).add(set.planks.get());
                tag(BlockTags.WOODEN_STAIRS).add(set.stairs.get());
                tag(BlockTags.WOODEN_SLABS).add(set.slab.get());
                tag(BlockTags.WOODEN_FENCES).add(set.fence.get());
                tag(BlockTags.FENCE_GATES).add(set.fenceGate.get());
                tag(Tags.Blocks.FENCE_GATES_WOODEN).add(set.fenceGate.get());
                tag(Tags.Blocks.FENCES_WOODEN).add(set.fence.get());
                tag(BlockTags.WOODEN_DOORS).add(set.door.get());
                tag(BlockTags.WOODEN_TRAPDOORS).add(set.trapdoor.get());
                tag(BlockTags.WOODEN_PRESSURE_PLATES).add(set.pressurePlate.get());
                tag(BlockTags.WOODEN_BUTTONS).add(set.button.get());
                tag(BlockTags.STANDING_SIGNS).add(set.sign.get());
                tag(BlockTags.WALL_SIGNS).add(set.wallSign.get());
                tag(BlockTags.CEILING_HANGING_SIGNS).add(set.hangingSign.get());
                tag(BlockTags.WALL_HANGING_SIGNS).add(set.wallHangingSign.get());
                tag(BlockTags.MINEABLE_WITH_AXE).add(set.log.get(), set.wood.get(), set.strippedLog.get(), set.strippedWood.get(),
                        set.planks.get(), set.stairs.get(), set.slab.get(), set.fence.get(), set.fenceGate.get(), set.door.get(),
                        set.trapdoor.get(), set.pressurePlate.get(), set.button.get(), set.sign.get(), set.wallSign.get(),
                        set.hangingSign.get(), set.wallHangingSign.get());
            }

            Block[] leaves = {ModBlocks.BLOSSOM_LEAVES.get(), ModBlocks.SNOW_BLOSSOM_LEAVES.get(), ModBlocks.MOONWOOD_LEAVES.get()};
            tag(BlockTags.LEAVES).add(leaves);
            tag(BlockTags.MINEABLE_WITH_HOE).add(leaves);
            tag(BlockTags.SAPLINGS).add(ModBlocks.BLOSSOM_SAPLING.get(), ModBlocks.SNOW_BLOSSOM_SAPLING.get(), ModBlocks.MOONWOOD_SAPLING.get());

            ModBlocks.SMALL_FLOWERS.forEach(flower -> tag(BlockTags.SMALL_FLOWERS).add(flower.get()));
            ModBlocks.TALL_FLOWERS.forEach(flower -> tag(BlockTags.TALL_FLOWERS).add(flower.get()));
            tag(BlockTags.FLOWERS).add(ModBlocks.BLOSSOM_PETALS.get(), ModBlocks.SNOW_BLOSSOM_PETALS.get());
            tag(BlockTags.INSIDE_STEP_SOUND_BLOCKS).add(ModBlocks.BLOSSOM_PETALS.get(), ModBlocks.SNOW_BLOSSOM_PETALS.get());
            ModBlocks.POTTED.values().forEach(pot -> tag(BlockTags.FLOWER_POTS).add(pot.get()));
            tag(BlockTags.CROPS).add(ModBlocks.STARBLOOM.get());
            tag(BlockTags.BEE_GROWABLES).add(ModBlocks.STARBLOOM.get(), ModBlocks.STRAWBERRY_BUSH.get());
            tag(BlockTags.REPLACEABLE).add(ModBlocks.BLUSH_GRASS.get(), ModBlocks.MOONLIT_GRASS.get());
            tag(BlockTags.REPLACEABLE_BY_TREES).add(ModBlocks.BLUSH_GRASS.get(), ModBlocks.MOONLIT_GRASS.get(),
                    ModBlocks.BLOSSOM_PETALS.get(), ModBlocks.SNOW_BLOSSOM_PETALS.get());
            ModBlocks.FLOWER_CARPETS.forEach(carpet -> tag(BlockTags.REPLACEABLE_BY_TREES).add(carpet.get()));
            tag(BlockTags.SWORD_EFFICIENT).add(ModBlocks.STRAWBERRY_BUSH.get(), ModBlocks.PINK_HYDRANGEA.get(),
                    ModBlocks.BLUE_HYDRANGEA.get(), ModBlocks.LAVENDER_HYDRANGEA.get());

            tag(BlockTags.MINEABLE_WITH_HOE).add(ModBlocks.FAIRY_MUSHROOM_BLOCK.get(), ModBlocks.GLOWCAP_BLOCK.get(),
                    ModBlocks.LAVENDER_BALE.get(), ModBlocks.GIANT_FLOWER_PISTIL.get(), ModBlocks.PINK_HYDRANGEA.get(),
                    ModBlocks.BLUE_HYDRANGEA.get(), ModBlocks.LAVENDER_HYDRANGEA.get());
            ModBlocks.GIANT_PETALS.forEach(petals -> tag(BlockTags.MINEABLE_WITH_HOE).add(petals.get()));
            tag(BlockTags.MINEABLE_WITH_AXE).add(ModBlocks.GIANT_FLOWER_STEM.get(), ModBlocks.FAIRY_MUSHROOM_BLOCK.get(),
                    ModBlocks.GLOWCAP_BLOCK.get());
            tag(BlockTags.MINEABLE_WITH_PICKAXE).add(ModBlocks.FAIRY_CRYSTAL_BLOCK.get(), ModBlocks.FAIRY_CRYSTAL_CLUSTER.get(),
                    ModBlocks.FAIRY_LANTERN.get(), ModBlocks.MOON_LANTERN.get());
            tag(BlockTags.CRYSTAL_SOUND_BLOCKS).add(ModBlocks.FAIRY_CRYSTAL_BLOCK.get(), ModBlocks.FAIRY_CRYSTAL_CLUSTER.get());

            tag(ModTags.Blocks.FAIRY_GROUND).addTag(BlockTags.DIRT);
        }
    }

    public static class Items extends ItemTagsProvider {
        public Items(PackOutput output, CompletableFuture<HolderLookup.Provider> lookup,
                     CompletableFuture<TagsProvider.TagLookup<Block>> blockTags, ExistingFileHelper files) {
            super(output, lookup, blockTags, FairyGarden.MOD_ID, files);
        }

        @Override
        protected void addTags(HolderLookup.Provider provider) {
            copy(ModTags.Blocks.BLOSSOM_LOGS, ModTags.Items.BLOSSOM_LOGS);
            copy(ModTags.Blocks.MOONWOOD_LOGS, ModTags.Items.MOONWOOD_LOGS);
            copy(BlockTags.LOGS_THAT_BURN, ItemTags.LOGS_THAT_BURN);
            copy(BlockTags.PLANKS, ItemTags.PLANKS);
            copy(BlockTags.WOODEN_STAIRS, ItemTags.WOODEN_STAIRS);
            copy(BlockTags.WOODEN_SLABS, ItemTags.WOODEN_SLABS);
            copy(BlockTags.WOODEN_FENCES, ItemTags.WOODEN_FENCES);
            copy(BlockTags.FENCE_GATES, ItemTags.FENCE_GATES);
            copy(Tags.Blocks.FENCE_GATES_WOODEN, Tags.Items.FENCE_GATES_WOODEN);
            copy(Tags.Blocks.FENCES_WOODEN, Tags.Items.FENCES_WOODEN);
            copy(BlockTags.WOODEN_DOORS, ItemTags.WOODEN_DOORS);
            copy(BlockTags.WOODEN_TRAPDOORS, ItemTags.WOODEN_TRAPDOORS);
            copy(BlockTags.WOODEN_PRESSURE_PLATES, ItemTags.WOODEN_PRESSURE_PLATES);
            copy(BlockTags.WOODEN_BUTTONS, ItemTags.WOODEN_BUTTONS);
            copy(BlockTags.LEAVES, ItemTags.LEAVES);
            copy(BlockTags.SAPLINGS, ItemTags.SAPLINGS);
            copy(BlockTags.SMALL_FLOWERS, ItemTags.SMALL_FLOWERS);
            copy(BlockTags.TALL_FLOWERS, ItemTags.TALL_FLOWERS);
            copy(BlockTags.FLOWERS, ItemTags.FLOWERS);

            for (WoodSet set : ModBlocks.WOOD_SETS) {
                tag(ItemTags.SIGNS).add(set.signItem.get());
                tag(ItemTags.HANGING_SIGNS).add(set.hangingSignItem.get());
                tag(ItemTags.BOATS).add(set.boat.get());
                tag(ItemTags.CHEST_BOATS).add(set.chestBoat.get());
            }
            ModBlocks.SMALL_FLOWERS.forEach(flower -> tag(ModTags.Items.FAIRY_FLOWERS).add(flower.get().asItem()));
            ModBlocks.TALL_FLOWERS.forEach(flower -> tag(ModTags.Items.FAIRY_FLOWERS).add(flower.get().asItem()));
            tag(ItemTags.FOX_FOOD).add(ModItems.STRAWBERRY.get());
            tag(ItemTags.VILLAGER_PLANTABLE_SEEDS).add(ModItems.STARBLOOM_SEEDS.get());
            tag(Tags.Items.SEEDS).add(ModItems.STARBLOOM_SEEDS.get());
            tag(ItemTags.create(new ResourceLocation("forge", "fruits"))).add(ModItems.STRAWBERRY.get());
            tag(ItemTags.create(new ResourceLocation("forge", "fruits/strawberry"))).add(ModItems.STRAWBERRY.get());
            tag(Tags.Items.GEMS).add(ModItems.CRYSTAL_SHARD.get());
            tag(Tags.Items.DUSTS).add(ModItems.FAIRY_DUST.get());
        }
    }

    public static class Biomes extends TagsProvider<Biome> {
        public Biomes(PackOutput output, CompletableFuture<HolderLookup.Provider> lookup, ExistingFileHelper files) {
            super(output, Registries.BIOME, lookup, FairyGarden.MOD_ID, files);
        }

        @Override
        protected void addTags(HolderLookup.Provider provider) {
            for (var biome : ModBiomes.ALL) {
                tag(ModTags.Biomes.FAIRY_GARDEN).add(biome);
                tag(BiomeTags.IS_OVERWORLD).add(biome);
                tag(BiomeTags.HAS_MINESHAFT).add(biome);
                tag(BiomeTags.HAS_RUINED_PORTAL_STANDARD).add(biome);
                tag(BiomeTags.STRONGHOLD_BIASED_TO).add(biome);
                tag(Tags.Biomes.IS_LUSH).add(biome);
            }
            tag(ModTags.Biomes.HAS_FLOWER_SHRINE).add(ModBiomes.BLOSSOM_VALLEY, ModBiomes.LAVENDER_FIELDS, ModBiomes.STRAWBERRY_MEADOW);
            tag(ModTags.Biomes.HAS_HIDDEN_FAIRY_GARDEN).add(ModBiomes.FAIRY_GLADE, ModBiomes.BLOSSOM_VALLEY);
            tag(ModTags.Biomes.HAS_MOONSTONE_CIRCLE).add(ModBiomes.MOONLIGHT_FOREST);
            tag(ModTags.Biomes.HAS_MUSHROOM_COTTAGE).add(ModBiomes.STRAWBERRY_MEADOW, ModBiomes.FAIRY_GLADE);

            // Keep vanilla structures that the replaced biomes used to host.
            tag(BiomeTags.HAS_VILLAGE_PLAINS).add(ModBiomes.STRAWBERRY_MEADOW, ModBiomes.LAVENDER_FIELDS);
            tag(BiomeTags.HAS_PILLAGER_OUTPOST).add(ModBiomes.STRAWBERRY_MEADOW, ModBiomes.LAVENDER_FIELDS);
            tag(BiomeTags.HAS_WOODLAND_MANSION).add(ModBiomes.MOONLIGHT_FOREST);
            tag(BiomeTags.HAS_TRAIL_RUINS).add(ModBiomes.BLOSSOM_VALLEY);
            tag(BiomeTags.IS_FOREST).add(ModBiomes.MOONLIGHT_FOREST, ModBiomes.FAIRY_GLADE);
            tag(BiomeTags.IS_HILL).add(ModBiomes.BLOSSOM_VALLEY);

            tag(Tags.Biomes.IS_PLAINS).add(ModBiomes.STRAWBERRY_MEADOW, ModBiomes.LAVENDER_FIELDS);
            tag(Tags.Biomes.IS_DENSE_OVERWORLD).add(ModBiomes.MOONLIGHT_FOREST);
            tag(Tags.Biomes.IS_MAGICAL).add(ModBiomes.MOONLIGHT_FOREST, ModBiomes.FAIRY_GLADE);
            tag(Tags.Biomes.IS_SPARSE_OVERWORLD).add(ModBiomes.LAVENDER_FIELDS);
        }
    }
}
