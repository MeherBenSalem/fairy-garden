package com.sugarplumstudios.fairygarden.registry;

import com.sugarplumstudios.fairygarden.platform.Services;
import com.sugarplumstudios.fairygarden.FairyGarden;
import com.sugarplumstudios.fairygarden.worldgen.structure.FlatJigsawStructure;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.structure.StructureType;
import com.sugarplumstudios.fairygarden.platform.RegistryEntry;

public final class ModStructureTypes {

    /** A jigsaw structure that only generates on dry, fairly level ground. */
    public static final RegistryEntry<StructureType<FlatJigsawStructure>> FLAT_JIGSAW = Services.PLATFORM.register(Registries.STRUCTURE_TYPE, "flat_jigsaw",
            () -> () -> FlatJigsawStructure.CODEC);

    /** Loads the class, which registers everything above. */
    public static void init() {
    }

    private ModStructureTypes() {
    }
}
