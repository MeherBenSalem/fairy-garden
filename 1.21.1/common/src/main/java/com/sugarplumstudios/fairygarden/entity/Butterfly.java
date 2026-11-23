package com.sugarplumstudios.fairygarden.entity;

import com.sugarplumstudios.fairygarden.worldgen.ModBiomes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.biome.Biome;
import org.jetbrains.annotations.Nullable;

/**
 * Butterflies flutter low over meadows by day, drifting from flower to flower. Six pastel colourways;
 * which ones appear depends on the biome.
 */
public class Butterfly extends FlutteringCreature {
    public static final int PINK = 0, LAVENDER = 1, CREAM = 2, MINT = 3, SKY = 4, WHITE = 5;
    public static final String[] VARIANT_NAMES = {"pink", "lavender", "cream", "mint", "sky", "white"};
    private static final EntityDataAccessor<Integer> DATA_VARIANT = SynchedEntityData.defineId(Butterfly.class, EntityDataSerializers.INT);

    public Butterfly(EntityType<? extends Butterfly> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 2.0);
    }

    public static boolean checkSpawnRules(EntityType<Butterfly> type, ServerLevelAccessor level, MobSpawnType reason, BlockPos pos,
                                          RandomSource random) {
        return pos.getY() >= level.getSeaLevel() && level.getLevel().isDay() && level.isEmptyBlock(pos) && level.canSeeSky(pos);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_VARIANT, PINK);
    }

    public int getVariant() {
        return Math.floorMod(entityData.get(DATA_VARIANT), VARIANT_NAMES.length);
    }

    public void setVariant(int variant) {
        entityData.set(DATA_VARIANT, Math.floorMod(variant, VARIANT_NAMES.length));
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType reason,
                                        @Nullable SpawnGroupData spawnData) {
        setVariant(pickVariant(level.getBiome(blockPosition()), random));
        return super.finalizeSpawn(level, difficulty, reason, spawnData);
    }

    private static int pickVariant(Holder<Biome> biome, RandomSource random) {
        int[] palette = biome.unwrapKey().map(Butterfly::paletteFor).orElse(null);
        if (palette == null) {
            return random.nextInt(VARIANT_NAMES.length);
        }
        return palette[random.nextInt(palette.length)];
    }

    @Nullable
    private static int[] paletteFor(ResourceKey<Biome> key) {
        if (key == ModBiomes.LAVENDER_FIELDS) return new int[]{LAVENDER, LAVENDER, WHITE, SKY};
        if (key == ModBiomes.BLOSSOM_VALLEY) return new int[]{PINK, PINK, WHITE, CREAM};
        if (key == ModBiomes.STRAWBERRY_MEADOW) return new int[]{CREAM, PINK, MINT, WHITE};
        if (key == ModBiomes.FAIRY_GLADE) return new int[]{MINT, SKY, LAVENDER, PINK};
        if (key == ModBiomes.MOONLIGHT_FOREST) return new int[]{SKY, LAVENDER, WHITE};
        return null;
    }

    @Override
    @Nullable
    protected BlockPos pickTarget() {
        BlockPos origin = blockPosition();
        for (int i = 0; i < 10; i++) {
            BlockPos probe = origin.offset(random.nextInt(11) - 5, random.nextInt(5) - 3, random.nextInt(11) - 5);
            if (level().getBlockState(probe).is(BlockTags.FLOWERS)) {
                return probe.above();
            }
        }
        return null;
    }

    @Override
    protected int minHover() {
        return 1;
    }

    @Override
    protected int maxHover() {
        return 3;
    }

    @Override
    protected double cruiseSpeed() {
        return level().isDay() ? 0.22 : 0.12;
    }

    @Override
    protected float getSoundVolume() {
        return 0.0F;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("Variant", getVariant());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        setVariant(tag.getInt("Variant"));
    }
}
