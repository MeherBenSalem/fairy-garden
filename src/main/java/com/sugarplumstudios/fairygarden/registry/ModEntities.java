package com.sugarplumstudios.fairygarden.registry;

import com.sugarplumstudios.fairygarden.FairyGarden;
import com.sugarplumstudios.fairygarden.entity.Butterfly;
import com.sugarplumstudios.fairygarden.entity.ModBoat;
import com.sugarplumstudios.fairygarden.entity.ModChestBoat;
import com.sugarplumstudios.fairygarden.entity.MoonWisp;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, FairyGarden.MOD_ID);

    public static final RegistryObject<EntityType<ModBoat>> BOAT = ENTITIES.register("boat",
            () -> EntityType.Builder.<ModBoat>of(ModBoat::new, MobCategory.MISC).sized(1.375F, 0.5625F).clientTrackingRange(10)
                    .build(FairyGarden.id("boat").toString()));
    public static final RegistryObject<EntityType<ModChestBoat>> CHEST_BOAT = ENTITIES.register("chest_boat",
            () -> EntityType.Builder.<ModChestBoat>of(ModChestBoat::new, MobCategory.MISC).sized(1.375F, 0.5625F).clientTrackingRange(10)
                    .build(FairyGarden.id("chest_boat").toString()));

    public static final RegistryObject<EntityType<Butterfly>> BUTTERFLY = ENTITIES.register("butterfly",
            () -> EntityType.Builder.of(Butterfly::new, MobCategory.AMBIENT).sized(0.45F, 0.35F).clientTrackingRange(8)
                    .build(FairyGarden.id("butterfly").toString()));
    public static final RegistryObject<EntityType<MoonWisp>> MOON_WISP = ENTITIES.register("moon_wisp",
            () -> EntityType.Builder.of(MoonWisp::new, MobCategory.AMBIENT).sized(0.4F, 0.4F).clientTrackingRange(8)
                    .build(FairyGarden.id("moon_wisp").toString()));

    private ModEntities() {
    }
}
