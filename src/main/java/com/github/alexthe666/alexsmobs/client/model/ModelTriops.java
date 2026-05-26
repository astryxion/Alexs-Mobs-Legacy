package com.github.alexthe666.alexsmobs.client.model;

import com.github.alexthe666.alexsmobs.entity.EntityTriops;
import com.github.alexthe666.citadel.client.model.AdvancedEntityModel;
import com.github.alexthe666.citadel.client.model.AdvancedModelBox;
import com.google.common.collect.ImmutableList;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;

public class ModelTriops extends AdvancedEntityModel<EntityTriops> {
    private final AdvancedModelBox root;
    private final AdvancedModelBox body;
    private final AdvancedModelBox leftAntenna;
    private final AdvancedModelBox rightAntenna;
    private final AdvancedModelBox leftLegs;
    private final AdvancedModelBox rightLegs;
    private final AdvancedModelBox tail1;
    private final AdvancedModelBox tail2;
    private final AdvancedModelBox leftTailFlipper;
    private final AdvancedModelBox rightTailFlipper;

    public ModelTriops() {
        textureWidth = 64;
        textureHeight = 64;

        root = new AdvancedModelBox(this);
        root.setRotationPoint(0.0F, 24.0F, 0.0F);
        body = new AdvancedModelBox(this);
        body.setRotationPoint(0.0F, -2.0F, -2.0F);
        root.addChild(body);
        body.setTextureOffset(0, 0).func_228303_a_(-3.5F, -1.1F, -3.5F, 7.0F, 3.0F, 7.0F, 0.0F, false);

        leftAntenna = new AdvancedModelBox(this);
        leftAntenna.setRotationPoint(3.5F, 2.0F, -2.0F);
        body.addChild(leftAntenna);
        leftAntenna.setTextureOffset(15, 21).func_228303_a_(-1.0F, -1.0F, 0.0F, 4.0F, 1.0F, 3.0F, 0.0F, false);

        rightAntenna = new AdvancedModelBox(this);
        rightAntenna.setRotationPoint(-3.5F, 2.0F, -2.0F);
        body.addChild(rightAntenna);
        rightAntenna.setTextureOffset(15, 21).func_228303_a_(-3.0F, -1.0F, 0.0F, 4.0F, 1.0F, 3.0F, 0.0F, true);

        leftLegs = new AdvancedModelBox(this);
        leftLegs.setRotationPoint(0.0F, 1.9F, 0.0F);
        body.addChild(leftLegs);
        setRotationAngle(leftLegs, 0.0F, 0.0F, 0.0873F);
        leftLegs.setTextureOffset(22, 0).func_228303_a_(0.0F, 0.0F, -2.0F, 3.0F, 0.0F, 4.0F, 0.0F, false);

        rightLegs = new AdvancedModelBox(this);
        rightLegs.setRotationPoint(0.0F, 1.9F, 0.0F);
        body.addChild(rightLegs);
        setRotationAngle(rightLegs, 0.0F, 0.0F, -0.0873F);
        rightLegs.setTextureOffset(22, 0).func_228303_a_(-3.0F, 0.0F, -2.0F, 3.0F, 0.0F, 4.0F, 0.0F, true);

        tail1 = new AdvancedModelBox(this);
        tail1.setRotationPoint(0.0F, 1.0F, 2.3F);
        body.addChild(tail1);
        tail1.setTextureOffset(0, 18).func_228303_a_(-1.5F, -1.0F, -0.8F, 3.0F, 2.0F, 4.0F, 0.0F, false);
        tail1.setTextureOffset(22, 11).func_228303_a_(1.5F, 1.0F, -0.8F, 2.0F, 0.0F, 4.0F, 0.0F, false);
        tail1.setTextureOffset(22, 11).func_228303_a_(-3.5F, 1.0F, -0.8F, 2.0F, 0.0F, 4.0F, 0.0F, true);

        tail2 = new AdvancedModelBox(this);
        tail2.setRotationPoint(0.0F, 0.2F, 3.2F);
        tail1.addChild(tail2);
        tail2.setTextureOffset(11, 14).func_228303_a_(-1.5F, -1.2F, 0.0F, 3.0F, 2.0F, 4.0F, 0.0F, false);

        leftTailFlipper = new AdvancedModelBox(this);
        leftTailFlipper.setRotationPoint(0.7F, -1.0F, 4.8F);
        tail2.addChild(leftTailFlipper);
        setRotationAngle(leftTailFlipper, 0.2618F, 0.2618F, 0.0F);
        leftTailFlipper.setTextureOffset(0, 11).func_228303_a_(0.0F, 0.0F, -1.0F, 1.0F, 0.0F, 6.0F, 0.0F, false);

        rightTailFlipper = new AdvancedModelBox(this);
        rightTailFlipper.setRotationPoint(-0.7F, -1.0F, 4.8F);
        tail2.addChild(rightTailFlipper);
        setRotationAngle(rightTailFlipper, 0.2618F, -0.2618F, 0.0F);
        rightTailFlipper.setTextureOffset(0, 11).func_228303_a_(-1.0F, 0.0F, -1.0F, 1.0F, 0.0F, 6.0F, 0.0F, true);
        this.updateDefaultPose();
    }

    

    @Override
    public Iterable<AdvancedModelBox> getAllParts() {
        return ImmutableList.of(root, body, leftAntenna, rightAntenna, leftLegs, rightLegs, tail1, tail2, leftTailFlipper, rightTailFlipper);
    }

    public void setRotationAngles(EntityTriops entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch){
        this.resetToDefaultPose();
        float idleSpeed = 0.5F;
        float idleDegree = 0.2F;
        float swimSpeed = 0.65F;
        float swimDegree = 0.25F;
        float partialTick = ageInTicks - entity.ticksExisted;
        float landProgress = entity.prevOnLandProgress + (entity.onLandProgress - entity.prevOnLandProgress) * partialTick;
        float swimAmount = 1F - landProgress * 0.2F;
        float swimRot = swimAmount * (entity.prevSwimRot + (entity.swimRot - entity.prevSwimRot) * partialTick);
        float yaw = entity.prevRenderYawOffset + (entity.renderYawOffset - entity.prevRenderYawOffset) * partialTick;
        float tail1Rot = MathHelper.wrapDegrees(entity.prevTail1Yaw + (entity.tail1Yaw - entity.prevTail1Yaw) * partialTick - yaw) * 0.35F;
        float tail2Rot = MathHelper.wrapDegrees(entity.prevTail2Yaw + (entity.tail2Yaw - entity.prevTail2Yaw) * partialTick - yaw) * 0.35F;
        progressRotationPrev(body, landProgress, 0, 0, (float)Math.toRadians(-180), 5F);
        progressPositionPrev(body, limbSwingAmount, 0, -3, 0, 1F);
        progressRotationPrev(leftAntenna, 1F - limbSwingAmount, 0, (float)Math.toRadians(20), 0, 1F);
        progressRotationPrev(rightAntenna, 1F - limbSwingAmount, 0, (float)Math.toRadians(-20), 0, 1F);
        this.body.rotateAngleX += headPitch * ((float) Math.PI / 180F) * swimAmount;
        this.body.rotateAngleZ += (float)Math.toRadians(swimRot);
        this.swing(rightAntenna, idleSpeed, idleDegree, false, 1F, -0.2F, ageInTicks, 1);
        this.swing(leftAntenna, idleSpeed, idleDegree, true, 1F, -0.2F, ageInTicks, 1);
        this.walk(leftTailFlipper, idleSpeed, idleDegree, false, 3F, 0.1F, ageInTicks, 1);
        this.walk(rightTailFlipper, idleSpeed, idleDegree, false, 3F, 0.1F, ageInTicks, 1);
        this.flap(leftLegs, idleSpeed * 3, idleDegree, true, 1F, -0.2F, ageInTicks, 1);
        this.flap(rightLegs, idleSpeed * 3, idleDegree, false, 1F, -0.2F, ageInTicks, 1);
        this.walk(body, swimSpeed, swimDegree, false, 2.5F, 0F, limbSwing, limbSwingAmount);
        this.walk(tail1, swimSpeed, swimDegree, false, 1.5F, 0F, limbSwing, limbSwingAmount);
        this.walk(tail2, swimSpeed, swimDegree * 1.5F, false, 0.5F, 0F, limbSwing, limbSwingAmount);
        this.walk(leftTailFlipper, swimSpeed, swimDegree, false, 0F, -0.1F, limbSwing, limbSwingAmount);
        this.walk(rightTailFlipper, swimSpeed, swimDegree, false, 0F, -0.1F, limbSwing, limbSwingAmount);
        this.walk(tail1, idleSpeed, idleDegree, false, 0, -0.1F, ageInTicks, landProgress * 0.2F);
        this.walk(tail2, idleSpeed, idleDegree * 1.5F, false, 0, -0.3F, ageInTicks, landProgress * 0.2F);
        this.walk(leftTailFlipper, idleSpeed, idleDegree, false, 0F, -0.3F, ageInTicks, landProgress * 0.2F);
        this.walk(rightTailFlipper, idleSpeed, idleDegree, false, 0F, -0.3F, ageInTicks, landProgress * 0.2F);
        tail1.rotateAngleY += (float)Math.toRadians(tail1Rot);
        tail2.rotateAngleY += (float)Math.toRadians(tail2Rot);
    }

    public void setRotationAngle(AdvancedModelBox box, float x, float y, float z) {
        box.rotateAngleX = x;
        box.rotateAngleY = y;
        box.rotateAngleZ = z;
    }

    public Iterable<ModelRenderer> getParts() {
        return ImmutableList.of(root);
    }

    @Override
    public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor, Entity entityIn) {
        setRotationAngles((EntityTriops) entityIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
    }

    @Override
    public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5) {
        getParts().forEach(part -> part.render(f5));
    }
}