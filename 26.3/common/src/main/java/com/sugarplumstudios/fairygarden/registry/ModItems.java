package com.sugarplumstudios.fairygarden.registry;

import com.sugarplumstudios.fairygarden.item.FairyDustItem;
import com.sugarplumstudios.fairygarden.item.ModArmorMaterials;
import com.sugarplumstudios.fairygarden.platform.RegistryEntry;
import com.sugarplumstudios.fairygarden.platform.Services;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.item.component.Consumables;
import net.minecraft.world.item.consume_effects.ApplyStatusEffectsConsumeEffect;
import net.minecraft.world.item.consume_effects.RemoveStatusEffectsConsumeEffect;
import net.minecraft.world.item.equipment.ArmorType;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public final class ModItems {

    /** Every Fairy Garden item, in registration order. */
    public static final List<RegistryEntry<? extends Item>> ALL = new ArrayList<>();
    /** Creative-tab order: block items (in block registration order) first, then the rest. */
    public static final List<RegistryEntry<? extends Item>> BLOCK_TAB_ORDER = new ArrayList<>();
    public static final List<RegistryEntry<? extends Item>> ITEM_TAB_ORDER = new ArrayList<>();

    public static final FoodProperties STRAWBERRY_FOOD = new FoodProperties.Builder().nutrition(2).saturationModifier(0.3F).build();
    public static final FoodProperties SHORTCAKE_FOOD = new FoodProperties.Builder().nutrition(7).saturationModifier(0.6F).build();
    public static final FoodProperties LAVENDER_TEA_FOOD = new FoodProperties.Builder().nutrition(2).saturationModifier(0.4F).alwaysEdible().build();

    /** Strawberries are quick to eat (as fast as dried kelp). */
    private static Consumable strawberry() {
        return Consumables.defaultFood().consumeSeconds(0.8F).build();
    }

    private static Consumable shortcake() {
        return Consumables.defaultFood()
                .onConsume(new ApplyStatusEffectsConsumeEffect(new MobEffectInstance(MobEffects.ABSORPTION, 600, 0))).build();
    }

    /** Lavender tea: gentle regeneration, and it settles nausea and poison. Leaves the bottle behind. */
    private static Consumable lavenderTea() {
        return Consumables.defaultDrink()
                .onConsume(new ApplyStatusEffectsConsumeEffect(new MobEffectInstance(MobEffects.REGENERATION, 200, 0)))
                .onConsume(new RemoveStatusEffectsConsumeEffect(HolderSet.direct(MobEffects.NAUSEA, MobEffects.POISON)))
                .build();
    }

    public static final RegistryEntry<BlockItem> STRAWBERRY = register("strawberry",
            () -> new BlockItem(ModBlocks.STRAWBERRY_BUSH.get(), Keys.itemProps().useItemDescriptionPrefix().food(STRAWBERRY_FOOD, strawberry())));
    public static final RegistryEntry<Item> STRAWBERRY_SHORTCAKE = register("strawberry_shortcake",
            () -> new Item(Keys.itemProps().food(SHORTCAKE_FOOD, shortcake()).stacksTo(16)));
    public static final RegistryEntry<Item> LAVENDER_TEA = register("lavender_tea",
            () -> new Item(Keys.itemProps().food(LAVENDER_TEA_FOOD, lavenderTea()).usingConvertsTo(Items.GLASS_BOTTLE)
                    .craftRemainder(Items.GLASS_BOTTLE).stacksTo(16)));
    public static final RegistryEntry<BlockItem> STARBLOOM_SEEDS = register("starbloom_seeds",
            () -> new BlockItem(ModBlocks.STARBLOOM.get(), Keys.itemProps().useItemDescriptionPrefix()));
    public static final RegistryEntry<Item> MOON_PETAL = register("moon_petal", () -> new Item(Keys.itemProps()));
    public static final RegistryEntry<FairyDustItem> FAIRY_DUST = register("fairy_dust",
            () -> new FairyDustItem(Keys.itemProps().rarity(Rarity.UNCOMMON)));
    public static final RegistryEntry<Item> CRYSTAL_SHARD = register("crystal_shard", () -> new Item(Keys.itemProps()));
    public static final RegistryEntry<Item> FLOWER_CROWN = register("flower_crown",
            () -> new Item(Keys.itemProps().humanoidArmor(ModArmorMaterials.FLOWER_CROWN, ArmorType.HELMET)
                    .durability(ModArmorMaterials.FLOWER_CROWN_DURABILITY)));

    // Spawn egg colours live in the item model's tints (assets/fairygarden/items/*_spawn_egg.json).
    public static final RegistryEntry<SpawnEggItem> BUTTERFLY_SPAWN_EGG = register("butterfly_spawn_egg",
            () -> new SpawnEggItem(Keys.itemProps().spawnEgg(ModEntities.BUTTERFLY.get())));
    public static final RegistryEntry<SpawnEggItem> MOON_WISP_SPAWN_EGG = register("moon_wisp_spawn_egg",
            () -> new SpawnEggItem(Keys.itemProps().spawnEgg(ModEntities.MOON_WISP.get())));
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
