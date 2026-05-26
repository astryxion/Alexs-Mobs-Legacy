package com.github.alexthe666.alexsmobs.client.model;

import com.github.alexthe666.alexsmobs.entity.EntitySkunk;
import com.github.alexthe666.citadel.client.model.AdvancedEntityModel;
import com.github.alexthe666.citadel.client.model.AdvancedModelBox;
import com.google.common.collect.ImmutableList;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

public class ModelSkunk extends AdvancedEntityModel<EntitySkunk> {
    private final AdvancedModelBox root;
    private final AdvancedModelBox body;
    private final AdvancedModelBox leftLeg;
    private final AdvancedModelBox rightLeg;
    private final AdvancedModelBox leftArm;
    private final AdvancedModelBox rightArm;
    private final AdvancedModelBox tail;
    private final AdvancedModelBox head;

    public ModelSkunk() {
        textureWidth = 64;
        textureHeight = 64;

        root = new AdvancedModelBox(this, "root");
        root.setRotationPoint(0.0F, 24.0F, 0.0F);

        body = new AdvancedModelBox(this, "body");
        body.setRotationPoint(0.0F, -3.0F, 0.0F);
        root.addChild(body);
        body.setTextureOffset(0, 0).func_228303_a_(-3.5F, -4.0F, -4.5F, 7.0F, 6.0F, 9.0F, 0.0F, false);

        leftLeg = new AdvancedModelBox(this, "leftLeg");
        leftLeg.setRotationPoint(4.0F, 2.0F, 4.0F);
        body.addChild(leftLeg);
        setRotationAngle(leftLeg, 0.0F, -0.7418F, 0.0F);
        leftLeg.setTextureOffset(0, 33).func_228303_a_(-1.0F, -1.0F, -3.0F, 2.0F, 2.0F, 4.0F, 0.0F, false);

        rightLeg = new AdvancedModelBox(this, "rightLeg");
        rightLeg.setRotationPoint(-4.0F, 2.0F, 4.0F);
        body.addChild(rightLeg);
        setRotationAngle(rightLeg, 0.0F, 0.7418F, 0.0F);
        rightLeg.setTextureOffset(0, 33).func_228303_a_(-1.0F, -1.0F, -3.0F, 2.0F, 2.0F, 4.0F, 0.0F, true);

        leftArm = new AdvancedModelBox(this, "leftArm");
        leftArm.setRotationPoint(3.5F, 2.0F, -3.0F);
        body.addChild(leftArm);
        setRotationAngle(leftArm, 0.0F, -0.5672F, 0.0F);
        leftArm.setTextureOffset(32, 31).func_228303_a_(-1.0F, -1.0F, -3.0F, 2.0F, 2.0F, 4.0F, 0.0F, false);

        rightArm = new AdvancedModelBox(this, "rightArm");
        rightArm.setRotationPoint(-3.5F, 2.0F, -3.0F);
        body.addChild(rightArm);
        setRotationAngle(rightArm, 0.0F, 0.5672F, 0.0F);
        rightArm.setTextureOffset(32, 31).func_228303_a_(-1.0F, -1.0F, -3.0F, 2.0F, 2.0F, 4.0F, 0.0F, true);

        tail = new AdvancedModelBox(this, "tail");
        tail.setRotationPoint(0.0F, -1.0F, 4.5F);
        body.addChild(tail);
        tail.setTextureOffset(0, 16).func_228303_a_(-3.0F, -10.0F, 0.0F, 6.0F, 12.0F, 4.0F, 0.0F, false);
        tail.setTextureOffset(21, 16).func_228303_a_(-3.0F, -10.0F, 4.0F, 6.0F, 7.0F, 5.0F, 0.0F, false);

        head = new AdvancedModelBox(this, "head");
        head.setRotationPoint(0.0F, 0.0F, -5.5F);
        body.addChild(head);
        head.setTextureOffset(24, 0).func_228303_a_(-3.0F, -2.0F, -3.0F, 6.0F, 4.0F, 4.0F, 0.0F, false);
        head.setTextureOffset(21, 29).func_228303_a_(-2.0F, 0.0F, -6.0F, 4.0F, 2.0F, 3.0F, 0.0F, false);
        this.updateDefaultPose();
    }

    public Iterable<AdvancedModelBox> getAllParts() {
        return ImmutableList.of(root, body, leftArm, rightArm, leftLeg, rightLeg, tail, head);
    }

    public void setRotationAngles(EntitySkunk entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.resetToDefaultPose();
        float idleSpeed = 0.1F;
        float idleDegree = 0.15F;
        float walkSpeed = 1.25F;
        float walkDegree = 0.5F;
        float partialTicks = ageInTicks - entity.ticksExisted;
        float sprayProgress = entity.prevSprayProgress + (entity.sprayProgress - entity.prevSprayProgress) * partialTicks;
        float legsStill = Math.max(sprayProgress * 0.2F, limbSwingAmount);
        progressRotationPrev(leftArm, sprayProgress, (float) Math.toRadians(80F), 0, 0, 5F);
        progressRotationPrev(rightArm, sprayProgress, (float) Math.toRadians(80F), 0, 0, 5F);
        progressRotationPrev(leftLeg, sprayProgress, (float) Math.toRadians(100F), 0, 0, 5F);
        progressRotationPrev(rightLeg, sprayProgress, (float) Math.toRadians(100F), 0, 0, 5F);
        progressRotationPrev(tail, sprayProgress, (float) Math.toRadians(30F), 0, 0, 5F);
        progressPositionPrev(body, sprayProgress, 0, -2.4F, 0, 5F);
        progressPositionPrev(tail, sprayProgress, 0, -2F, -1F, 5F);
        this.walk(body, 0.5F, 0.2F, true, 4F, 0F, ageInTicks, sprayProgress * 0.2F);
        this.swing(body, 0.5F, 0.2F, true, 1.5F, 0F, ageInTicks, sprayProgress * 0.2F);
        this.walk(head, 0.5F, 0.2F, false, 4F, 0F, ageInTicks, sprayProgress * 0.2F);
        this.swing(head, 0.5F, 0.2F, false, 1.5F, 0F, ageInTicks, sprayProgress * 0.2F);
        this.walk(leftArm, 0.5F, 0.2F, false, 4F, 0F, ageInTicks, sprayProgress * 0.2F);
        this.swing(leftArm, 0.5F, 0.2F, false, 1.5F, 0F, ageInTicks, sprayProgress * 0.2F);
        this.walk(rightArm, 0.5F, 0.2F, false, 4F, 0F, ageInTicks, sprayProgress * 0.2F);
        this.swing(rightArm, 0.5F, 0.2F, false, 1.5F, 0F, ageInTicks, sprayProgress * 0.2F);
        this.walk(leftLeg, 0.5F, 0.2F, false, 4F, 0F, ageInTicks, sprayProgress * 0.2F);
        this.swing(leftLeg, 0.5F, 0.2F, false, 1.5F, 0F, ageInTicks, sprayProgress * 0.2F);
        this.walk(rightLeg, 0.5F, 0.2F, false, 4F, 0F, ageInTicks, sprayProgress * 0.2F);
        this.swing(rightLeg, 0.5F, 0.2F, false, 1.5F, 0F, ageInTicks, sprayProgress * 0.2F);
        this.flap(tail, 0.5F, 0.5F, false, 2.5F, 0F, ageInTicks, sprayProgress * 0.2F);
        this.walk(tail, idleSpeed, idleDegree, false, 1F, 0F, ageInTicks, 1);
        progressRotationPrev(leftArm, Math.min(legsStill, 0.5F), 0, (float) Math.toRadians(30F), 0, 0.5F);
        progressRotationPrev(rightArm, Math.min(legsStill, 0.5F), 0, (float) Math.toRadians(-30F), 0, 0.5F);
        progressRotationPrev(leftLeg, Math.min(legsStill, 0.5F), 0, (float) Math.toRadians(40F), 0, 0.5F);
        progressRotationPrev(rightLeg, Math.min(legsStill, 0.5F), 0, (float) Math.toRadians(-40F), 0, 0.5F);
        progressPositionPrev(head, Math.min(legsStill, 0.5F), 0, -1F, 0, 0.5F);
        this.swing(body, walkSpeed, walkDegree * 0.5F, false, 3F, 0F, limbSwing, limbSwingAmount);
        this.swing(head, walkSpeed, walkDegree * 0.5F, true, 2F, 0F, limbSwing, limbSwingAmount);
        this.swing(tail, walkSpeed, walkDegree * 0.5F, false, 4F, 0F, limbSwing, limbSwingAmount);
        this.walk(tail, walkSpeed, walkDegree * 0.2F, true, 2F, 0.3F, limbSwing, limbSwingAmount);
        this.walk(leftArm, walkSpeed, walkDegree * 1.2F, true, -2.5F, -0.2F, limbSwing, limbSwingAmount);
        this.walk(rightArm, walkSpeed, walkDegree * 1.2F, false, -2.5F, 0.2F, limbSwing, limbSwingAmount);
        this.walk(rightLeg, walkSpeed, walkDegree * 1.2F, true, -2.5F, -0.2F, limbSwing, limbSwingAmount);
        this.walk(leftLeg, walkSpeed, walkDegree * 1.2F, false, -2.5F, 0.2F, limbSwing, limbSwingAmount);
        this.flap(body, walkSpeed, walkSpeed * 0.3F, false, -1, 0, limbSwing, limbSwingAmount);
        this.flap(rightLeg, walkSpeed, walkSpeed * 0.3F, true, -1, 0, limbSwing, limbSwingAmount);
        this.flap(leftLeg, walkSpeed, walkSpeed * 0.3F, true, -1, 0, limbSwing, limbSwingAmount);
        this.flap(rightArm, walkSpeed, walkSpeed * 0.3F, true, -1, 0, limbSwing, limbSwingAmount);
        this.flap(leftArm, walkSpeed, walkSpeed * 0.3F, true, -1, 0, limbSwing, limbSwingAmount);
        this.flap(head, walkSpeed, walkSpeed * 0.3F, true, -1, 0, limbSwing, limbSwingAmount);
        this.flap(tail, walkSpeed, walkSpeed * 0.2F, true, -1, 0, limbSwing, limbSwingAmount);
        this.faceTarget(netHeadYaw, headPitch, 1.2F, head);
        float leftLegS = (float) (Math.sin(limbSwing * walkSpeed - 2.5F) * limbSwingAmount * walkDegree - limbSwingAmount * walkDegree);
        float rightLegS = (float) (Math.sin(-(limbSwing * walkSpeed) + 2.5F) * limbSwingAmount * walkDegree - limbSwingAmount * walkDegree);
        this.leftArm.rotationPointY += 3 * leftLegS;
        this.rightArm.rotationPointY += 3 * rightLegS;
        this.rightArm.rotationPointZ += 1F * leftLegS;
        this.leftArm.rotationPointZ += 1F * rightLegS;
        this.leftLeg.rotationPointY += 3 * leftLegS;
        this.rightLeg.rotationPointY += 3 * rightLegS;
        this.leftLeg.rotationPointZ += 1F * leftLegS;
        this.rightLeg.rotationPointZ += 1F * rightLegS;
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
        setRotationAngles((EntitySkunk) entityIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
    }

    @Override
    public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5) {
        EntitySkunk skunk = (EntitySkunk) entity;
        if (skunk.isChild()) {
            this.head.setScale(1.5F, 1.5F, 1.5F);
            net.minecraft.client.renderer.GlStateManager.pushMatrix();
            net.minecraft.client.renderer.GlStateManager.scale(0.65F, 0.65F, 0.65F);
            net.minecraft.client.renderer.GlStateManager.translate(0.0D, 0.815D, 0.125D);
            getParts().forEach(part -> part.render(f5));
            net.minecraft.client.renderer.GlStateManager.popMatrix();
        } else {
            this.head.setScale(1F, 1F, 1F);
            getParts().forEach(part -> part.render(f5));
        }
    }
}
