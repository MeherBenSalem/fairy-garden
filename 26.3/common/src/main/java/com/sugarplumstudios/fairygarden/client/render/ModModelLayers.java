package com.sugarplumstudios.fairygarden.client.render;

import com.sugarplumstudios.fairygarden.FairyGarden;
import net.minecraft.client.model.geom.ModelLayerLocation;

public final class ModModelLayers {
    public static final ModelLayerLocation BUTTERFLY = new ModelLayerLocation(FairyGarden.id("butterfly"), "main");
    public static final ModelLayerLocation MOON_WISP = new ModelLayerLocation(FairyGarden.id("moon_wisp"), "main");

    /**
     * The boat model layer of a wood. The vanilla boat renderer draws textures/entity/boat/&lt;wood&gt;.png (or
     * chest_boat/) from this layer's id.
     */
    public static ModelLayerLocation boat(String wood, boolean chest) {
        return new ModelLayerLocation(FairyGarden.id((chest ? "chest_boat/" : "boat/") + wood), "main");
    }

    private ModModelLayers() {
    }
}
