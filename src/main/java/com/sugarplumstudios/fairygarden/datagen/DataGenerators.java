package com.sugarplumstudios.fairygarden.datagen;

import com.sugarplumstudios.fairygarden.FairyGarden;
import com.sugarplumstudios.fairygarden.datagen.structure.StructureTemplateProvider;
import com.sugarplumstudios.fairygarden.worldgen.ModBiomes;
import com.sugarplumstudios.fairygarden.worldgen.ModConfiguredFeatures;
import com.sugarplumstudios.fairygarden.worldgen.ModPlacedFeatures;
import com.sugarplumstudios.fairygarden.worldgen.ModStructures;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraftforge.common.data.BlockTagsProvider;
import net.minecraftforge.common.data.DatapackBuiltinEntriesProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.common.data.ForgeAdvancementProvider;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

@Mod.EventBusSubscriber(modid = FairyGarden.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class DataGenerators {
    public static final RegistrySetBuilder WORLDGEN = new RegistrySetBuilder()
            .add(Registries.CONFIGURED_FEATURE, ModConfiguredFeatures::bootstrap)
            .add(Registries.PLACED_FEATURE, ModPlacedFeatures::bootstrap)
            .add(Registries.BIOME, ModBiomes::bootstrap)
            .add(Registries.PROCESSOR_LIST, ModStructures::bootstrapProcessors)
            .add(Registries.TEMPLATE_POOL, ModStructures::bootstrapPools)
            .add(Registries.STRUCTURE, ModStructures::bootstrapStructures)
            .add(Registries.STRUCTURE_SET, ModStructures::bootstrapSets);

    private DataGenerators() {
    }

    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput output = generator.getPackOutput();
        ExistingFileHelper files = event.getExistingFileHelper();

        DatapackBuiltinEntriesProvider worldgen = new DatapackBuiltinEntriesProvider(output, event.getLookupProvider(), WORLDGEN,
                Set.of(FairyGarden.MOD_ID));
        generator.addProvider(event.includeServer(), worldgen);
        CompletableFuture<HolderLookup.Provider> lookup = worldgen.getRegistryProvider();

        generator.addProvider(event.includeClient(), new ModBlockStateProvider(output, files));
        generator.addProvider(event.includeClient(), new ModItemModelProvider(output, files));
        generator.addProvider(event.includeClient(), new ModLanguageProvider(output));
        generator.addProvider(event.includeClient(), new ModSoundDefinitionsProvider(output, files));
        generator.addProvider(event.includeClient(), new ModParticleDescriptionProvider(output, files));

        BlockTagsProvider blockTags = new ModTagProviders.Blocks(output, lookup, files);
        generator.addProvider(event.includeServer(), blockTags);
        generator.addProvider(event.includeServer(), new ModTagProviders.Items(output, lookup, blockTags.contentsGetter(), files));
        generator.addProvider(event.includeServer(), new ModTagProviders.Biomes(output, lookup, files));
        generator.addProvider(event.includeServer(), new ModRecipeProvider(output));
        generator.addProvider(event.includeServer(), new LootTableProvider(output, Set.of(), List.of(
                new LootTableProvider.SubProviderEntry(ModLootProviders.Blocks::new, LootContextParamSets.BLOCK),
                new LootTableProvider.SubProviderEntry(ModLootProviders.Chests::new, LootContextParamSets.CHEST),
                new LootTableProvider.SubProviderEntry(ModLootProviders.Entities::new, LootContextParamSets.ENTITY))));
        generator.addProvider(event.includeServer(), new ForgeAdvancementProvider(output, lookup, files, List.of(new ModAdvancements())));
        generator.addProvider(event.includeServer(), new StructureTemplateProvider(output));
    }
}
