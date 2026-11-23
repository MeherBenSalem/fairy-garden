package com.sugarplumstudios.fairygarden.item;

import com.sugarplumstudios.fairygarden.FairyGarden;
import com.sugarplumstudios.fairygarden.registry.ModTags;
import net.minecraft.core.Holder;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.EnumMap;
import java.util.List;

/**
 * Fairy Garden's armor material. ArmorMaterial is a registry record since 1.20.5; the crown's material lives only in
 * its item (a direct holder), the way 1.20.1 kept it inside the item too.
 */
public final class ModArmorMaterials {
    /** A woven crown of fairy flowers: about as sturdy as a leather cap, and much prettier. */
    public static final Holder<ArmorMaterial> FLOWER_CROWN = Holder.direct(new ArmorMaterial(defense(1), 18,
            SoundEvents.ARMOR_EQUIP_LEATHER, () -> Ingredient.of(ModTags.Items.FAIRY_FLOWERS),
            List.of(new ArmorMaterial.Layer(FairyGarden.id("flower_crown"))), 0.0F, 0.0F));
    /** 1.20.1 gave the crown a flat durability of 60 (not scaled by slot). */
    public static final int FLOWER_CROWN_DURABILITY = 60;

    private static EnumMap<ArmorItem.Type, Integer> defense(int helmet) {
        EnumMap<ArmorItem.Type, Integer> map = new EnumMap<>(ArmorItem.Type.class);
        for (ArmorItem.Type type : ArmorItem.Type.values()) {
            map.put(type, type == ArmorItem.Type.HELMET ? helmet : 0);
        }
        return map;
    }

    private ModArmorMaterials() {
    }
}
