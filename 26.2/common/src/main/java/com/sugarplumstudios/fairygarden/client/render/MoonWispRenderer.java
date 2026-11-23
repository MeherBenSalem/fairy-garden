package com.sugarplumstudios.fairygarden.client.render;

import com.sugarplumstudios.fairygarden.FairyGarden;
import com.sugarplumstudios.fairygarden.entity.MoonWisp;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;

/** Moon wisps always render at full brightness, so they glow in the dark. */
public class MoonWispRenderer extends MobRenderer<MoonWisp, LivingEntityRenderState, MoonWispModel> {
    private static final Identifier TEXTURE = FairyGarden.id("textures/entity/moon_wisp.png");

    public MoonWispRenderer(EntityRendererProvider.Context context) {
        super(context, new MoonWispModel(context.bakeLayer(ModModelLayers.MOON_WISP)), 0.0F);
    }

    @Override
    public LivingEntityRenderState createRenderState() {
        return new LivingEntityRenderState();
    }

    @Override
    public Identifier getTextureLocation(LivingEntityRenderState state) {
        return TEXTURE;
    }

    @Override
    protected int getBlockLightLevel(MoonWisp entity, BlockPos pos) {
        return 15;
    }
}
