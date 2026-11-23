package com.sugarplumstudios.fairygarden.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ambient.AmbientCreature;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

/**
 * Shared flight for tiny ambient creatures: they meander between random points a little above the ground,
 * never take fall damage and don't push or trigger anything. Movement runs server-side like a bat's.
 */
public abstract class FlutteringCreature extends AmbientCreature {
    @Nullable
    private BlockPos target;

    protected FlutteringCreature(EntityType<? extends FlutteringCreature> type, Level level) {
        super(type, level);
    }

    /** Lowest and highest preferred cruising height above the ground. */
    protected abstract int minHover();

    protected abstract int maxHover();

    /** Horizontal and vertical speed caps. */
    protected abstract double cruiseSpeed();

    /** Lets subclasses steer toward something interesting (a flower, a moonflower...). */
    @Nullable
    protected BlockPos pickTarget() {
        return null;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    protected void doPush(Entity entity) {
    }

    @Override
    protected void pushEntities() {
    }

    @Override
    public boolean causeFallDamage(float distance, float multiplier, DamageSource source) {
        return false;
    }

    @Override
    protected void checkFallDamage(double y, boolean onGround, BlockState state, BlockPos pos) {
    }

    @Override
    public boolean isIgnoringBlockTriggers() {
        return true;
    }

    @Override
    public void tick() {
        super.tick();
        // Counter most of gravity so the creature drifts instead of dropping.
        setDeltaMovement(getDeltaMovement().multiply(1.0, 0.6, 1.0));
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        Level level = level();
        if (target != null && (!level.isEmptyBlock(target) || target.getY() <= level.getMinBuildHeight())) {
            target = null;
        }
        if (target == null || random.nextInt(50) == 0 || target.closerToCenterThan(position(), 1.2)) {
            BlockPos interesting = random.nextInt(3) == 0 ? pickTarget() : null;
            if (interesting != null) {
                target = interesting;
            } else {
                int x = getBlockX() + random.nextInt(11) - 5;
                int z = getBlockZ() + random.nextInt(11) - 5;
                int ground = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
                int y = ground + minHover() + random.nextInt(maxHover() - minHover() + 1);
                target = new BlockPos(x, Math.min(y, getBlockY() + 3), z);
            }
        }
        double speed = cruiseSpeed();
        double dx = target.getX() + 0.5 - getX();
        double dy = target.getY() + 0.1 - getY();
        double dz = target.getZ() + 0.5 - getZ();
        Vec3 motion = getDeltaMovement();
        Vec3 steered = motion.add(
                (Math.signum(dx) * speed - motion.x) * 0.1,
                (Math.signum(dy) * speed * 1.4 - motion.y) * 0.1,
                (Math.signum(dz) * speed - motion.z) * 0.1);
        setDeltaMovement(steered);
        float yaw = (float) (Mth.atan2(steered.z, steered.x) * (180.0 / Math.PI)) - 90.0F;
        setYRot(getYRot() + Mth.wrapDegrees(yaw - getYRot()) * 0.5F);
        zza = 0.5F;
    }
}
