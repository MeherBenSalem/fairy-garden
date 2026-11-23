package com.sugarplumstudios.fairygarden.item;

import com.sugarplumstudios.fairygarden.FairyGarden;
import com.sugarplumstudios.fairygarden.registry.ModTags;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.crafting.Ingredient;

public enum ModArmorMaterials implements ArmorMaterial {
    /** A woven crown of fairy flowers: about as sturdy as a leather cap, and much prettier. */
    FLOWER_CROWN("flower_crown", 60, 1, 18);

    private final String name;
    private final int durability;
    private final int defense;
    private final int enchantability;

    ModArmorMaterials(String name, int durability, int defense, int enchantability) {
        this.name = FairyGarden.MOD_ID + ":" + name;
        this.durability = durability;
        this.defense = defense;
        this.enchantability = enchantability;
    }

    @Override
    public int getDurabilityForType(ArmorItem.Type type) {
        return durability;
    }

    @Override
    public int getDefenseForType(ArmorItem.Type type) {
        return type == ArmorItem.Type.HELMET ? defense : 0;
    }

    @Override
    public int getEnchantmentValue() {
        return enchantability;
    }

    @Override
    public SoundEvent getEquipSound() {
        return SoundEvents.ARMOR_EQUIP_LEATHER;
    }

    @Override
    public Ingredient getRepairIngredient() {
        return Ingredient.of(ModTags.Items.FAIRY_FLOWERS);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public float getToughness() {
        return 0.0F;
    }

    @Override
    public float getKnockbackResistance() {
        return 0.0F;
    }
}
