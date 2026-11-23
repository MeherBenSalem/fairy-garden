package com.sugarplumstudios.fairygarden.client.render;

import com.mojang.datafixers.util.Pair;
import com.sugarplumstudios.fairygarden.FairyGarden;
import com.sugarplumstudios.fairygarden.entity.BoatWood;
import com.sugarplumstudios.fairygarden.entity.ModBoat;
import com.sugarplumstudios.fairygarden.entity.ModChestBoat;
import net.minecraft.client.model.BoatModel;
import net.minecraft.client.model.ChestBoatModel;
import net.minecraft.client.model.ListModel;
import net.minecraft.client.renderer.entity.BoatRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.vehicle.Boat;

import java.util.EnumMap;
import java.util.Map;

/** Renders Fairy Garden boats with their own wood textures, reusing vanilla's boat models. */
public class ModBoatRenderer extends BoatRenderer {
    private final Map<BoatWood, Pair<ResourceLocation, ListModel<Boat>>> models = new EnumMap<>(BoatWood.class);

    public ModBoatRenderer(EntityRendererProvider.Context context, boolean chest) {
        super(context, chest);
        for (BoatWood wood : BoatWood.values()) {
            ResourceLocation texture = FairyGarden.id("textures/entity/" + (chest ? "chest_boat/" : "boat/") + wood.id() + ".png");
            ListModel<Boat> model = chest
                    ? new ChestBoatModel(context.bakeLayer(ModModelLayers.boat(wood, true)))
                    : new BoatModel(context.bakeLayer(ModModelLayers.boat(wood, false)));
            models.put(wood, Pair.of(texture, model));
        }
    }

    @Override
    public Pair<ResourceLocation, ListModel<Boat>> getModelWithLocation(Boat boat) {
        if (boat instanceof ModBoat modBoat) {
            return models.get(modBoat.getWood());
        }
        if (boat instanceof ModChestBoat modChestBoat) {
            return models.get(modChestBoat.getWood());
        }
        return super.getModelWithLocation(boat);
    }
}
