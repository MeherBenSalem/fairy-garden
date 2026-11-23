package com.sugarplumstudios.fairygarden.client.render;

import com.sugarplumstudios.fairygarden.FairyGarden;
import com.sugarplumstudios.fairygarden.entity.BoatWood;
import net.minecraft.client.model.geom.ModelLayerLocation;

public final class ModModelLayers {
    public static final ModelLayerLocation BUTTERFLY = new ModelLayerLocation(FairyGarden.id("butterfly"), "main");
    public static final ModelLayerLocation MOON_WISP = new ModelLayerLocation(FairyGarden.id("moon_wisp"), "main");

    public static ModelLayerLocation boat(BoatWood wood, boolean chest) {
        return new ModelLayerLocation(FairyGarden.id((chest ? "chest_boat/" : "boat/") + wood.id()), "main");
    }

    private ModModelLayers() {
    }
}
