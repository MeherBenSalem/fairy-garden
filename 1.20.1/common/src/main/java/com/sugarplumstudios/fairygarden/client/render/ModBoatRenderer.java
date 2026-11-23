package com.sugarplumstudios.fairygarden.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.datafixers.util.Pair;
import com.mojang.math.Axis;
import com.sugarplumstudios.fairygarden.FairyGarden;
import com.sugarplumstudios.fairygarden.entity.BoatWood;
import com.sugarplumstudios.fairygarden.entity.ModBoat;
import com.sugarplumstudios.fairygarden.entity.ModChestBoat;
import net.minecraft.client.model.BoatModel;
import net.minecraft.client.model.ChestBoatModel;
import net.minecraft.client.model.ListModel;
import net.minecraft.client.model.WaterPatchModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.vehicle.Boat;
import org.joml.Quaternionf;

import java.util.EnumMap;
import java.util.Map;

/**
 * Renders Fairy Garden boats with their own wood textures, reusing vanilla's boat models. Vanilla's BoatRenderer picks
 * its model from the vanilla boat type only, so this is vanilla's render code with the model chosen by our wood.
 */
public class ModBoatRenderer extends EntityRenderer<Boat> {
    private final Map<BoatWood, Pair<ResourceLocation, ListModel<Boat>>> models = new EnumMap<>(BoatWood.class);

    public ModBoatRenderer(EntityRendererProvider.Context context, boolean chest) {
        super(context);
        this.shadowRadius = 0.8F;
        for (BoatWood wood : BoatWood.values()) {
            ResourceLocation texture = FairyGarden.id("textures/entity/" + (chest ? "chest_boat/" : "boat/") + wood.id() + ".png");
            ListModel<Boat> model = chest
                    ? new ChestBoatModel(context.bakeLayer(ModModelLayers.boat(wood, true)))
                    : new BoatModel(context.bakeLayer(ModModelLayers.boat(wood, false)));
            models.put(wood, Pair.of(texture, model));
        }
    }

    private Pair<ResourceLocation, ListModel<Boat>> modelFor(Boat boat) {
        if (boat instanceof ModChestBoat chestBoat) {
            return models.get(chestBoat.getWood());
        }
        if (boat instanceof ModBoat modBoat) {
            return models.get(modBoat.getWood());
        }
        return models.get(BoatWood.BLOSSOM);
    }

    @Override
    public void render(Boat boat, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        poseStack.translate(0.0F, 0.375F, 0.0F);
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - entityYaw));
        float hurt = (float) boat.getHurtTime() - partialTicks;
        float damage = Math.max(boat.getDamage() - partialTicks, 0.0F);
        if (hurt > 0.0F) {
            poseStack.mulPose(Axis.XP.rotationDegrees(Mth.sin(hurt) * hurt * damage / 10.0F * (float) boat.getHurtDir()));
        }
        float bubble = boat.getBubbleAngle(partialTicks);
        if (!Mth.equal(bubble, 0.0F)) {
            poseStack.mulPose(new Quaternionf().setAngleAxis(bubble * ((float) Math.PI / 180.0F), 1.0F, 0.0F, 1.0F));
        }
        Pair<ResourceLocation, ListModel<Boat>> pair = modelFor(boat);
        ListModel<Boat> model = pair.getSecond();
        poseStack.scale(-1.0F, -1.0F, 1.0F);
        poseStack.mulPose(Axis.YP.rotationDegrees(90.0F));
        model.setupAnim(boat, partialTicks, 0.0F, -0.1F, 0.0F, 0.0F);
        VertexConsumer consumer = buffer.getBuffer(model.renderType(pair.getFirst()));
        model.renderToBuffer(poseStack, consumer, packedLight, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
        if (!boat.isUnderWater() && model instanceof WaterPatchModel waterPatch) {
            waterPatch.waterPatch().render(poseStack, buffer.getBuffer(RenderType.waterMask()), packedLight, OverlayTexture.NO_OVERLAY);
        }
        poseStack.popPose();
        super.render(boat, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(Boat boat) {
        return modelFor(boat).getFirst();
    }
}
