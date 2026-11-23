package com.sugarplumstudios.fairygarden.registry;

import com.sugarplumstudios.fairygarden.FairyGarden;
import com.sugarplumstudios.fairygarden.worldgen.structure.FlatJigsawStructure;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public final class ModStructureTypes {
    public static final DeferredRegister<StructureType<?>> STRUCTURE_TYPES = DeferredRegister.create(Registries.STRUCTURE_TYPE, FairyGarden.MOD_ID);

    /** A jigsaw structure that only generates on dry, fairly level ground. */
    public static final RegistryObject<StructureType<FlatJigsawStructure>> FLAT_JIGSAW = STRUCTURE_TYPES.register("flat_jigsaw",
            () -> () -> FlatJigsawStructure.CODEC);

    private ModStructureTypes() {
    }
}
