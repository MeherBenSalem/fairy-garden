package com.sugarplumstudios.fairygarden.client.render;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.util.Mth;

/** A glowing core inside a slowly turning halo, with two translucent moth wings. */
public class MoonWispModel extends EntityModel<LivingEntityRenderState> {
    private final ModelPart core;
    private final ModelPart halo;
    private final ModelPart leftWing;
    private final ModelPart rightWing;

    public MoonWispModel(ModelPart root) {
        super(root, RenderTypes::entityTranslucent);
        this.core = root.getChild("root");
        this.halo = core.getChild("halo");
        this.leftWing = core.getChild("left_wing");
        this.rightWing = core.getChild("right_wing");
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot().addOrReplaceChild("root", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-2.0F, -2.0F, -2.0F, 4.0F, 4.0F, 4.0F), PartPose.offset(0.0F, 20.0F, 0.0F));
        root.addOrReplaceChild("halo", CubeListBuilder.create()
                .texOffs(0, 8).addBox(-3.0F, -3.0F, -3.0F, 6.0F, 6.0F, 6.0F), PartPose.ZERO);
        root.addOrReplaceChild("left_wing", CubeListBuilder.create()
                .texOffs(0, 20).addBox(0.0F, 0.0F, -2.0F, 5.0F, 0.0F, 4.0F), PartPose.offset(2.0F, -1.0F, 0.0F));
        root.addOrReplaceChild("right_wing", CubeListBuilder.create()
                .texOffs(0, 26).addBox(-5.0F, 0.0F, -2.0F, 5.0F, 0.0F, 4.0F), PartPose.offset(-2.0F, -1.0F, 0.0F));
        return LayerDefinition.create(mesh, 32, 32);
    }

    @Override
    public void setupAnim(LivingEntityRenderState state) {
        super.setupAnim(state);
        float ageInTicks = state.ageInTicks;
        core.y = 20.0F + Mth.sin(ageInTicks * 0.12F) * 1.2F;
        halo.yRot = ageInTicks * 0.05F;
        halo.xRot = ageInTicks * 0.03F;
        float flap = Mth.cos(ageInTicks * 0.9F) * 0.6F;
        leftWing.zRot = -0.3F - flap;
        rightWing.zRot = 0.3F + flap;
    }

}
