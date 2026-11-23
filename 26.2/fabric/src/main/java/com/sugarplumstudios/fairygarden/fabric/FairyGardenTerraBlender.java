package com.sugarplumstudios.fairygarden.fabric;

import com.sugarplumstudios.fairygarden.FairyGarden;
import com.sugarplumstudios.fairygarden.config.FairyConfig;
import com.sugarplumstudios.fairygarden.worldgen.FairyGardenRegion;
import terrablender.api.Regions;
import terrablender.api.TerraBlenderApi;

/** TerraBlender's Fabric entrypoint ("terrablender" in fabric.mod.json): adds the Fairy Garden overworld region. */
public final class FairyGardenTerraBlender implements TerraBlenderApi {
    @Override
    public void onTerraBlenderInitialized() {
        // TerraBlender may call this before the mod's main entrypoint has read the config.
        FairyConfig.loadCommon();
        int weight = FairyConfig.REGION_WEIGHT.get();
        if (weight > 0 && FairyGardenRegion.anyBiomeEnabled()) {
            Regions.register(new FairyGardenRegion(weight));
            FairyGarden.LOGGER.info("Registered the Fairy Garden overworld region with weight {}", weight);
        } else {
            FairyGarden.LOGGER.info("Fairy Garden biomes are disabled in the config; no region registered");
        }
    }
}
