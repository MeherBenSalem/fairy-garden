package com.sugarplumstudios.fairygarden.item;

import com.sugarplumstudios.fairygarden.FairyGarden;
import com.sugarplumstudios.fairygarden.registry.ModTags;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.EquipmentAssets;

import java.util.EnumMap;
import java.util.Map;

/**
 * Fairy Garden's armor material. A plain record: the worn look comes from the equipment asset
 * (assets/fairygarden/equipment/flower_crown.json) and repairs from the fairy flowers item tag.
 */
public final class ModArmorMaterials {
    /** A woven crown of fairy flowers: about as sturdy as a leather cap, and much prettier. */
    public static final ArmorMaterial FLOWER_CROWN = new ArmorMaterial(5, defense(1), 18, SoundEvents.ARMOR_EQUIP_LEATHER,
            0.0F, 0.0F, ModTags.Items.FAIRY_FLOWERS, ResourceKey.create(EquipmentAssets.ROOT_ID, FairyGarden.id("flower_crown")));
    /** 1.20.1 gave the crown a flat durability of 60 (not scaled by slot). */
    public static final int FLOWER_CROWN_DURABILITY = 60;

    private static Map<ArmorType, Integer> defense(int helmet) {
        Map<ArmorType, Integer> map = new EnumMap<>(ArmorType.class);
        for (ArmorType type : ArmorType.values()) {
            map.put(type, type == ArmorType.HELMET ? helmet : 0);
        }
        return map;
    }

    private ModArmorMaterials() {
    }
}
