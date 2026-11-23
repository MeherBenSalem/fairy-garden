package com.sugarplumstudios.fairygarden.registry;

import net.minecraft.core.registries.Registries;
import com.sugarplumstudios.fairygarden.platform.Services;
import com.sugarplumstudios.fairygarden.FairyGarden;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import com.sugarplumstudios.fairygarden.platform.RegistryEntry;

/** Anonymous subclasses reach SimpleParticleType's protected constructor on every loader. */
public final class ModParticles {

    /** Pink blossom petals tumbling from blossom leaves and drifting through Blossom Valley. */
    public static final RegistryEntry<SimpleParticleType> BLOSSOM_PETAL = Services.PLATFORM.register(Registries.PARTICLE_TYPE, "blossom_petal", () -> new SimpleParticleType(false) { });
    /** White-cream petals from snow blossom trees. */
    public static final RegistryEntry<SimpleParticleType> SNOW_PETAL = Services.PLATFORM.register(Registries.PARTICLE_TYPE, "snow_petal", () -> new SimpleParticleType(false) { });
    /** Tiny wing-flapping butterflies in pastel colours (purely visual). */
    public static final RegistryEntry<SimpleParticleType> BUTTERFLY = Services.PLATFORM.register(Registries.PARTICLE_TYPE, "butterfly", () -> new SimpleParticleType(false) { });
    /** Twinkling four-point fairy sparkles. */
    public static final RegistryEntry<SimpleParticleType> FAIRY_SPARKLE = Services.PLATFORM.register(Registries.PARTICLE_TYPE, "fairy_sparkle", () -> new SimpleParticleType(false) { });
    /** Soft glowing motes of moonlight, like fireflies. */
    public static final RegistryEntry<SimpleParticleType> MOON_GLIMMER = Services.PLATFORM.register(Registries.PARTICLE_TYPE, "moon_glimmer", () -> new SimpleParticleType(false) { });
    /** Floating golden pollen. */
    public static final RegistryEntry<SimpleParticleType> POLLEN = Services.PLATFORM.register(Registries.PARTICLE_TYPE, "pollen", () -> new SimpleParticleType(false) { });

    /** Loads the class, which registers everything above. */
    public static void init() {
    }

    private ModParticles() {
    }
}
