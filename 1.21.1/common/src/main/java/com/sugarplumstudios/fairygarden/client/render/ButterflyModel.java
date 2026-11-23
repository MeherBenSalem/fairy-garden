package com.sugarplumstudios.fairygarden.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.sugarplumstudios.fairygarden.entity.Butterfly;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.Mth;

/** A slender body, two antennae and a pair of broad flat wings that beat quickly while flying. */
public class ButterflyModel extends EntityModel<Butterfly> {
    private final ModelPart body;
    private final ModelPart leftWing;
    private final ModelPart rightWing;

    public ButterflyModel(ModelPart root) {
        super(RenderType::entityCutoutNoCull);
        this.body = root.getChild("body");
        this.leftWing = body.getChild("left_wing");
        this.rightWing = body.getChild("right_wing");
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition body = root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-0.5F, -0.5F, -3.0F, 1.0F, 1.0F, 6.0F), PartPose.offset(0.0F, 21.5F, 0.0F));
        body.addOrReplaceChild("antennae", CubeListBuilder.create()
                .texOffs(16, 0).addBox(-1.5F, -2.0F, -4.0F, 3.0F, 2.0F, 0.0F), PartPose.ZERO);
        body.addOrReplaceChild("left_wing", CubeListBuilder.create()
                .texOffs(0, 8).addBox(0.0F, 0.0F, -4.0F, 7.0F, 0.0F, 8.0F), PartPose.offset(0.5F, -0.2F, 0.0F));
        body.addOrReplaceChild("right_wing", CubeListBuilder.create()
                .texOffs(0, 16).addBox(-7.0F, 0.0F, -4.0F, 7.0F, 0.0F, 8.0F), PartPose.offset(-0.5F, -0.2F, 0.0F));
        return LayerDefinition.create(mesh, 32, 32);
    }

    @Override
    public void setupAnim(Butterfly entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        float flap = Mth.cos(ageInTicks * 1.4F + entity.getId()) * 0.9F;
        leftWing.zRot = -0.35F - flap;
        rightWing.zRot = 0.35F + flap;
        body.y = 21.5F + Mth.sin(ageInTicks * 0.25F) * 0.6F;
        body.xRot = -0.15F;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int light, int overlay, int color) {
        body.render(poseStack, buffer, light, overlay, color);
    }
}
