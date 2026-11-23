package com.sugarplumstudios.fairygarden.datagen;

import com.sugarplumstudios.fairygarden.FairyGarden;
import com.sugarplumstudios.fairygarden.registry.ModBlocks;
import com.sugarplumstudios.fairygarden.registry.ModItems;
import com.sugarplumstudios.fairygarden.registry.ModTags;
import com.sugarplumstudios.fairygarden.worldgen.ModBiomes;
import com.sugarplumstudios.fairygarden.worldgen.ModStructures;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.FrameType;
import net.minecraft.advancements.RequirementsStrategy;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.LocationPredicate;
import net.minecraft.advancements.critereon.PlayerTrigger;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.common.data.ForgeAdvancementProvider;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Consumer;

public class ModAdvancements implements ForgeAdvancementProvider.AdvancementGenerator {
    @Override
    public void generate(HolderLookup.Provider registries, Consumer<Advancement> saver, ExistingFileHelper files) {
        Advancement root = Advancement.Builder.advancement()
                .display(ModBlocks.CRYSTAL_FLOWER.get(), title("root"), description("root"),
                        FairyGarden.id("textures/block/blossom_planks.png"), FrameType.TASK, false, false, false)
                .addCriterion("has_fairy_flower", InventoryChangeTrigger.TriggerInstance.hasItems(
                        ItemPredicate.Builder.item().of(ModTags.Items.FAIRY_FLOWERS).build()))
                .save(saver, FairyGarden.id("root"), files);

        Advancement.Builder explorer = display(Advancement.Builder.advancement().parent(root), ModBlocks.FAIRY_FOXGLOVE.get(),
                "all_biomes", FrameType.CHALLENGE).requirements(RequirementsStrategy.AND);
        Object[][] biomes = {
                {ModBiomes.BLOSSOM_VALLEY, ModBlocks.BLOSSOM_SAPLING, "blossom_valley"},
                {ModBiomes.LAVENDER_FIELDS, ModBlocks.LAVENDER, "lavender_fields"},
                {ModBiomes.STRAWBERRY_MEADOW, ModItems.STRAWBERRY, "strawberry_meadow"},
                {ModBiomes.MOONLIGHT_FOREST, ModBlocks.MOONFLOWER, "moonlight_forest"},
                {ModBiomes.FAIRY_GLADE, ModBlocks.CRYSTAL_FLOWER, "fairy_glade"}};
        for (Object[] entry : biomes) {
            @SuppressWarnings("unchecked") ResourceKey<Biome> biome = (ResourceKey<Biome>) entry[0];
            ItemLike icon = (ItemLike) ((RegistryObject<?>) entry[1]).get();
            String id = (String) entry[2];
            display(Advancement.Builder.advancement().parent(root), icon, id, FrameType.TASK)
                    .addCriterion("entered", PlayerTrigger.TriggerInstance.located(LocationPredicate.inBiome(biome)))
                    .save(saver, FairyGarden.id(id), files);
            explorer.addCriterion(id, PlayerTrigger.TriggerInstance.located(LocationPredicate.inBiome(biome)));
        }
        explorer.save(saver, FairyGarden.id("all_biomes"), files);

        Advancement.Builder collector = display(Advancement.Builder.advancement().parent(root), ModBlocks.ROSY_DAISY.get(),
                "flower_collector", FrameType.GOAL).requirements(RequirementsStrategy.AND);
        for (RegistryObject<? extends Block> flower : ModBlocks.SMALL_FLOWERS) {
            collector.addCriterion(flower.getId().getPath(), InventoryChangeTrigger.TriggerInstance.hasItems(flower.get()));
        }
        for (RegistryObject<? extends Block> flower : ModBlocks.TALL_FLOWERS) {
            collector.addCriterion(flower.getId().getPath(), InventoryChangeTrigger.TriggerInstance.hasItems(flower.get()));
        }
        collector.save(saver, FairyGarden.id("flower_collector"), files);

        item(saver, files, root, ModItems.MOON_PETAL.get(), "moon_petal", FrameType.TASK);
        item(saver, files, root, ModItems.FAIRY_DUST.get(), "fairy_dust", FrameType.TASK);
        item(saver, files, root, ModItems.FLOWER_CROWN.get(), "flower_crown", FrameType.TASK);

        display(Advancement.Builder.advancement().parent(root), ModBlocks.FAIRY_LANTERN.get(), "hidden_fairy_garden", FrameType.GOAL)
                .addCriterion("found", inStructure(ModStructures.HIDDEN_FAIRY_GARDEN))
                .save(saver, FairyGarden.id("hidden_fairy_garden"), files);
        display(Advancement.Builder.advancement().parent(root), ModBlocks.MOON_LANTERN.get(), "treasure", FrameType.TASK)
                .addCriterion("flower_shrine", inStructure(ModStructures.FLOWER_SHRINE))
                .addCriterion("moonstone_circle", inStructure(ModStructures.MOONSTONE_CIRCLE))
                .addCriterion("mushroom_cottage", inStructure(ModStructures.MUSHROOM_COTTAGE))
                .requirements(RequirementsStrategy.OR)
                .save(saver, FairyGarden.id("treasure"), files);
    }

    private static PlayerTrigger.TriggerInstance inStructure(ResourceKey<Structure> structure) {
        return PlayerTrigger.TriggerInstance.located(LocationPredicate.inStructure(structure));
    }

    private static void item(Consumer<Advancement> saver, ExistingFileHelper files, Advancement parent, ItemLike item, String id, FrameType frame) {
        display(Advancement.Builder.advancement().parent(parent), item, id, frame)
                .addCriterion("has_item", InventoryChangeTrigger.TriggerInstance.hasItems(item))
                .save(saver, FairyGarden.id(id), files);
    }

    private static Advancement.Builder display(Advancement.Builder builder, ItemLike icon, String id, FrameType frame) {
        return builder.display(icon, title(id), description(id), null, frame, true, frame != FrameType.TASK, false);
    }

    private static Component title(String id) {
        return Component.translatable("advancements.fairygarden." + id + ".title");
    }

    private static Component description(String id) {
        return Component.translatable("advancements.fairygarden." + id + ".description");
    }
}
