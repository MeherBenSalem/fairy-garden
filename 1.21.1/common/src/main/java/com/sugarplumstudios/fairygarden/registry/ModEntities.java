package com.sugarplumstudios.fairygarden.registry;

import net.minecraft.core.registries.Registries;
import com.sugarplumstudios.fairygarden.platform.Services;
import com.sugarplumstudios.fairygarden.FairyGarden;
import com.sugarplumstudios.fairygarden.entity.Butterfly;
import com.sugarplumstudios.fairygarden.entity.ModBoat;
import com.sugarplumstudios.fairygarden.entity.ModChestBoat;
import com.sugarplumstudios.fairygarden.entity.MoonWisp;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import com.sugarplumstudios.fairygarden.platform.RegistryEntry;

public final class ModEntities {

    public static final RegistryEntry<EntityType<ModBoat>> BOAT = Services.PLATFORM.register(Registries.ENTITY_TYPE, "boat",
            () -> EntityType.Builder.<ModBoat>of(ModBoat::new, MobCategory.MISC).sized(1.375F, 0.5625F).clientTrackingRange(10)
                    .build(FairyGarden.id("boat").toString()));
    public static final RegistryEntry<EntityType<ModChestBoat>> CHEST_BOAT = Services.PLATFORM.register(Registries.ENTITY_TYPE, "chest_boat",
            () -> EntityType.Builder.<ModChestBoat>of(ModChestBoat::new, MobCategory.MISC).sized(1.375F, 0.5625F).clientTrackingRange(10)
                    .build(FairyGarden.id("chest_boat").toString()));

    public static final RegistryEntry<EntityType<Butterfly>> BUTTERFLY = Services.PLATFORM.register(Registries.ENTITY_TYPE, "butterfly",
            () -> EntityType.Builder.of(Butterfly::new, MobCategory.AMBIENT).sized(0.45F, 0.35F).clientTrackingRange(8)
                    .build(FairyGarden.id("butterfly").toString()));
    public static final RegistryEntry<EntityType<MoonWisp>> MOON_WISP = Services.PLATFORM.register(Registries.ENTITY_TYPE, "moon_wisp",
            () -> EntityType.Builder.of(MoonWisp::new, MobCategory.AMBIENT).sized(0.4F, 0.4F).clientTrackingRange(8)
                    .build(FairyGarden.id("moon_wisp").toString()));

    /** Loads the class, which registers everything above. */
    public static void init() {
    }

    private ModEntities() {
    }
}
