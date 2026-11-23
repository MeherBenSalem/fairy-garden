package com.sugarplumstudios.fairygarden.client;

import com.sugarplumstudios.fairygarden.FairyGarden;
import com.sugarplumstudios.fairygarden.config.FairyConfig;
import com.sugarplumstudios.fairygarden.registry.ModParticles;
import com.sugarplumstudios.fairygarden.worldgen.ModBiomes;
import net.minecraft.client.Minecraft;
import net.minecraft.server.level.ParticleStatus;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;

/**
 * Client-side biome ambience: drifting petals in Blossom Valley, butterflies over the meadows by day, moonlight
 * motes after dark and fairy sparkles in the glades. Purely visual, cheap (a handful of particles per tick around
 * the local player only) and scaled by the client config and the vanilla particle setting. Each loader calls
 * {@link #tick()} at the end of every client tick.
 */
public final class AmbientEffects {
    private static final int RADIUS = 14;

    private AmbientEffects() {
    }

    public static void tick() {
        Minecraft minecraft = Minecraft.getInstance();
        ClientLevel level = minecraft.level;
        if (level == null || minecraft.player == null || minecraft.isPaused() || !FairyConfig.AMBIENT_PARTICLES.get()) {
            return;
        }
        ParticleStatus status = minecraft.options.particles().get();
        if (status == ParticleStatus.MINIMAL) {
            return;
        }
        double density = FairyConfig.AMBIENT_PARTICLE_DENSITY.get() * (status == ParticleStatus.DECREASED ? 0.4 : 1.0);
        BlockPos center = minecraft.player.blockPosition();
        ResourceKey<Biome> biome = level.getBiome(center).unwrapKey().orElse(null);
        if (biome == null || !biome.identifier().getNamespace().equals(FairyGarden.MOD_ID)) {
            return;
        }
        boolean night = !level.isBrightOutside();
        Vec3 eye = minecraft.player.position();
        RandomSource random = level.getRandom();

        if (biome == ModBiomes.BLOSSOM_VALLEY) {
            spawnInAir(level, random, eye, biome, ModParticles.BLOSSOM_PETAL.get(), 0.9 * density);
            spawnInAir(level, random, eye, biome, ModParticles.SNOW_PETAL.get(), 0.2 * density);
            if (!night) spawnNearGround(level, random, eye, biome, ModParticles.BUTTERFLY.get(), 0.05 * density);
            spawnNearGround(level, random, eye, biome, ModParticles.POLLEN.get(), 0.15 * density);
        } else if (biome == ModBiomes.LAVENDER_FIELDS) {
            if (!night) spawnNearGround(level, random, eye, biome, ModParticles.BUTTERFLY.get(), 0.14 * density);
            spawnNearGround(level, random, eye, biome, ModParticles.POLLEN.get(), 0.3 * density);
            if (night) spawnNearGround(level, random, eye, biome, ModParticles.MOON_GLIMMER.get(), 0.15 * density);
        } else if (biome == ModBiomes.STRAWBERRY_MEADOW) {
            if (!night) spawnNearGround(level, random, eye, biome, ModParticles.BUTTERFLY.get(), 0.07 * density);
            spawnNearGround(level, random, eye, biome, ModParticles.POLLEN.get(), 0.15 * density);
            spawnNearGround(level, random, eye, biome, ModParticles.FAIRY_SPARKLE.get(), 0.03 * density);
        } else if (biome == ModBiomes.MOONLIGHT_FOREST) {
            spawnAround(level, random, eye, biome, ModParticles.MOON_GLIMMER.get(), (night ? 0.8 : 0.2) * density);
            spawnAround(level, random, eye, biome, ModParticles.FAIRY_SPARKLE.get(), 0.05 * density);
        } else if (biome == ModBiomes.FAIRY_GLADE) {
            spawnInAir(level, random, eye, biome, ModParticles.FAIRY_SPARKLE.get(), (night ? 1.0 : 0.5) * density);
            if (night) spawnNearGround(level, random, eye, biome, ModParticles.MOON_GLIMMER.get(), 0.35 * density);
            if (!night) spawnNearGround(level, random, eye, biome, ModParticles.BUTTERFLY.get(), 0.07 * density);
            spawnNearGround(level, random, eye, biome, ModParticles.POLLEN.get(), 0.15 * density);
        }
    }

    private static int count(RandomSource random, double rate) {
        int n = (int) rate;
        return random.nextDouble() < rate - n ? n + 1 : n;
    }

    private static void spawnInAir(ClientLevel level, RandomSource random, Vec3 around, ResourceKey<Biome> biome, ParticleOptions particle,
                                   double rate) {
        for (int i = count(random, rate); i > 0; i--) {
            double x = around.x + (random.nextDouble() * 2 - 1) * RADIUS;
            double y = around.y + 1 + random.nextDouble() * 9;
            double z = around.z + (random.nextDouble() * 2 - 1) * RADIUS;
            BlockPos pos = BlockPos.containing(x, y, z);
            if (level.isEmptyBlock(pos) && level.getBiome(pos).is(biome) && level.canSeeSky(pos)) {
                level.addParticle(particle, x, y, z, 0.0, 0.0, 0.0);
            }
        }
    }

    /** Anywhere in the open air around the player, including under a forest canopy. */
    private static void spawnAround(ClientLevel level, RandomSource random, Vec3 around, ResourceKey<Biome> biome, ParticleOptions particle,
                                    double rate) {
        for (int i = count(random, rate); i > 0; i--) {
            double x = around.x + (random.nextDouble() * 2 - 1) * RADIUS;
            double y = around.y - 1 + random.nextDouble() * 6;
            double z = around.z + (random.nextDouble() * 2 - 1) * RADIUS;
            BlockPos pos = BlockPos.containing(x, y, z);
            if (level.isEmptyBlock(pos) && level.getBiome(pos).is(biome)) {
                level.addParticle(particle, x, y, z, 0.0, 0.0, 0.0);
            }
        }
    }

    private static void spawnNearGround(ClientLevel level, RandomSource random, Vec3 around, ResourceKey<Biome> biome, ParticleOptions particle,
                                        double rate) {
        for (int i = count(random, rate); i > 0; i--) {
            double x = around.x + (random.nextDouble() * 2 - 1) * RADIUS;
            double z = around.z + (random.nextDouble() * 2 - 1) * RADIUS;
            int ground = level.getHeight(Heightmap.Types.MOTION_BLOCKING, (int) Math.floor(x), (int) Math.floor(z));
            if (Math.abs(ground - around.y) > 12) {
                continue;
            }
            double y = ground + 0.3 + random.nextDouble() * 2.5;
            BlockPos pos = BlockPos.containing(x, y, z);
            if (level.isEmptyBlock(pos) && level.getBiome(pos).is(biome)) {
                level.addParticle(particle, x, y, z, 0.0, 0.0, 0.0);
            }
        }
    }
}
