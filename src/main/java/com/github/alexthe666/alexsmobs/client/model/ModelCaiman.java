package com.github.alexthe666.alexsmobs.client.model;


import com.github.alexthe666.alexsmobs.entity.EntityCaiman;
import com.github.alexthe666.alexsmobs.entity.util.Maths;
import com.github.alexthe666.citadel.client.model.AdvancedEntityModel;
import com.github.alexthe666.citadel.client.model.AdvancedModelBox;
import com.google.common.collect.ImmutableList;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

public class ModelCaiman extends AdvancedEntityModel<EntityCaiman> {
    private final AdvancedModelBox root;
    private final AdvancedModelBox body;
    private final AdvancedModelBox leftLeg;
    private final AdvancedModelBox leftFoot;
    private final AdvancedModelBox rightLeg;
    private final AdvancedModelBox rightFoot;
    private final AdvancedModelBox leftArm;
    private final AdvancedModelBox leftHand;
    private final AdvancedModelBox rightArm;
    private final AdvancedModelBox rightHand;
    private final AdvancedModelBox tail1;
    private final AdvancedModelBox tail2;
    private final AdvancedModelBox tail3;
    private final AdvancedModelBox head;
    private final AdvancedModelBox bottomJaw;
    private final AdvancedModelBox topJaw;

    public ModelCaiman() {
        textureWidth = 128;
        textureHeight = 128;

        root = new AdvancedModelBox(this);
        root.setRotationPoint(0.0F, 24.0F, 0.0F);


        body = new AdvancedModelBox(this);
        body.setRotationPoint(0.0F, -4.0F, 0.0F);
        root.addChild(body);
        body.setTextureOffset(0, 0).func_228303_a_(-4.0F, -3.0F, -8.0F, 8.0F, 6.0F, 16.0F, 0.0F, false);

        leftLeg = new AdvancedModelBox(this);
        leftLeg.setRotationPoint(4.0F, 1.0F, 6.0F);
        body.addChild(leftLeg);
        leftLeg.setTextureOffset(56, 13).func_228303_a_(-1.0F, -1.0F, -4.0F, 3.0F, 4.0F, 5.0F, 0.0F, false);

        leftFoot = new AdvancedModelBox(this);
        leftFoot.setRotationPoint(1.0F, 3.0F, -3.0F);
        leftLeg.addChild(leftFoot);
        leftFoot.setTextureOffset(26, 55).func_228303_a_(-2.0F, -0.01F, -4.0F, 5.0F, 0.0F, 5.0F, 0.0F, false);

        rightLeg = new AdvancedModelBox(this);
        rightLeg.setRotationPoint(-4.0F, 1.0F, 6.0F);
        body.addChild(rightLeg);
        rightLeg.setTextureOffset(56, 13).func_228303_a_(-2.0F, -1.0F, -4.0F, 3.0F, 4.0F, 5.0F, 0.0F, true);

        rightFoot = new AdvancedModelBox(this);
        rightFoot.setRotationPoint(-1.0F, 3.0F, -3.0F);
        rightLeg.addChild(rightFoot);
        rightFoot.setTextureOffset(26, 55).func_228303_a_(-3.0F, -0.01F, -4.0F, 5.0F, 0.0F, 5.0F, 0.0F, true);

        leftArm = new AdvancedModelBox(this);
        leftArm.setRotationPoint(4.2F, 0.2F, -5.5F);
        body.addChild(leftArm);
        leftArm.setTextureOffset(0, 0).func_228303_a_(-1.2F, -1.2F, -1.5F, 3.0F, 5.0F, 3.0F, 0.0F, false);

        leftHand = new AdvancedModelBox(this);
        leftHand.setRotationPoint(0.3F, 3.8F, -0.5F);
        leftArm.addChild(leftHand);
        leftHand.setTextureOffset(55, 39).func_228303_a_(-2.5F, -0.01F, -4.0F, 5.0F, 0.0F, 5.0F, 0.0F, false);

        rightArm = new AdvancedModelBox(this);
        rightArm.setRotationPoint(-4.2F, 0.2F, -5.5F);
        body.addChild(rightArm);
        rightArm.setTextureOffset(0, 0).func_228303_a_(-1.8F, -1.2F, -1.5F, 3.0F, 5.0F, 3.0F, 0.0F, true);

        rightHand = new AdvancedModelBox(this);
        rightHand.setRotationPoint(-0.3F, 3.8F, -0.5F);
        rightArm.addChild(rightHand);
        rightHand.setTextureOffset(55, 39).func_228303_a_(-2.5F, -0.01F, -4.0F, 5.0F, 0.0F, 5.0F, 0.0F, true);

        tail1 = new AdvancedModelBox(this);
        tail1.setRotationPoint(0.0F, 0.0F, 7.0F);
        body.addChild(tail1);
        tail1.setTextureOffset(0, 23).func_228303_a_(-3.0F, -2.0F, 1.0F, 6.0F, 5.0F, 11.0F, 0.0F, false);
        tail1.setTextureOffset(24, 23).func_228303_a_(-2.0F, -3.0F, 7.0F, 4.0F, 1.0F, 5.0F, 0.0F, false);
        tail1.setTextureOffset(50, 28).func_228303_a_(-4.0F, -2.0F, 1.0F, 8.0F, 0.0F, 6.0F, 0.0F, false);

        tail2 = new AdvancedModelBox(this);
        tail2.setRotationPoint(0.0F, 1.0F, 12.0F);
        tail1.addChild(tail2);
        tail2.setTextureOffset(39, 13).func_228303_a_(-1.5F, -2.0F, 0.0F, 3.0F, 4.0F, 10.0F, 0.0F, false);
        tail2.setTextureOffset(43, 45).func_228303_a_(-1.5F, -4.0F, 0.0F, 3.0F, 2.0F, 10.0F, 0.0F, false);

        tail3 = new AdvancedModelBox(this);
        tail3.setRotationPoint(0.0F, 0.0F, 10.0F);
        tail2.addChild(tail3);
        tail3.setTextureOffset(15, 55).func_228303_a_(0.0F, -3.0F, 0.0F, 0.0F, 5.0F, 10.0F, 0.0F, false);

        head = new AdvancedModelBox(this);
        head.setRotationPoint(0.0F, 0.0F, -10.0F);
        body.addChild(head);
        head.setTextureOffset(0, 52).func_228303_a_(-3.5F, -5.0F, -3.0F, 7.0F, 7.0F, 5.0F, 0.0F, false);
        head.setTextureOffset(58, 0).func_228303_a_(-2.5F, -6.0F, -3.0F, 5.0F, 1.0F, 3.0F, 0.0F, false);

        bottomJaw = new AdvancedModelBox(this);
        bottomJaw.setRotationPoint(0.0F, -3.0F, -2.0F);
        head.addChild(bottomJaw);
        bottomJaw.setTextureOffset(0, 40).func_228303_a_(-3.0F, 0.0F, -10.0F, 6.0F, 2.0F, 9.0F, 0.0F, false);
        bottomJaw.setTextureOffset(22, 44).func_228303_a_(-3.0F, -1.0F, -10.0F, 6.0F, 1.0F, 9.0F, 0.0F, false);

        topJaw = new AdvancedModelBox(this);
        topJaw.setRotationPoint(0.0F, -4.0F, -3.0F);
        head.addChild(topJaw);
        topJaw.setTextureOffset(33, 0).func_228303_a_(-3.5F, -1.0F, -10.0F, 7.0F, 2.0F, 10.0F, 0.0F, false);
        topJaw.setTextureOffset(25, 30).func_228303_a_(-3.5F, 1.0F, -10.0F, 7.0F, 3.0F, 10.0F, 0.0F, false);
        this.updateDefaultPose();
    }

    public Iterable<net.minecraft.client.model.ModelRenderer> getParts() { return ImmutableList.of(root); }

    @Override
    public Iterable<AdvancedModelBox> getAllParts() {
        return ImmutableList.of(root, body, bottomJaw, head, topJaw, tail1, tail2, tail3, leftLeg, leftFoot, rightLeg, rightFoot, rightArm, rightHand, leftArm, leftHand);
    }

    public void setRotationAngles(EntityCaiman entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.resetToDefaultPose();
        float idleSpeed = 0.05F;
        float idleDegree = 0.1F;
        float walkSpeed = 1F;
        float walkDegree = 1F;
        float swimSpeed = 0.65F;
        float swimDegree = 0.65F;
        float partialTick = ageInTicks - entity.ticksExisted;
        float swimProgress = (entity.prevSwimProgress + (entity.swimProgress - entity.prevSwimProgress) * partialTick) * 0.2F;
        float grabProgress = entity.prevHoldProgress + (entity.holdProgress - entity.prevHoldProgress) * partialTick;
        float vibrateProgress = (entity.prevVibrateProgress + (entity.vibrateProgress - entity.prevVibrateProgress) * partialTick) * 0.2F;
        float sitProgress = entity.prevSitProgress + (entity.sitProgress - entity.prevSitProgress) * partialTick;
        float walkAmount = (1F - swimProgress) * limbSwingAmount;
        float swimAmount = swimProgress * limbSwingAmount;
        progressRotationPrev(rightArm, swimProgress, Maths.rad(75), 0, Maths.rad(60), 1F);
        progressRotationPrev(leftArm, swimProgress, Maths.rad(75), 0, Maths.rad(-60), 1F);
        progressRotationPrev(rightLeg, swimProgress, Maths.rad(75), 0, Maths.rad(60), 1F);
        progressRotationPrev(leftLeg, swimProgress, Maths.rad(75), 0, Maths.rad(-60), 1F);
        progressPositionPrev(head, swimAmount, 0, 2, 0, 1F);
        progressPositionPrev(head, entity.holdProgress, 0, 0, 2, 5F);
        progressPositionPrev(bottomJaw, grabProgress, 0, 1, 0, 5F);
        progressRotationPrev(topJaw, grabProgress, Maths.rad(-15), 0, 0, 5F);
        progressRotationPrev(bottomJaw, grabProgress, Maths.rad(25), 0, 0, 5F);
        progressRotationPrev(head, sitProgress, Maths.rad(10), Maths.rad(-20), 0, 5F);
        progressRotationPrev(body, sitProgress, 0, Maths.rad(10), 0, 5F);
        progressPositionPrev(tail1, sitProgress, -1, 0, -1, 5F);
        progressRotationPrev(tail1, sitProgress, 0, Maths.rad(40), 0, 5F);
        progressRotationPrev(tail2, sitProgress, 0, Maths.rad(40), 0, 5F);
        progressRotationPrev(tail3, sitProgress, 0, Maths.rad(50), 0, 5F);
        this.bob(head, idleSpeed, idleDegree * 5, false, ageInTicks, 1);
        this.bob(body, 20, 0.5F, false, ageInTicks, vibrateProgress);
        this.swing(body, 20, 0.04F, false, 3F, 0F, ageInTicks, vibrateProgress);
        this.swing(head, 0.5F, 0.4F, true, 2, 0F, ageInTicks, grabProgress * 0.2F);
        this.swing(body, 0.5F, 0.4F, false, 2, 0F, ageInTicks, grabProgress * 0.2F);
        this.swing(tail1, 0.5F, 0.4F, false, 4, 0F, ageInTicks, grabProgress * 0.2F);
        this.swing(tail2, 0.5F, 0.4F, false, 3, 0F, ageInTicks, grabProgress * 0.2F);
        this.head.rotationPointX += walkValue(ageInTicks, grabProgress * 0.2F, 0.5F, 2F, 2, false);
        this.swing(tail1, idleSpeed, idleDegree, false, 3F, 0F, ageInTicks, 1);
        this.swing(tail2, idleSpeed, idleDegree, false, 2F, 0F, ageInTicks, 1);
        this.swing(tail3, idleSpeed, idleDegree, false, 1F, 0F, ageInTicks, 1);
        this.flap(body, walkSpeed, walkDegree * 0.1F, true, 1F, 0F, limbSwing, walkAmount);
        this.flap(head, walkSpeed, walkDegree * 0.1F, false, 1F, 0F, limbSwing, walkAmount);
        this.flap(leftLeg, walkSpeed, walkDegree * 0.1F, false, 1F, 0F, limbSwing, walkAmount);
        this.flap(rightLeg, walkSpeed, walkDegree * 0.1F, false, 1F, 0F, limbSwing, walkAmount);
        this.flap(leftArm, walkSpeed, walkDegree * 0.1F, false, 1F, 0F, limbSwing, walkAmount);
        this.flap(rightArm, walkSpeed, walkDegree * 0.1F, false, 1F, 0F, limbSwing, walkAmount);
        this.flap(tail1, walkSpeed, walkDegree * 0.1F, true, -1F, 0F, limbSwing, walkAmount);
        this.swing(tail1, walkSpeed, walkDegree * 0.3F, false, 0F, 0F, limbSwing, walkAmount);
        this.swing(tail2, walkSpeed, walkDegree * 0.3F, false, 1F, 0F, limbSwing, walkAmount);
        this.swing(tail3, walkSpeed, walkDegree * 0.3F, false, -1F, 0F, limbSwing, walkAmount);
        this.bob(head, walkSpeed, walkDegree * -1, false, limbSwing, walkAmount);
        float bodyBob = walkValue(limbSwing, walkAmount, walkSpeed, 0.5F, 1F, true) - walkAmount * 2;
        this.body.rotationPointY += bodyBob;
        this.walk(leftArm, walkSpeed, walkDegree * 0.4F, true, 0F, 0F, limbSwing, walkAmount);
        this.walk(leftHand, walkSpeed, walkDegree * 0.2F, true, -3F, 0.1F, limbSwing, walkAmount);
        leftArm.rotationPointY += Math.min(0, walkValue(limbSwing, walkAmount, walkSpeed, -1.5F, 3, false)) - bodyBob;
        leftArm.rotationPointZ += walkValue(limbSwing, walkAmount, walkSpeed, -1.5F, walkDegree * 3, false);
        leftHand.rotationPointY += Math.min(0, walkValue(limbSwing, walkAmount, walkSpeed, -2.5F, walkDegree * 1F, true));
        this.walk(rightArm, walkSpeed, walkDegree * 0.4F, false, 0F, 0F, limbSwing, walkAmount);
        this.walk(rightHand, walkSpeed, walkDegree * 0.2F, false, -3F, -0.1F, limbSwing, walkAmount);
        rightArm.rotationPointY += Math.min(0, walkValue(limbSwing, walkAmount, walkSpeed, -1.5F, 3, true)) - bodyBob;
        rightArm.rotationPointZ += walkValue(limbSwing, walkAmount, walkSpeed, -1.5F, walkDegree * 3, true);
        rightHand.rotationPointY += Math.min(0, walkValue(limbSwing, walkAmount, walkSpeed, -2.5F, walkDegree * 1F, false));
        this.walk(leftLeg, walkSpeed, walkDegree * 0.3F, false, 1F, 0F, limbSwing, walkAmount);
        this.walk(leftFoot, walkSpeed, walkDegree * 0.2F, false, -2F, -0.1F, limbSwing, walkAmount);
        leftLeg.rotationPointY += Math.min(0, walkValue(limbSwing, walkAmount, walkSpeed, -0.5F, 3, true)) - bodyBob;
        leftLeg.rotationPointZ += walkValue(limbSwing, walkAmount, walkSpeed, -0.5F, walkDegree * 3, true);
        leftLeg.rotationPointY += Math.min(0, walkValue(limbSwing, walkAmount, walkSpeed, -2F, walkDegree * 0.5F, false));
        this.walk(rightLeg, walkSpeed, walkDegree * 0.3F, true, 1F, 0F, limbSwing, walkAmount);
        this.walk(rightFoot, walkSpeed, walkDegree * 0.2F, true, -2F, 0.1F, limbSwing, walkAmount);
        rightLeg.rotationPointY += Math.min(0, walkValue(limbSwing, walkAmount, walkSpeed, -0.5F, 3, false)) - bodyBob;
        rightLeg.rotationPointZ += walkValue(limbSwing, walkAmount, walkSpeed, -0.5F, walkDegree * 3, false);
        rightFoot.rotationPointY += Math.min(0, walkValue(limbSwing, walkAmount, walkSpeed, -2F, walkDegree * 0.5F, true));
        this.walk(rightArm, swimSpeed, swimDegree, false, 0F, -0.25F, limbSwing, swimAmount);
        this.walk(leftArm, swimSpeed, swimDegree, false, 0F, -0.25F, limbSwing, swimAmount);
        this.walk(rightLeg, swimSpeed, swimDegree, true, 0F, 0.25F, limbSwing, swimAmount);
        this.walk(leftLeg, swimSpeed, swimDegree, true, 0F, 0.25F, limbSwing, swimAmount);
        this.swing(body, swimSpeed, swimDegree * 0.4F, false, 1.5F, 0F, limbSwing, swimAmount);
        this.swing(head, swimSpeed, swimDegree * 0.1F, true, 2F, 0F, limbSwing, swimAmount);
        this.chainSwing(new AdvancedModelBox[]{tail1, tail2, tail3}, swimSpeed, swimDegree * 1F, -2.5F, limbSwing, swimAmount);
        this.faceTarget(netHeadYaw, headPitch, 1, head);
    }

    public void setRotationAngle(AdvancedModelBox AdvancedModelBox, float x, float y, float z) {
        AdvancedModelBox.rotateAngleX = x;
        AdvancedModelBox.rotateAngleY = y;
        AdvancedModelBox.rotateAngleZ = z;
    }

    @Override
    public void render(net.minecraft.entity.Entity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
        if (this.isChild) {
            float headScale = 1.25F;
            head.setScale(headScale, headScale, headScale);
            head.setShouldScaleChildren(true);
        }
        for (net.minecraft.client.model.ModelRenderer part : this.getParts()) {
            part.render(scale);
        }
        if (this.isChild) {
            head.setScale(1.0F, 1.0F, 1.0F);
            head.setShouldScaleChildren(false);
        }
    }

    private float walkValue(float limbSwing, float limbSwingAmount, float speed, float offset, float degree, boolean inverse) {
        return (float) ((Math.cos(limbSwing * speed + offset) * degree * limbSwingAmount) * (inverse ? -1 : 1));
    }

    @Override
    public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor, Entity entityIn) {
        setRotationAngles((EntityCaiman) entityIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
    }
}
