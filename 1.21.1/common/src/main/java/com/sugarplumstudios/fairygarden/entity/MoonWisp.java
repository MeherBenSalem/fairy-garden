package com.sugarplumstudios.fairygarden.entity;

import com.sugarplumstudios.fairygarden.block.MoonflowerBlock;
import com.sugarplumstudios.fairygarden.registry.ModParticles;
import com.sugarplumstudios.fairygarden.registry.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import org.jetbrains.annotations.Nullable;

/**
 * A rare, glowing little spirit of the night. Moon wisps drift above moonlit forests and fairy glades, are drawn
 * to open moonflowers, trail moonlight behind them and fade away at sunrise. They leave a pinch of fairy dust.
 */
public class MoonWisp extends FlutteringCreature {
    /** Only wisps that appeared on their own fade at dawn; spawn-egg and named wisps stay. */
    private boolean fadesAtDawn;

    public MoonWisp(EntityType<? extends MoonWisp> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 4.0);
    }

    public static boolean checkSpawnRules(EntityType<MoonWisp> type, ServerLevelAccessor level, MobSpawnType reason, BlockPos pos,
                                          RandomSource random) {
        return pos.getY() >= level.getSeaLevel() && !level.getLevel().isDay() && level.isEmptyBlock(pos) && level.canSeeSky(pos)
                && random.nextInt(3) == 0;
    }

    @Override
    public void tick() {
        super.tick();
        Level level = level();
        if (level.isClientSide && random.nextInt(3) == 0) {
            level.addParticle(ModParticles.MOON_GLIMMER.get(), getRandomX(0.6), getY() + 0.1 + random.nextDouble() * 0.3,
                    getRandomZ(0.6), 0.0, -0.01, 0.0);
        }
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType reason,
                                        @Nullable SpawnGroupData spawnData) {
        fadesAtDawn = reason == MobSpawnType.NATURAL || reason == MobSpawnType.CHUNK_GENERATION;
        return super.finalizeSpawn(level, difficulty, reason, spawnData);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("FadesAtDawn", fadesAtDawn);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        fadesAtDawn = tag.getBoolean("FadesAtDawn");
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        if (fadesAtDawn && !hasCustomName() && level() instanceof ServerLevel server && server.isDay()
                && server.canSeeSky(blockPosition()) && random.nextInt(300) == 0) {
            server.sendParticles(ModParticles.FAIRY_SPARKLE.get(), getX(), getY() + 0.2, getZ(), 12, 0.25, 0.25, 0.25, 0.02);
            discard();
        }
    }

    @Override
    @Nullable
    protected BlockPos pickTarget() {
        BlockPos origin = blockPosition();
        for (int i = 0; i < 10; i++) {
            BlockPos probe = origin.offset(random.nextInt(13) - 6, random.nextInt(7) - 5, random.nextInt(13) - 6);
            if (level().getBlockState(probe).getBlock() instanceof MoonflowerBlock) {
                return probe.above(2);
            }
        }
        return null;
    }

    @Override
    protected int minHover() {
        return 2;
    }

    @Override
    protected int maxHover() {
        return 6;
    }

    @Override
    protected double cruiseSpeed() {
        return 0.16;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.MOON_WISP_AMBIENT.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.MOON_WISP_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.MOON_WISP_HURT.get();
    }

    @Override
    public int getAmbientSoundInterval() {
        return 160;
    }

    @Override
    protected float getSoundVolume() {
        return 0.5F;
    }
}
