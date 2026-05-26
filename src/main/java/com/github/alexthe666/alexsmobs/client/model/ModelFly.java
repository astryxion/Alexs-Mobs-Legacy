package com.github.alexthe666.alexsmobs.client.model;

import net.minecraft.entity.Entity;

import com.github.alexthe666.alexsmobs.entity.EntityFly;
import com.github.alexthe666.citadel.client.model.AdvancedEntityModel;
import com.github.alexthe666.citadel.client.model.AdvancedModelBox;
import com.google.common.collect.ImmutableList;
import net.minecraft.client.model.ModelRenderer;

public class ModelFly extends AdvancedEntityModel<EntityFly> {
    private final AdvancedModelBox root;
    private final AdvancedModelBox body;
    private final AdvancedModelBox legs;
    private final AdvancedModelBox left_wing;
    private final AdvancedModelBox right_wing;
    private final AdvancedModelBox mouth;

    public ModelFly() {
        textureWidth = 32;
        textureHeight = 32;

        root = new AdvancedModelBox(this);
        root.setRotationPoint(0.0F, 24.0F, 0.0F);


        body = new AdvancedModelBox(this);
        body.setRotationPoint(0.0F, -3.0F, 0.0F);
        root.addChild(body);
        body.setTextureOffset(0, 0).func_228303_a_(-2.0F, -2.0F, -3.0F, 4.0F, 4.0F, 6.0F, 0.0F, false);

        legs = new AdvancedModelBox(this);
        legs.setRotationPoint(0.0F, 2.0F, -2.0F);
        body.addChild(legs);
        legs.setTextureOffset(0, 11).func_228303_a_(-1.5F, 0.0F, 0.0F, 3.0F, 1.0F, 5.0F, 0.0F, false);

        left_wing = new AdvancedModelBox(this);
        left_wing.setRotationPoint(1.0F, -2.0F, -1.0F);
        body.addChild(left_wing);
        left_wing.setTextureOffset(12, 11).func_228303_a_(0.0F, 0.0F, -1.0F, 4.0F, 0.0F, 3.0F, 0.0F, false);

        right_wing = new AdvancedModelBox(this);
        right_wing.setRotationPoint(-1.0F, -2.0F, -1.0F);
        body.addChild(right_wing);
        right_wing.setTextureOffset(12, 11).func_228303_a_(-4.0F, 0.0F, -1.0F, 4.0F, 0.0F, 3.0F, 0.0F, true);

        mouth = new AdvancedModelBox(this);
        mouth.setRotationPoint(0.0F, 0.0F, -3.0F);
        body.addChild(mouth);
        mouth.setTextureOffset(15, 16).func_228303_a_(0.0F, 0.0F, -1.0F, 0.0F, 4.0F, 2.0F, 0.0F, false);
        this.updateDefaultPose();
    }

    @Override
    public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale, Entity entityIn) {
        setRotationAngles((EntityFly) entityIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
    }

    public void setRotationAngles(EntityFly entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.resetToDefaultPose();
        float flySpeed = 1.4F;
        float flyDegree = 0.8F;
        float idleSpeed = 1.4F;
        float idleDegree = 0.8F;
        this.walk(mouth, idleSpeed * 0.2F, idleDegree * 0.1F, false, -1, 0.2F, ageInTicks, 1);
        this.flap(mouth, idleSpeed * 0.2F, idleDegree * 0.05F, false, -2, 0F, ageInTicks, 1);
        double motionSq = entityIn.motionX * entityIn.motionX + entityIn.motionY * entityIn.motionY + entityIn.motionZ * entityIn.motionZ;
        boolean flag = entityIn.onGround && motionSq < 1.0E-7D;
        if (flag) {
            this.left_wing.rotateAngleZ = (float) Math.toRadians(-35);
            this.right_wing.rotateAngleZ = (float) Math.toRadians(35);
            this.swing(legs, flySpeed * 0.6F, flyDegree * 0.2F, false, 1, 0F, limbSwing, limbSwingAmount);
        } else {
            this.flap(left_wing, flySpeed * 1.3F, flyDegree, true, 0, 0.2F, ageInTicks, 1);
            this.flap(right_wing, flySpeed * 1.3F, flyDegree, false, 0, 0.2F, ageInTicks, 1);
            this.walk(legs, flySpeed * 0.2F, flyDegree * 0.2F, false, 1, 0.2F, ageInTicks, 1);
        }
    }
    @Override
    public void render(net.minecraft.entity.Entity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
        for (net.minecraft.client.model.ModelRenderer part : this.getParts()) {
            part.render(scale);
        }
    }


        public Iterable<ModelRenderer> getParts() {
        return ImmutableList.of(root);
    }

    @Override
    public Iterable<AdvancedModelBox> getAllParts() {
        return ImmutableList.of(root, body, left_wing, right_wing, legs, mouth);
    }

    public void setRotationAngle(AdvancedModelBox advancedModelBox, float x, float y, float z) {
        advancedModelBox.rotateAngleX = x;
        advancedModelBox.rotateAngleY = y;
        advancedModelBox.rotateAngleZ = z;
    }
}
