package com.sugarplumstudios.fairygarden.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.util.RandomSource;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;

/** A tiny pastel butterfly that flaps its wings and wanders in loops before fluttering away. */
public class ButterflyParticle extends SingleQuadParticle {
    /** This particle's own random source (the one the provider hands over). */
    private final RandomSource rng;
    private static final int[] COLORS = {0xF7A8C8, 0xCBB0F2, 0xFFEFC2, 0xAEEFD2, 0xAFD8FF, 0xFFFFFF};
    private final SpriteSet sprites;
    private float heading;
    private final float flapOffset;

    protected ButterflyParticle(ClientLevel level, double x, double y, double z, SpriteSet sprites, RandomSource random) {
        super(level, x, y, z, sprites.get(0, 3));
        this.rng = random;
        this.sprites = sprites;
        int color = COLORS[rng.nextInt(COLORS.length)];
        setColor(((color >> 16) & 0xFF) / 255.0F, ((color >> 8) & 0xFF) / 255.0F, (color & 0xFF) / 255.0F);
        lifetime = 90 + rng.nextInt(90);
        quadSize = 0.09F + rng.nextFloat() * 0.04F;
        gravity = 0.0F;
        hasPhysics = true;
        heading = rng.nextFloat() * Mth.TWO_PI;
        flapOffset = rng.nextFloat() * 4;
        alpha = 0.0F;
        setSprite(sprites.get(0, 3));
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
        heading += (rng.nextFloat() - 0.5F) * 0.5F;
        double speed = 0.035;
        xd = xd * 0.8 + Mth.cos(heading) * speed * 0.2;
        zd = zd * 0.8 + Mth.sin(heading) * speed * 0.2;
        yd = yd * 0.8 + (Mth.sin((age + flapOffset) * 0.4F) * 0.012 + 0.0015);
        move(xd, yd, zd);
        int frame = (int) ((age + flapOffset) / 2) % 4;
        setSprite(sprites.get(frame == 3 ? 1 : frame, 2));
        alpha = Math.min(1.0F, Math.min(age / 10.0F, (lifetime - age) / 15.0F));
    }

    @Override
    protected Layer getLayer() {
        return Layer.TRANSLUCENT;
    }

    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Provider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double xd, double yd, double zd,
                                       RandomSource random) {
            return new ButterflyParticle(level, x, y, z, sprites, random);
        }
    }
}
