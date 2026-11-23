package com.sugarplumstudios.fairygarden.datagen;

import com.sugarplumstudios.fairygarden.FairyGarden;
import com.sugarplumstudios.fairygarden.registry.ModParticles;
import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.common.data.ParticleDescriptionProvider;

public class ModParticleDescriptionProvider extends ParticleDescriptionProvider {
    public ModParticleDescriptionProvider(PackOutput output, ExistingFileHelper files) {
        super(output, files);
    }

    @Override
    protected void addDescriptions() {
        spriteSet(ModParticles.BLOSSOM_PETAL.get(), FairyGarden.id("blossom_petal"), 4, false);
        spriteSet(ModParticles.SNOW_PETAL.get(), FairyGarden.id("snow_petal"), 4, false);
        spriteSet(ModParticles.BUTTERFLY.get(), FairyGarden.id("butterfly"), 3, false);
        spriteSet(ModParticles.FAIRY_SPARKLE.get(), FairyGarden.id("sparkle"), 4, false);
        spriteSet(ModParticles.MOON_GLIMMER.get(), FairyGarden.id("glimmer"), 2, false);
        spriteSet(ModParticles.POLLEN.get(), FairyGarden.id("pollen"), 2, false);
    }
}
