package com.sugarplumstudios.fairygarden.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.util.RandomSource;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;

/**
 * Soft magical light particles: fairy sparkles (twinkling stars), moon glimmers (firefly-like motes) and pollen.
 * They glow at full brightness and fade in and out.
 */
public class GlowParticle extends SingleQuadParticle {
    /** This particle's own random source (the one the provider hands over). */
    private final RandomSource rng;
    public enum Style {
        SPARKLE(new int[]{0xFFD6EC, 0xE4D2FF, 0xFFF4D6, 0xCFF7E6, 0xD4ECFF, 0xFFFFFF}, 24, 24, 0.05F, true, 0.002),
        GLIMMER(new int[]{0x9FC6FF, 0xC4B2FF, 0xE6F2FF, 0x8FE6FF}, 60, 60, 0.035F, true, 0.0),
        POLLEN(new int[]{0xFFF0A8, 0xFFE38A, 0xFFF7D1}, 50, 40, 0.025F, false, 0.0006);

        final int[] colors;
        final int minLife;
        final int extraLife;
        final float size;
        final boolean fullBright;
        final double lift;

        Style(int[] colors, int minLife, int extraLife, float size, boolean fullBright, double lift) {
            this.colors = colors;
            this.minLife = minLife;
            this.extraLife = extraLife;
            this.size = size;
            this.fullBright = fullBright;
            this.lift = lift;
        }
    }

    private final Style style;
    private final SpriteSet sprites;
    private final float baseSize;
    private final float pulseOffset;

    protected GlowParticle(ClientLevel level, double x, double y, double z, double xd, double yd, double zd, SpriteSet sprites, Style style,
                           RandomSource random) {
        super(level, x, y, z, sprites.first());
        this.rng = random;
        this.style = style;
        this.sprites = sprites;
        int color = style.colors[rng.nextInt(style.colors.length)];
        setColor(((color >> 16) & 0xFF) / 255.0F, ((color >> 8) & 0xFF) / 255.0F, (color & 0xFF) / 255.0F);
        lifetime = style.minLife + rng.nextInt(style.extraLife + 1);
        baseSize = style.size * (0.7F + rng.nextFloat() * 0.6F);
        quadSize = baseSize;
        gravity = 0.0F;
        friction = 0.92F;
        hasPhysics = style != Style.SPARKLE;
        pulseOffset = rng.nextFloat() * Mth.TWO_PI;
        this.xd = xd + (rng.nextFloat() - 0.5F) * 0.01F;
        this.yd = yd + style.lift;
        this.zd = zd + (rng.nextFloat() - 0.5F) * 0.01F;
        alpha = 0.0F;
        setSprite(sprites.get(age, lifetime));
    }

    @Override
    public void tick() {
        super.tick();
        if (removed) {
            return;
        }
        setSprite(sprites.get(age, lifetime));
        float progress = (float) age / lifetime;
        float fade = Math.min(1.0F, Math.min(progress * 5.0F, (1.0F - progress) * 4.0F));
        float pulse = 0.75F + 0.25F * Mth.sin(age * 0.25F + pulseOffset);
        alpha = fade * (style == Style.GLIMMER ? pulse : 1.0F);
        quadSize = baseSize * (style == Style.SPARKLE ? (0.6F + 0.4F * Mth.sin(progress * Mth.PI)) : 1.0F);
        if (style == Style.GLIMMER) {
            xd += (rng.nextFloat() - 0.5F) * 0.004F;
            yd += (rng.nextFloat() - 0.5F) * 0.003F;
            zd += (rng.nextFloat() - 0.5F) * 0.004F;
        } else if (style == Style.POLLEN) {
            xd += Mth.sin(age * 0.1F + pulseOffset) * 0.0008F;
            yd += style.lift * 0.1;
        }
    }

    @Override
    protected int getLightCoords(float partialTick) {
        return style.fullBright ? 0xF000F0 : super.getLightCoords(partialTick);
    }

    @Override
    protected Layer getLayer() {
        return Layer.TRANSLUCENT;
    }

    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;
        private final Style style;

        public Provider(SpriteSet sprites, Style style) {
            this.sprites = sprites;
            this.style = style;
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double xd, double yd, double zd,
                                       RandomSource random) {
            return new GlowParticle(level, x, y, z, xd, yd, zd, sprites, style, random);
        }
    }
}
