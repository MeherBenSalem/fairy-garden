package com.sugarplumstudios.fairygarden.datagen;

import com.sugarplumstudios.fairygarden.FairyGarden;
import com.sugarplumstudios.fairygarden.registry.ModBlocks;
import com.sugarplumstudios.fairygarden.registry.ModItems;
import com.sugarplumstudios.fairygarden.registry.ModTags;
import com.sugarplumstudios.fairygarden.registry.WoodSet;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.function.Consumer;

public class ModRecipeProvider extends RecipeProvider {
    public ModRecipeProvider(PackOutput output) {
        super(output);
    }

    @Override
    protected void buildRecipes(Consumer<FinishedRecipe> out) {
        for (WoodSet set : ModBlocks.WOOD_SETS) {
            woodRecipes(out, set, set == ModBlocks.BLOSSOM ? ModTags.Items.BLOSSOM_LOGS : ModTags.Items.MOONWOOD_LOGS);
        }

        // Flowers into dyes
        dye(out, Items.PINK_DYE, ModBlocks.ROSY_DAISY.get(), 1);
        dye(out, Items.PURPLE_DYE, ModBlocks.LAVENDER.get(), 1);
        dye(out, Items.YELLOW_DYE, ModBlocks.CREAM_BUTTERCUP.get(), 1);
        dye(out, Items.LIME_DYE, ModBlocks.MINT_POSY.get(), 1);
        dye(out, Items.WHITE_DYE, ModBlocks.STARFLOWER.get(), 1);
        dye(out, Items.LIGHT_BLUE_DYE, ModBlocks.BLUEBELL.get(), 1);
        dye(out, Items.CYAN_DYE, ModBlocks.MOONFLOWER.get(), 1);
        dye(out, Items.MAGENTA_DYE, ModBlocks.CRYSTAL_FLOWER.get(), 1);
        dye(out, Items.PURPLE_DYE, ModBlocks.TALL_LAVENDER.get(), 2);
        dye(out, Items.PINK_DYE, ModBlocks.FAIRY_FOXGLOVE.get(), 2);
        dye(out, Items.PINK_DYE, ModBlocks.BLOSSOM_PETALS.get(), 1);
        dye(out, Items.WHITE_DYE, ModBlocks.SNOW_BLOSSOM_PETALS.get(), 1);
        dye(out, Items.PINK_DYE, ModBlocks.PINK_HYDRANGEA.get(), 2);
        dye(out, Items.LIGHT_BLUE_DYE, ModBlocks.BLUE_HYDRANGEA.get(), 2);
        dye(out, Items.PURPLE_DYE, ModBlocks.LAVENDER_HYDRANGEA.get(), 2);
        dye(out, Items.LIGHT_BLUE_DYE, ModItems.MOON_PETAL.get(), 1);

        // Flower carpets and giant petals
        flowerCarpet(out, ModBlocks.PINK_FLOWER_CARPET.get(), ModBlocks.ROSY_DAISY.get());
        flowerCarpet(out, ModBlocks.LAVENDER_FLOWER_CARPET.get(), ModBlocks.LAVENDER.get());
        flowerCarpet(out, ModBlocks.CREAM_FLOWER_CARPET.get(), ModBlocks.CREAM_BUTTERCUP.get());
        flowerCarpet(out, ModBlocks.MINT_FLOWER_CARPET.get(), ModBlocks.MINT_POSY.get());
        flowerCarpet(out, ModBlocks.BLUE_FLOWER_CARPET.get(), ModBlocks.BLUEBELL.get());
        twoByTwo(out, ModBlocks.PINK_GIANT_PETALS.get(), ModBlocks.BLOSSOM_PETALS.get(), 1);
        twoByTwo(out, ModBlocks.LAVENDER_GIANT_PETALS.get(), ModBlocks.LAVENDER.get(), 1);
        twoByTwo(out, ModBlocks.BLUE_GIANT_PETALS.get(), ModBlocks.BLUEBELL.get(), 1);
        twoByTwo(out, ModBlocks.CREAM_GIANT_PETALS.get(), ModBlocks.CREAM_BUTTERCUP.get(), 1);
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ModBlocks.GIANT_FLOWER_STEM.get(), 3)
                .pattern("S").pattern("S").pattern("S").define('S', Items.SUGAR_CANE)
                .unlockedBy(getHasName(Items.SUGAR_CANE), has(Items.SUGAR_CANE)).save(out);
        ShapelessRecipeBuilder.shapeless(RecipeCategory.BUILDING_BLOCKS, ModBlocks.GIANT_FLOWER_PISTIL.get())
                .requires(Items.GLOWSTONE_DUST).requires(ModBlocks.CREAM_BUTTERCUP.get(), 2).requires(Items.HONEYCOMB)
                .unlockedBy(getHasName(ModBlocks.CREAM_BUTTERCUP.get()), has(ModBlocks.CREAM_BUTTERCUP.get())).save(out);

        // Lavender bale <-> lavender
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ModBlocks.LAVENDER_BALE.get())
                .pattern("LLL").pattern("LLL").pattern("LLL").define('L', ModBlocks.LAVENDER.get())
                .unlockedBy(getHasName(ModBlocks.LAVENDER.get()), has(ModBlocks.LAVENDER.get())).save(out);
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModBlocks.LAVENDER.get(), 9).requires(ModBlocks.LAVENDER_BALE.get())
                .unlockedBy(getHasName(ModBlocks.LAVENDER_BALE.get()), has(ModBlocks.LAVENDER_BALE.get()))
                .save(out, FairyGarden.id("lavender_from_bale"));

        // Crystals and fairy magic
        twoByTwo(out, ModBlocks.FAIRY_CRYSTAL_BLOCK.get(), ModItems.CRYSTAL_SHARD.get(), 1);
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.FAIRY_DUST.get(), 2)
                .requires(ModItems.CRYSTAL_SHARD.get()).requires(Items.GLOWSTONE_DUST).requires(Items.SUGAR)
                .unlockedBy(getHasName(ModItems.CRYSTAL_SHARD.get()), has(ModItems.CRYSTAL_SHARD.get()))
                .save(out, FairyGarden.id("fairy_dust_from_crystal"));
        lantern(out, ModBlocks.FAIRY_LANTERN.get(), Items.GOLD_NUGGET, ModItems.FAIRY_DUST.get());
        lantern(out, ModBlocks.MOON_LANTERN.get(), Items.IRON_NUGGET, ModItems.MOON_PETAL.get());
        ShapelessRecipeBuilder.shapeless(RecipeCategory.DECORATIONS, ModBlocks.GLIMMER_LILY.get())
                .requires(Items.LILY_PAD).requires(ModItems.FAIRY_DUST.get())
                .unlockedBy(getHasName(ModItems.FAIRY_DUST.get()), has(ModItems.FAIRY_DUST.get())).save(out);
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.FLOWER_CROWN.get())
                .pattern("FSF").pattern("F F").define('F', ModTags.Items.FAIRY_FLOWERS).define('S', Items.STRING)
                .unlockedBy("has_fairy_flower", has(ModTags.Items.FAIRY_FLOWERS)).save(out);

        // Treats
        ShapelessRecipeBuilder.shapeless(RecipeCategory.FOOD, ModItems.LAVENDER_TEA.get())
                .requires(Items.GLASS_BOTTLE).requires(ModBlocks.LAVENDER.get(), 2).requires(Items.SUGAR)
                .unlockedBy(getHasName(ModBlocks.LAVENDER.get()), has(ModBlocks.LAVENDER.get())).save(out);
        ShapelessRecipeBuilder.shapeless(RecipeCategory.FOOD, ModItems.STRAWBERRY_SHORTCAKE.get(), 2)
                .requires(ModItems.STRAWBERRY.get(), 2).requires(Items.WHEAT).requires(Items.SUGAR).requires(Items.EGG)
                .unlockedBy(getHasName(ModItems.STRAWBERRY.get()), has(ModItems.STRAWBERRY.get())).save(out);
    }

    private void woodRecipes(Consumer<FinishedRecipe> out, WoodSet set, TagKey<Item> logs) {
        planksFromLog(out, set.planks.get(), logs, 4);
        woodFromLogs(out, set.wood.get(), set.log.get());
        woodFromLogs(out, set.strippedWood.get(), set.strippedLog.get());
        Ingredient planks = Ingredient.of(set.planks.get());
        String has = getHasName(set.planks.get());
        stairBuilder(set.stairs.get(), planks).unlockedBy(has, has(set.planks.get())).save(out);
        slab(out, RecipeCategory.BUILDING_BLOCKS, set.slab.get(), set.planks.get());
        fenceBuilder(set.fence.get(), planks).unlockedBy(has, has(set.planks.get())).save(out);
        fenceGateBuilder(set.fenceGate.get(), planks).unlockedBy(has, has(set.planks.get())).save(out);
        doorBuilder(set.door.get(), planks).unlockedBy(has, has(set.planks.get())).save(out);
        trapdoorBuilder(set.trapdoor.get(), planks).unlockedBy(has, has(set.planks.get())).save(out);
        pressurePlate(out, set.pressurePlate.get(), set.planks.get());
        buttonBuilder(set.button.get(), planks).unlockedBy(has, has(set.planks.get())).save(out);
        signBuilder(set.signItem.get(), planks).unlockedBy(has, has(set.planks.get())).save(out);
        hangingSign(out, set.hangingSignItem.get(), set.strippedLog.get());
        woodenBoat(out, set.boat.get(), set.planks.get());
        chestBoat(out, set.chestBoat.get(), set.boat.get());
    }

    private static void dye(Consumer<FinishedRecipe> out, Item dye, ItemLike flower, int count) {
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, dye, count).requires(flower).group(name(dye))
                .unlockedBy(getHasName(flower), has(flower))
                .save(out, FairyGarden.id(name(dye) + "_from_" + name(flower)));
    }

    private static void flowerCarpet(Consumer<FinishedRecipe> out, ItemLike carpet, ItemLike flower) {
        ShapelessRecipeBuilder.shapeless(RecipeCategory.DECORATIONS, carpet, 4).requires(flower, 2).requires(Items.MOSS_CARPET)
                .unlockedBy(getHasName(flower), has(flower)).save(out);
    }

    private static void twoByTwo(Consumer<FinishedRecipe> out, ItemLike result, ItemLike input, int count) {
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, result, count).pattern("##").pattern("##").define('#', input)
                .unlockedBy(getHasName(input), has(input)).save(out);
    }

    private static void lantern(Consumer<FinishedRecipe> out, ItemLike lantern, ItemLike nugget, ItemLike core) {
        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, lantern).pattern("NNN").pattern("NCN").pattern("NNN")
                .define('N', nugget).define('C', core).unlockedBy(getHasName(core), has(core)).save(out);
    }

    private static String name(ItemLike item) {
        return ForgeRegistries.ITEMS.getKey(item.asItem()).getPath();
    }
}
