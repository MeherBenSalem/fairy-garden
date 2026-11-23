package com.sugarplumstudios.fairygarden.registry;

import com.sugarplumstudios.fairygarden.FairyGarden;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModParticles {
    public static final DeferredRegister<ParticleType<?>> PARTICLES = DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, FairyGarden.MOD_ID);

    /** Pink blossom petals tumbling from blossom leaves and drifting through Blossom Valley. */
    public static final RegistryObject<SimpleParticleType> BLOSSOM_PETAL = PARTICLES.register("blossom_petal", () -> new SimpleParticleType(false));
    /** White-cream petals from snow blossom trees. */
    public static final RegistryObject<SimpleParticleType> SNOW_PETAL = PARTICLES.register("snow_petal", () -> new SimpleParticleType(false));
    /** Tiny wing-flapping butterflies in pastel colours (purely visual). */
    public static final RegistryObject<SimpleParticleType> BUTTERFLY = PARTICLES.register("butterfly", () -> new SimpleParticleType(false));
    /** Twinkling four-point fairy sparkles. */
    public static final RegistryObject<SimpleParticleType> FAIRY_SPARKLE = PARTICLES.register("fairy_sparkle", () -> new SimpleParticleType(false));
    /** Soft glowing motes of moonlight, like fireflies. */
    public static final RegistryObject<SimpleParticleType> MOON_GLIMMER = PARTICLES.register("moon_glimmer", () -> new SimpleParticleType(false));
    /** Floating golden pollen. */
    public static final RegistryObject<SimpleParticleType> POLLEN = PARTICLES.register("pollen", () -> new SimpleParticleType(false));

    private ModParticles() {
    }
}
