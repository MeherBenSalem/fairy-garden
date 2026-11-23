package com.sugarplumstudios.fairygarden.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.sugarplumstudios.fairygarden.FairyGarden;
import com.sugarplumstudios.fairygarden.entity.Butterfly;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.Identifier;

public class ButterflyRenderer extends MobRenderer<Butterfly, ButterflyRenderState, ButterflyModel> {
    private static final Identifier[] TEXTURES = new Identifier[Butterfly.VARIANT_NAMES.length];

    static {
        for (int i = 0; i < TEXTURES.length; i++) {
            TEXTURES[i] = FairyGarden.id("textures/entity/butterfly/" + Butterfly.VARIANT_NAMES[i] + ".png");
        }
    }

    public ButterflyRenderer(EntityRendererProvider.Context context) {
        super(context, new ButterflyModel(context.bakeLayer(ModModelLayers.BUTTERFLY)), 0.1F);
    }

    @Override
    public ButterflyRenderState createRenderState() {
        return new ButterflyRenderState();
    }

    @Override
    public void extractRenderState(Butterfly entity, ButterflyRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.variant = entity.getVariant();
        state.entityId = entity.getId();
    }

    @Override
    public Identifier getTextureLocation(ButterflyRenderState state) {
        return TEXTURES[state.variant];
    }

    @Override
    protected void scale(ButterflyRenderState state, PoseStack poseStack) {
        poseStack.scale(0.8F, 0.8F, 0.8F);
    }
}
