package com.sugarplumstudios.fairygarden.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;

/** A single petal that tumbles, spins and sways as it drifts to the ground. */
public class PetalParticle extends TextureSheetParticle {
    private final float swayPhase;
    private final float swaySpeed;
    private float spin;

    protected PetalParticle(ClientLevel level, double x, double y, double z, double xd, double yd, double zd, SpriteSet sprites) {
        super(level, x, y, z);
        pickSprite(sprites);
        lifetime = 160 + random.nextInt(80);
        gravity = 0.0F;
        friction = 1.0F;
        quadSize = 0.045F + random.nextFloat() * 0.035F;
        swayPhase = random.nextFloat() * Mth.TWO_PI;
        swaySpeed = 0.04F + random.nextFloat() * 0.05F;
        spin = (random.nextBoolean() ? 1 : -1) * (0.03F + random.nextFloat() * 0.06F);
        roll = random.nextFloat() * Mth.TWO_PI;
        oRoll = roll;
        this.xd = xd + (random.nextFloat() - 0.5F) * 0.01F;
        this.yd = Math.min(yd, 0.0) - 0.01;
        this.zd = zd + (random.nextFloat() - 0.5F) * 0.01F;
    }

    @Override
    public void tick() {
        xo = x;
        yo = y;
        zo = z;
        if (age++ >= lifetime) {
            remove();
            return;
        }
        float t = age * swaySpeed + swayPhase;
        xd = xd * 0.96 + Mth.cos(t) * 0.0025;
        zd = zd * 0.96 + Mth.sin(t * 0.7F) * 0.0025;
        yd = Math.max(yd - 0.0006, -0.035);
        oRoll = roll;
        roll += spin;
        move(xd, yd, zd);
        if (onGround) {
            // Rest briefly on the ground, then fade out.
            xd = zd = 0;
            spin = 0;
            if (lifetime - age > 20) {
                age = lifetime - 20;
            }
        }
        if (lifetime - age < 20) {
            alpha = (lifetime - age) / 20.0F;
        }
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Provider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double xd, double yd, double zd) {
            return new PetalParticle(level, x, y, z, xd, yd, zd, sprites);
        }
    }
}
