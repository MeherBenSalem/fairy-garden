package com.sugarplumstudios.fairygarden.entity;

import com.sugarplumstudios.fairygarden.registry.ModEntities;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

public class ModBoat extends Boat {
    private static final EntityDataAccessor<Integer> DATA_WOOD = SynchedEntityData.defineId(ModBoat.class, EntityDataSerializers.INT);

    public ModBoat(EntityType<? extends Boat> type, Level level) {
        super(type, level);
    }

    public ModBoat(Level level, double x, double y, double z) {
        this(ModEntities.BOAT.get(), level);
        setPos(x, y, z);
        xo = x;
        yo = y;
        zo = z;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_WOOD, BoatWood.BLOSSOM.ordinal());
    }

    public BoatWood getWood() {
        return BoatWood.values()[entityData.get(DATA_WOOD)];
    }

    public void setWood(BoatWood wood) {
        entityData.set(DATA_WOOD, wood.ordinal());
    }

    @Override
    public Item getDropItem() {
        return getWood().woodSet().boat.get();
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putString("FairyWood", getWood().id());
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        setWood(BoatWood.byId(tag.getString("FairyWood")));
    }
}
