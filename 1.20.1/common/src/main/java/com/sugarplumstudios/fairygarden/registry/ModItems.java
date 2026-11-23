package com.sugarplumstudios.fairygarden.registry;

import net.minecraft.core.registries.Registries;
import com.sugarplumstudios.fairygarden.platform.Services;
import com.sugarplumstudios.fairygarden.FairyGarden;
import com.sugarplumstudios.fairygarden.item.FairyDustItem;
import com.sugarplumstudios.fairygarden.item.LavenderTeaItem;
import com.sugarplumstudios.fairygarden.item.ModArmorMaterials;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemNameBlockItem;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import com.sugarplumstudios.fairygarden.platform.RegistryEntry;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public final class ModItems {

    /** Every Fairy Garden item, in registration order. */
    public static final List<RegistryEntry<? extends Item>> ALL = new ArrayList<>();
    /** Creative-tab order: block items (in block registration order) first, then the rest. */
    public static final List<RegistryEntry<? extends Item>> BLOCK_TAB_ORDER = new ArrayList<>();
    public static final List<RegistryEntry<? extends Item>> ITEM_TAB_ORDER = new ArrayList<>();

    public static final FoodProperties STRAWBERRY_FOOD = new FoodProperties.Builder().nutrition(2).saturationMod(0.3F).fast().build();
    public static final FoodProperties SHORTCAKE_FOOD = new FoodProperties.Builder().nutrition(7).saturationMod(0.6F)
            .effect(new MobEffectInstance(MobEffects.ABSORPTION, 600, 0), 1.0F).build();
    public static final FoodProperties LAVENDER_TEA_FOOD = new FoodProperties.Builder().nutrition(2).saturationMod(0.4F).alwaysEat()
            .effect(new MobEffectInstance(MobEffects.REGENERATION, 200, 0), 1.0F).build();

    public static final RegistryEntry<ItemNameBlockItem> STRAWBERRY = register("strawberry",
            () -> new ItemNameBlockItem(ModBlocks.STRAWBERRY_BUSH.get(), new Item.Properties().food(STRAWBERRY_FOOD)));
    public static final RegistryEntry<Item> STRAWBERRY_SHORTCAKE = register("strawberry_shortcake",
            () -> new Item(new Item.Properties().food(SHORTCAKE_FOOD).stacksTo(16)));
    public static final RegistryEntry<LavenderTeaItem> LAVENDER_TEA = register("lavender_tea",
            () -> new LavenderTeaItem(new Item.Properties().food(LAVENDER_TEA_FOOD).craftRemainder(Items.GLASS_BOTTLE).stacksTo(16)));
    public static final RegistryEntry<ItemNameBlockItem> STARBLOOM_SEEDS = register("starbloom_seeds",
            () -> new ItemNameBlockItem(ModBlocks.STARBLOOM.get(), new Item.Properties()));
    public static final RegistryEntry<Item> MOON_PETAL = register("moon_petal", () -> new Item(new Item.Properties()));
    public static final RegistryEntry<FairyDustItem> FAIRY_DUST = register("fairy_dust",
            () -> new FairyDustItem(new Item.Properties().rarity(Rarity.UNCOMMON)));
    public static final RegistryEntry<Item> CRYSTAL_SHARD = register("crystal_shard", () -> new Item(new Item.Properties()));
    public static final RegistryEntry<ArmorItem> FLOWER_CROWN = register("flower_crown",
            () -> new ArmorItem(ModArmorMaterials.FLOWER_CROWN, ArmorItem.Type.HELMET, new Item.Properties()));

    public static final RegistryEntry<Item> BUTTERFLY_SPAWN_EGG = register("butterfly_spawn_egg",
            () -> Services.PLATFORM.spawnEgg(ModEntities.BUTTERFLY, 0xF7B8D4, 0xB9E8D0, new Item.Properties()));
    public static final RegistryEntry<Item> MOON_WISP_SPAWN_EGG = register("moon_wisp_spawn_egg",
            () -> Services.PLATFORM.spawnEgg(ModEntities.MOON_WISP, 0x4B5BA8, 0xBFE4FF, new Item.Properties()));

    private static <T extends Item> RegistryEntry<T> register(String name, Supplier<T> item) {
        RegistryEntry<T> registered = Services.PLATFORM.register(Registries.ITEM, name, item);
        ITEM_TAB_ORDER.add(registered);
        ALL.add(registered);
        return registered;
    }

    static <T extends Item> RegistryEntry<T> registerBlockItem(String name, Supplier<T> item) {
        RegistryEntry<T> registered = Services.PLATFORM.register(Registries.ITEM, name, item);
        BLOCK_TAB_ORDER.add(registered);
        ALL.add(registered);
        return registered;
    }

    /** Loads the class, which registers everything above. */
    public static void init() {
    }

    private ModItems() {
    }
}
