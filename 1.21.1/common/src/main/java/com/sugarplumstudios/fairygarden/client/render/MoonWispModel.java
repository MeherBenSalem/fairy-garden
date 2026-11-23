package com.sugarplumstudios.fairygarden.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.sugarplumstudios.fairygarden.entity.MoonWisp;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.Mth;

/** A glowing core inside a slowly turning halo, with two translucent moth wings. */
public class MoonWispModel extends EntityModel<MoonWisp> {
    private final ModelPart root;
    private final ModelPart halo;
    private final ModelPart leftWing;
    private final ModelPart rightWing;

    public MoonWispModel(ModelPart root) {
        super(RenderType::entityTranslucent);
        this.root = root.getChild("root");
        this.halo = this.root.getChild("halo");
        this.leftWing = this.root.getChild("left_wing");
        this.rightWing = this.root.getChild("right_wing");
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
    public void setupAnim(MoonWisp entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        root.y = 20.0F + Mth.sin(ageInTicks * 0.12F) * 1.2F;
        halo.yRot = ageInTicks * 0.05F;
        halo.xRot = ageInTicks * 0.03F;
        float flap = Mth.cos(ageInTicks * 0.9F) * 0.6F;
        leftWing.zRot = -0.3F - flap;
        rightWing.zRot = 0.3F + flap;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int light, int overlay, int color) {
        root.render(poseStack, buffer, light, overlay, color);
    }
}
