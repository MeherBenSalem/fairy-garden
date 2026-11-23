package com.sugarplumstudios.fairygarden.registry;

import com.sugarplumstudios.fairygarden.entity.Butterfly;
import com.sugarplumstudios.fairygarden.entity.MoonWisp;
import com.sugarplumstudios.fairygarden.platform.RegistryEntry;
import com.sugarplumstudios.fairygarden.platform.Services;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

/** The two creatures. Each wood's boat and chest boat entity types are registered by its {@link WoodSet}. */
public final class ModEntities {

    public static final RegistryEntry<EntityType<Butterfly>> BUTTERFLY = Services.PLATFORM.register(Registries.ENTITY_TYPE, "butterfly",
            () -> EntityType.Builder.of(Butterfly::new, MobCategory.AMBIENT).sized(0.45F, 0.35F).clientTrackingRange(8)
                    .build(Keys.entity()));
    public static final RegistryEntry<EntityType<MoonWisp>> MOON_WISP = Services.PLATFORM.register(Registries.ENTITY_TYPE, "moon_wisp",
            () -> EntityType.Builder.of(MoonWisp::new, MobCategory.AMBIENT).sized(0.4F, 0.4F).clientTrackingRange(8)
                    .build(Keys.entity()));

    /** Loads the class, which registers everything above. */
    public static void init() {
    }

    private ModEntities() {
    }
}
