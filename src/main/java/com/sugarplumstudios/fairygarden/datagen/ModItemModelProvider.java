package com.sugarplumstudios.fairygarden.datagen;

import com.sugarplumstudios.fairygarden.FairyGarden;
import com.sugarplumstudios.fairygarden.registry.ModBlocks;
import com.sugarplumstudios.fairygarden.registry.ModItems;
import com.sugarplumstudios.fairygarden.registry.WoodSet;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItemModelProvider extends ItemModelProvider {
    public ModItemModelProvider(PackOutput output, ExistingFileHelper files) {
        super(output, FairyGarden.MOD_ID, files);
    }

    @Override
    protected void registerModels() {
        for (WoodSet set : ModBlocks.WOOD_SETS) {
            for (Block block : new Block[]{set.log.get(), set.wood.get(), set.strippedLog.get(), set.strippedWood.get(), set.planks.get(),
                    set.stairs.get(), set.slab.get(), set.fenceGate.get(), set.pressurePlate.get()}) {
                blockParent(block);
            }
            withExistingParent(name(set.fence.get()), modLoc("block/" + set.name + "_fence_inventory"));
            withExistingParent(name(set.button.get()), modLoc("block/" + set.name + "_button_inventory"));
            withExistingParent(name(set.trapdoor.get()), modLoc("block/" + set.name + "_trapdoor_bottom"));
            flatItem(set.door.get(), itemTex(name(set.door.get())));
            flatItem(set.signItem.get(), itemTex(name(set.signItem.get())));
            flatItem(set.hangingSignItem.get(), itemTex(name(set.hangingSignItem.get())));
            flatItem(set.boat.get(), itemTex(name(set.boat.get())));
            flatItem(set.chestBoat.get(), itemTex(name(set.chestBoat.get())));
        }

        for (Block block : new Block[]{ModBlocks.BLOSSOM_LEAVES.get(), ModBlocks.SNOW_BLOSSOM_LEAVES.get(), ModBlocks.MOONWOOD_LEAVES.get(),
                ModBlocks.PINK_HYDRANGEA.get(), ModBlocks.BLUE_HYDRANGEA.get(), ModBlocks.LAVENDER_HYDRANGEA.get(),
                ModBlocks.GIANT_FLOWER_STEM.get(),
                ModBlocks.GIANT_FLOWER_PISTIL.get(), ModBlocks.FAIRY_CRYSTAL_BLOCK.get(), ModBlocks.LAVENDER_BALE.get()}) {
            blockParent(block);
        }
        ModBlocks.GIANT_PETALS.forEach(petals -> blockParent(petals.get()));
        for (Block cap : new Block[]{ModBlocks.FAIRY_MUSHROOM_BLOCK.get(), ModBlocks.GLOWCAP_BLOCK.get()}) {
            withExistingParent(name(cap), modLoc("block/" + name(cap) + "_inventory"));
        }
        ModBlocks.FLOWER_CARPETS.forEach(carpet -> blockParent(carpet.get()));

        // Plants show their sprite in the hand.
        for (Block block : new Block[]{ModBlocks.BLOSSOM_SAPLING.get(), ModBlocks.SNOW_BLOSSOM_SAPLING.get(), ModBlocks.MOONWOOD_SAPLING.get(),
                ModBlocks.BLUSH_GRASS.get(), ModBlocks.MOONLIT_GRASS.get(), ModBlocks.FAIRY_MUSHROOM.get(), ModBlocks.GLOWCAP.get(),
                ModBlocks.MOONBELL.get(), ModBlocks.FAIRY_CRYSTAL_CLUSTER.get()}) {
            flatItem(block, blockTex(name(block)));
        }
        for (RegistryObject<? extends Block> flower : ModBlocks.SMALL_FLOWERS) {
            Block block = flower.get();
            flatItem(block, blockTex(block == ModBlocks.MOONFLOWER.get() ? name(block) + "_open" : name(block)));
        }
        for (RegistryObject<? extends Block> flower : ModBlocks.TALL_FLOWERS) {
            flatItem(flower.get(), blockTex(name(flower.get()) + "_top"));
        }
        for (Block block : new Block[]{ModBlocks.BLOSSOM_PETALS.get(), ModBlocks.SNOW_BLOSSOM_PETALS.get(), ModBlocks.GLIMMER_LILY.get(),
                ModBlocks.FAIRY_LANTERN.get(), ModBlocks.MOON_LANTERN.get()}) {
            flatItem(block, itemTex(name(block)));
        }

        for (RegistryObject<? extends Item> item : new RegistryObject[]{ModItems.STRAWBERRY, ModItems.STRAWBERRY_SHORTCAKE, ModItems.LAVENDER_TEA,
                ModItems.STARBLOOM_SEEDS, ModItems.MOON_PETAL, ModItems.FAIRY_DUST, ModItems.CRYSTAL_SHARD, ModItems.FLOWER_CROWN}) {
            flatItem(item.get(), itemTex(name(item.get())));
        }
        withExistingParent(name(ModItems.BUTTERFLY_SPAWN_EGG.get()), mcLoc("item/template_spawn_egg"));
        withExistingParent(name(ModItems.MOON_WISP_SPAWN_EGG.get()), mcLoc("item/template_spawn_egg"));
    }

    private void blockParent(Block block) {
        withExistingParent(name(block), modLoc("block/" + name(block)));
    }

    private void flatItem(ItemLike item, ResourceLocation texture) {
        getBuilder(name(item)).parent(new ModelFile.UncheckedModelFile("item/generated")).texture("layer0", texture);
    }

    private static String name(ItemLike item) {
        return ForgeRegistries.ITEMS.getKey(item.asItem()).getPath();
    }

    private static String name(Block block) {
        return ForgeRegistries.BLOCKS.getKey(block).getPath();
    }

    private ResourceLocation itemTex(String name) {
        return modLoc("item/" + name);
    }

    private ResourceLocation blockTex(String name) {
        return modLoc("block/" + name);
    }
}
