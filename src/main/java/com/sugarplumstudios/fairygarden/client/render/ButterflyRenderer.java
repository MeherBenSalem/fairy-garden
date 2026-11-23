package com.sugarplumstudios.fairygarden.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.sugarplumstudios.fairygarden.FairyGarden;
import com.sugarplumstudios.fairygarden.entity.Butterfly;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class ButterflyRenderer extends MobRenderer<Butterfly, ButterflyModel> {
    private static final ResourceLocation[] TEXTURES = new ResourceLocation[Butterfly.VARIANT_NAMES.length];

    static {
        for (int i = 0; i < TEXTURES.length; i++) {
            TEXTURES[i] = FairyGarden.id("textures/entity/butterfly/" + Butterfly.VARIANT_NAMES[i] + ".png");
        }
    }

    public ButterflyRenderer(EntityRendererProvider.Context context) {
        super(context, new ButterflyModel(context.bakeLayer(ModModelLayers.BUTTERFLY)), 0.1F);
    }

    @Override
    public ResourceLocation getTextureLocation(Butterfly entity) {
        return TEXTURES[entity.getVariant()];
    }

    @Override
    protected void scale(Butterfly entity, PoseStack poseStack, float partialTick) {
        poseStack.scale(0.8F, 0.8F, 0.8F);
    }
}
