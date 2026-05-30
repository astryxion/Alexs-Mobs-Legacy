package com.github.alexthe666.alexsmobs.client.model;

import com.github.alexthe666.alexsmobs.entity.EntitySugarGlider;
import com.github.alexthe666.alexsmobs.entity.util.Maths;
import com.github.alexthe666.citadel.client.model.AdvancedEntityModel;
import com.github.alexthe666.citadel.client.model.AdvancedModelBox;
import com.google.common.collect.ImmutableList;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.EnumFacing;

public class ModelSugarGlider extends AdvancedEntityModel<EntitySugarGlider> {

    private final AdvancedModelBox root;
    private final AdvancedModelBox body;
    private final AdvancedModelBox leftArm;
    private final AdvancedModelBox rightArm;
    private final AdvancedModelBox leftLeg;
    private final AdvancedModelBox rightLeg;
    private final AdvancedModelBox tail;
    private final AdvancedModelBox head;
    private final AdvancedModelBox leftEar;
    private final AdvancedModelBox rightEar;

    public ModelSugarGlider() {
        textureWidth = 64;
        textureHeight = 64;

        root = new AdvancedModelBox(this);
        root.setRotationPoint(0.0F, 24.0F, 0.0F);

        body = new AdvancedModelBox(this);
        body.setRotationPoint(0.0F, -2.0F, -1.0F);
        root.addChild(body);
        body.setTextureOffset(0, 0).func_228303_a_(-2.0F, -1.0F, -3.0F, 4.0F, 3.0F, 7.0F, 0.0F, false);

        leftArm = new AdvancedModelBox(this);
        leftArm.setRotationPoint(1.0F, 1.0F, -3.0F);
        body.addChild(leftArm);
        setRotationAngle(leftArm, 0.0F, 0.0F, 0.20944F);
        leftArm.setTextureOffset(12, 11).func_228303_a_(-1.0F, 0.0F, -2.0F, 6.0F, 0.0F, 6.0F, 0.0F, false);

        rightArm = new AdvancedModelBox(this);
        rightArm.setRotationPoint(-1.0F, 1.0F, -3.0F);
        body.addChild(rightArm);
        setRotationAngle(rightArm, 0.0F, 0.0F, -0.20944F);
        rightArm.setTextureOffset(12, 11).func_228303_a_(-5.0F, 0.0F, -2.0F, 6.0F, 0.0F, 6.0F, 0.0F, true);

        leftLeg = new AdvancedModelBox(this);
        leftLeg.setRotationPoint(1.0F, 1.0F, 3.0F);
        body.addChild(leftLeg);
        setRotationAngle(leftLeg, 0.0F, 0.0F, 0.20944F);
        leftLeg.setTextureOffset(15, 0).func_228303_a_(-1.0F, 0.0F, -2.0F, 6.0F, 0.0F, 5.0F, 0.0F, false);

        rightLeg = new AdvancedModelBox(this);
        rightLeg.setRotationPoint(-1.0F, 1.0F, 3.0F);
        body.addChild(rightLeg);
        setRotationAngle(rightLeg, 0.0F, 0.0F, -0.20944F);
        rightLeg.setTextureOffset(15, 0).func_228303_a_(-5.0F, 0.0F, -2.0F, 6.0F, 0.0F, 5.0F, 0.0F, true);

        tail = new AdvancedModelBox(this);
        tail.setRotationPoint(0.0F, 0.0F, 4.0F);
        body.addChild(tail);
        tail.setTextureOffset(0, 11).func_228303_a_(-1.0F, -1.0F, 0.0F, 2.0F, 2.0F, 8.0F, 0.0F, false);

        head = new AdvancedModelBox(this);
        head.setRotationPoint(0.0F, 0.0F, -3.0F);
        body.addChild(head);
        head.setTextureOffset(17, 18).func_228303_a_(-2.5F, -2.0F, -4.0F, 5.0F, 4.0F, 4.0F, 0.0F, false);
        head.setTextureOffset(0, 22).func_228303_a_(-1.5F, 0.0F, -5.0F, 3.0F, 2.0F, 1.0F, 0.0F, false);

        leftEar = new AdvancedModelBox(this);
        leftEar.setRotationPoint(2.2F, -1.6F, -2.9F);
        head.addChild(leftEar);
        setRotationAngle(leftEar, 0.0F, -0.6109F, 0.0F);
        leftEar.setTextureOffset(0, 0).func_228303_a_(0.0F, -2.0F, 0.0F, 2.0F, 2.0F, 1.0F, 0.0F, false);

        rightEar = new AdvancedModelBox(this);
        rightEar.setRotationPoint(-2.2F, -1.6F, -2.9F);
        head.addChild(rightEar);
        setRotationAngle(rightEar, 0.0F, 0.6109F, 0.0F);
        rightEar.setTextureOffset(0, 0).func_228303_a_(-2.0F, -2.0F, 0.0F, 2.0F, 2.0F, 1.0F, 0.0F, true);
        this.updateDefaultPose();
    }

    @Override
    public Iterable<AdvancedModelBox> getAllParts() {
        return ImmutableList.of(root, body, head, leftArm, rightArm, leftEar, rightEar, tail, leftLeg, rightLeg);
    }

    public Iterable<ModelRenderer> getParts() {
        return ImmutableList.of(root);
    }

    @Override
    public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor, Entity entityIn) {
        setRotationAngles((EntitySugarGlider) entityIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
    }

    public void setRotationAngles(EntitySugarGlider entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.resetToDefaultPose();
        if (entityIn.isRiding()) {
            netHeadYaw = 0.0F;
            headPitch = 0.0F;
            limbSwingAmount = 0.0F;
        }
        float idleSpeed = 0.1F;
        float idleDegree = 0.25F;
        float walkSpeed = 0.9F;
        float walkDegree = 0.5F;
        float glideSpeed = 1.3F;
        float glideDegree = 0.6F;
        float partialTick = ageInTicks - entityIn.ticksExisted;
        float glideProgress = entityIn.prevGlideProgress + (entityIn.glideProgress - entityIn.prevGlideProgress) * partialTick;
        float sitProgress = entityIn.prevSitProgress + (entityIn.sitProgress - entityIn.prevSitProgress) * partialTick;
        float forageProgress = entityIn.forageProgress + (entityIn.forageProgress - entityIn.prevForageProgress) * partialTick;
        float glideSwingAmount = glideProgress * 0.2F;
        float walkSwingAmount = (1f - glideSwingAmount) * limbSwingAmount;
        progressRotationPrev(body, glideProgress, Maths.rad(-15), 0, 0, 5F);
        progressRotationPrev(tail, glideProgress, Maths.rad(12), 0, 0, 5F);
        progressRotationPrev(head, glideProgress, Maths.rad(12), 0, 0, 5F);
        progressRotationPrev(leftArm, glideProgress, 0, 0, Maths.rad(-20), 5F);
        progressRotationPrev(leftLeg, glideProgress, 0, 0, Maths.rad(-20), 5F);
        progressRotationPrev(rightArm, glideProgress, 0, 0, Maths.rad(20), 5F);
        progressRotationPrev(rightLeg, glideProgress, 0, 0, Maths.rad(20), 5F);
        progressPositionPrev(body, glideProgress, 0, -2, 2, 5F);
        progressPositionPrev(leftArm, glideProgress, 2, 0, 0, 5F);
        progressPositionPrev(rightArm, glideProgress, -2, 0, 0, 5F);
        progressPositionPrev(leftLeg, glideProgress, 2, 0, 0, 5F);
        progressPositionPrev(rightLeg, glideProgress, -2, 0, 0, 5F);
        progressRotationPrev(head, forageProgress, Maths.rad(35), 0, 0, 5F);
        progressRotationPrev(tail, forageProgress, Maths.rad(10), 0, 0, 5F);
        progressPositionPrev(head, forageProgress, 0, -1, 1, 5F);
        progressRotationPrev(body, sitProgress, Maths.rad(-170), 0, 0, 5F);
        progressRotationPrev(tail, sitProgress, Maths.rad(-50), 0, 0, 5F);
        progressRotationPrev(head, sitProgress, Maths.rad(150), 0, 0, 5F);
        progressRotationPrev(leftArm, sitProgress, 0, 0, Maths.rad(20), 5F);
        progressRotationPrev(leftLeg, sitProgress, 0, 0, Maths.rad(20), 5F);
        progressRotationPrev(rightArm, sitProgress, 0, 0, Maths.rad(-20), 5F);
        progressRotationPrev(rightLeg, sitProgress, 0, 0, Maths.rad(-20), 5F);
        progressPositionPrev(body, sitProgress, 0, 1, 1, 5F);
        progressPositionPrev(head, sitProgress, 0, 2, -2, 5F);
        this.flap(rightEar, idleSpeed, idleDegree, false, 0F, -0.05F, ageInTicks, 1);
        this.flap(leftEar, idleSpeed, idleDegree, true, 0F, -0.05F, ageInTicks, 1);
        this.swing(leftArm, walkSpeed, walkDegree, false, 1.5F, -0.2F, limbSwing, walkSwingAmount);
        this.swing(leftLeg, walkSpeed, walkDegree, true, 1.5F, -0.2F, limbSwing, walkSwingAmount);
        this.swing(rightArm, walkSpeed, walkDegree, false, 1.5F, -0.2F, limbSwing, walkSwingAmount);
        this.swing(rightLeg, walkSpeed, walkDegree, true, 1.5F, -0.2F, limbSwing, walkSwingAmount);
        this.swing(tail, walkSpeed, walkDegree, true, 0F, 0F, limbSwing, walkSwingAmount);
        this.bob(head, walkSpeed * 0.5F, walkDegree, true, limbSwing, walkSwingAmount);
        this.flap(leftArm, glideSpeed, glideDegree * 0.1F, true, 0F, -0.05F, ageInTicks, glideSwingAmount);
        this.flap(leftLeg, glideSpeed, glideDegree * 0.1F, true, 0F, -0.05F, ageInTicks, glideSwingAmount);
        this.flap(rightArm, glideSpeed, glideDegree * 0.1F, false, 0F, 0.05F, ageInTicks, glideSwingAmount);
        this.flap(rightLeg, glideSpeed, glideDegree * 0.1F, false, 0F, 0.05F, ageInTicks, glideSwingAmount);
        this.swing(head, glideSpeed * 0.2F, glideDegree * 0.4F, false, 0F, 0F, ageInTicks, glideSwingAmount);
        this.swing(body, glideSpeed * 0.2F, glideDegree * 0.4F, true, 1F, 0F, ageInTicks, glideSwingAmount);
        this.swing(tail, glideSpeed * 0.2F, glideDegree, true, -1F, 0F, ageInTicks, glideSwingAmount);
        this.bob(head, 1F, 0.6F, false, ageInTicks, forageProgress * 0.2F);
        this.swing(head, 0.5F, 0.6F, true, -1F, 0F, ageInTicks, forageProgress * 0.2F);
        if (forageProgress == 0 && entityIn.getAttachmentFacing() == EnumFacing.DOWN && !entityIn.isGliding()) {
            this.faceTarget(netHeadYaw, headPitch, 1.2F, head);
        }
    }

    @Override
    public void render(Entity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
        if (this.isChild) {
            float f = 1.35F;
            head.setScale(f, f, f);
            head.setShouldScaleChildren(true);
            GlStateManager.pushMatrix();
            GlStateManager.scale(0.5F, 0.5F, 0.5F);
            GlStateManager.translate(0.0D, 1.5D, 0.0D);
            for (ModelRenderer part : this.getParts()) {
                part.render(scale);
            }
            GlStateManager.popMatrix();
            this.head.setScale(1F, 1F, 1F);
        } else {
            this.head.setScale(1F, 1F, 1F);
            for (ModelRenderer part : this.getParts()) {
                part.render(scale);
            }
        }
    }

    public void setRotationAngle(AdvancedModelBox box, float x, float y, float z) {
        box.rotateAngleX = x;
        box.rotateAngleY = y;
        box.rotateAngleZ = z;
    }
}
