package com.github.alexthe666.alexsmobs.client.model;

import com.github.alexthe666.alexsmobs.entity.EntityMoose;
import com.github.alexthe666.alexsmobs.entity.util.Maths;
import com.github.alexthe666.citadel.animation.IAnimatedEntity;
import com.github.alexthe666.citadel.client.model.AdvancedEntityModel;
import com.github.alexthe666.citadel.client.model.AdvancedModelBox;
import com.github.alexthe666.citadel.client.model.ModelAnimator;
import com.google.common.collect.ImmutableList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

public class ModelMoose extends AdvancedEntityModel<EntityMoose> {
    private final AdvancedModelBox root;
    private final AdvancedModelBox body;
    private final AdvancedModelBox left_arm;
    private final AdvancedModelBox right_arm;
    private final AdvancedModelBox left_leg;
    private final AdvancedModelBox right_leg;
    private final AdvancedModelBox upper_body;
    private final AdvancedModelBox neck;
    private final AdvancedModelBox head;
    private final AdvancedModelBox left_ear;
    private final AdvancedModelBox right_ear;
    private final AdvancedModelBox beard;
    private final ModelAnimator animator;

    public ModelMoose() {
        textureWidth = 128;
        textureHeight = 128;

        root = new AdvancedModelBox(this);
        root.setRotationPoint(0.0F, 24.0F, 0.0F);

        body = new AdvancedModelBox(this);
        body.setRotationPoint(0.0F, -28.0F, 0.0F);
        root.addChild(body);
        body.setTextureOffset(37, 80).func_228303_a_(-6.0F, -8.0F, -4.0F, 12.0F, 15.0F, 20.0F, 0.0F, false);

        left_arm = new AdvancedModelBox(this);
        left_arm.setRotationPoint(4.7F, 6.0F, -13.0F);
        body.addChild(left_arm);
        left_arm.setTextureOffset(19, 58).func_228303_a_(-2.0F, 1.0F, -2.0F, 4.0F, 21.0F, 4.0F, 0.0F, false);

        right_arm = new AdvancedModelBox(this);
        right_arm.setRotationPoint(-4.7F, 6.0F, -13.0F);
        body.addChild(right_arm);
        right_arm.setTextureOffset(19, 58).func_228303_a_(-2.0F, 1.0F, -2.0F, 4.0F, 21.0F, 4.0F, 0.0F, true);

        left_leg = new AdvancedModelBox(this);
        left_leg.setRotationPoint(3.7F, 6.0F, 14.0F);
        body.addChild(left_leg);
        left_leg.setTextureOffset(0, 58).func_228303_a_(-2.0F, 1.0F, -3.0F, 4.0F, 21.0F, 5.0F, 0.0F, false);

        right_leg = new AdvancedModelBox(this);
        right_leg.setRotationPoint(-3.7F, 6.0F, 14.0F);
        body.addChild(right_leg);
        right_leg.setTextureOffset(0, 58).func_228303_a_(-2.0F, 1.0F, -3.0F, 4.0F, 21.0F, 5.0F, 0.0F, true);

        upper_body = new AdvancedModelBox(this);
        upper_body.setRotationPoint(0.0F, -1.0F, -4.0F);
        body.addChild(upper_body);
        upper_body.setTextureOffset(52, 45).func_228303_a_(-7.0F, -10.0F, -13.0F, 14.0F, 18.0F, 13.0F, 0.0F, false);

        neck = new AdvancedModelBox(this);
        neck.setRotationPoint(0.0F, -6.0F, -14.0F);
        upper_body.addChild(neck);
        neck.setTextureOffset(45, 0).func_228303_a_(-4.0F, -3.0F, -6.0F, 8.0F, 9.0F, 7.0F, 0.0F, false);

        head = new AdvancedModelBox(this);
        head.setRotationPoint(0.0F, 0.0F, -7.0F);
        neck.addChild(head);
        head.setTextureOffset(51, 18).func_228303_a_(-3.0F, -3.0F, -15.0F, 6.0F, 7.0F, 16.0F, 0.0F, false);
        head.setTextureOffset(0, 34).func_228303_a_(3.0F, -12.0F, -7.0F, 18.0F, 9.0F, 14.0F, 0.0F, false);
        head.setTextureOffset(0, 34).func_228303_a_(-21.0F, -12.0F, -7.0F, 18.0F, 9.0F, 14.0F, 0.0F, true);

        left_ear = new AdvancedModelBox(this);
        left_ear.setRotationPoint(1.3F, -3.0F, 0.5F);
        head.addChild(left_ear);
        setRotationAngle(left_ear, -0.3054F, -0.2618F, 0.3927F);
        left_ear.setTextureOffset(11, 0).func_228303_a_(-0.3F, -4.0F, -0.5F, 2.0F, 4.0F, 1.0F, 0.0F, false);

        right_ear = new AdvancedModelBox(this);
        right_ear.setRotationPoint(-1.3F, -3.0F, 0.5F);
        head.addChild(right_ear);
        setRotationAngle(right_ear, -0.3054F, 0.2618F, -0.3927F);
        right_ear.setTextureOffset(11, 0).func_228303_a_(-1.7F, -4.0F, -0.5F, 2.0F, 4.0F, 1.0F, 0.0F, true);

        beard = new AdvancedModelBox(this);
        beard.setRotationPoint(0.0F, 4.0F, 0.0F);
        head.addChild(beard);
        beard.setTextureOffset(0, 0).func_228303_a_(0.0F, 0.0F, -4.0F, 0.0F, 6.0F, 5.0F, 0.0F, false);
        animator = ModelAnimator.create();
        this.updateDefaultPose();
    }

    @Override
    public Iterable<AdvancedModelBox> getAllParts() {
        return ImmutableList.of(root, left_ear, right_ear, head, neck, body, upper_body, beard, left_leg, right_leg, left_arm, right_arm);
    }

    public void animate(IAnimatedEntity entity, float f, float f1, float f2, float f3, float f4) {
        this.resetToDefaultPose();
        animator.update(entity);
        animator.setAnimation(EntityMoose.ANIMATION_EAT_GRASS);
        animator.startKeyframe(5);
        animator.rotate(neck, Maths.rad(50), 0, 0);
        animator.rotate(head, Maths.rad(4), 0, 0);
        eatPose();
        animator.endKeyframe();
        animator.startKeyframe(4);
        animator.rotate(neck, Maths.rad(70), 0, 0);
        animator.rotate(head, Maths.rad(10), 0, 0);
        eatPose();
        animator.endKeyframe();
        animator.startKeyframe(4);
        animator.rotate(neck, Maths.rad(50), 0, 0);
        animator.rotate(head, Maths.rad(0), 0, 0);
        eatPose();
        animator.endKeyframe();
        animator.startKeyframe(4);
        animator.rotate(neck, Maths.rad(70), 0, 0);
        animator.rotate(head, Maths.rad(10), 0, 0);
        eatPose();
        animator.endKeyframe();
        animator.startKeyframe(4);
        animator.rotate(neck, Maths.rad(50), 0, 0);
        animator.rotate(head, Maths.rad(0), 0, 0);
        eatPose();
        animator.endKeyframe();
        animator.startKeyframe(4);
        animator.rotate(neck, Maths.rad(70), 0, 0);
        animator.rotate(head, Maths.rad(10), 0, 0);
        eatPose();
        animator.endKeyframe();
        animator.resetKeyframe(5);
        animator.setAnimation(EntityMoose.ANIMATION_ATTACK);
        animator.startKeyframe(8);
        eatPose();
        animator.rotate(neck, Maths.rad(50), 0, 0);
        animator.rotate(head, Maths.rad(10), 0, 0);
        animator.endKeyframe();
        animator.startKeyframe(3);
        animator.rotate(neck, Maths.rad(-34), 0, 0);
        animator.rotate(head, Maths.rad(-20), 0, 0);
        animator.endKeyframe();
        animator.resetKeyframe(4);
    }

    private void eatPose() {
        animator.rotate(body, Maths.rad(10), 0, 0);
        animator.move(body, 0, 2, 0);
        animator.rotate(left_leg, Maths.rad(-10), 0, 0);
        animator.rotate(right_leg, Maths.rad(-10), 0, 0);
        animator.rotate(left_arm, Maths.rad(-10), 0, Maths.rad(-10));
        animator.rotate(right_arm, Maths.rad(-10), 0, Maths.rad(10));
        animator.move(left_arm, 0.1F, -3, 0F);
        animator.move(right_arm, -0.1F, -3, 0F);
        animator.move(left_leg, 0, -0.2F, 0);
        animator.move(right_leg, 0, -0.2F, 0);
        animator.move(neck, 0, 1, 0);
    }

    @Override
    public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale, Entity entityIn) {
        setRotationAngles((EntityMoose) entityIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
    }

    public void setRotationAngles(EntityMoose entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.resetToDefaultPose();
        animate(entityIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
        float walkSpeed = 0.7F;
        float walkDegree = 0.6F;
        float idleSpeed = 0.1F;
        float idleDegree = 0.1F;
        float partialTick = Minecraft.getMinecraft().getRenderPartialTicks();
        float jostleProgress = entityIn.prevJostleProgress + (entityIn.jostleProgress - entityIn.prevJostleProgress) * partialTick;
        float jostleAngle = entityIn.prevJostleAngle + (entityIn.getJostleAngle() - entityIn.prevJostleAngle) * partialTick;
        this.flap(beard, idleSpeed, idleDegree * 4, false, 0F, 0F, ageInTicks, 1);
        this.flap(left_ear, idleSpeed, idleDegree, false, 1F, -0.2F, ageInTicks, 1);
        this.flap(right_ear, idleSpeed, idleDegree, true, 1F, 0.2F, ageInTicks, 1);
        this.walk(neck, idleSpeed, idleDegree, false, 0F, 0F, ageInTicks, 1);
        this.walk(head, idleSpeed, -idleDegree, false, 0.5F, 0F, ageInTicks, 1);
        this.walk(body, walkSpeed, walkDegree * 0.05F, true, 0F, 0F, limbSwing, limbSwingAmount);
        this.bob(body, walkSpeed, walkDegree, false, limbSwing, limbSwingAmount);
        this.walk(neck, walkSpeed, walkDegree * 0.25F, true, 1F, 0F, limbSwing, limbSwingAmount);
        this.walk(head, walkSpeed, -walkDegree * 0.25F, true, 1F, 0F, limbSwing, limbSwingAmount);
        this.walk(right_arm, walkSpeed, walkDegree * 1.1F, true, 0F, 0F, limbSwing, limbSwingAmount);
        this.bob(right_arm, walkSpeed, walkDegree, false, limbSwing, limbSwingAmount);
        this.walk(left_arm, walkSpeed, walkDegree * 1.1F, false, 0F, 0F, limbSwing, limbSwingAmount);
        this.bob(left_arm, walkSpeed, walkDegree, false, limbSwing, limbSwingAmount);
        this.walk(right_leg, walkSpeed, walkDegree * 1.1F, false, 0F, 0F, limbSwing, limbSwingAmount);
        this.bob(right_leg, walkSpeed, walkDegree, false, limbSwing, limbSwingAmount);
        this.walk(left_leg, walkSpeed, walkDegree * 1.1F, true, 0F, 0F, limbSwing, limbSwingAmount);
        this.bob(left_leg, walkSpeed, walkDegree, false, limbSwing, limbSwingAmount);
        progressRotationPrev(neck, jostleProgress, Maths.rad(7), 0, 0, 5F);
        progressRotationPrev(head, jostleProgress, Maths.rad(80), 0, 0, 5F);
        progressPositionPrev(neck, jostleProgress, 0, 0, 1, 5F);
        progressPositionPrev(head, jostleProgress, 0, 0, -1, 5F);
        if (jostleProgress > 0) {
            float yawAmount = jostleAngle / 57.295776F * 0.5F * jostleProgress * 0.2F;
            neck.rotateAngleY += yawAmount;
            head.rotateAngleY += yawAmount;
            head.rotateAngleZ += yawAmount;
        } else {
            this.faceTarget(netHeadYaw, headPitch, 2, neck, head);
        }
    }

    @Override
    public void render(Entity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
        if (this.isChild) {
            float f = 1.35F;
            float feet = 1.45F;
            head.setScale(f, f, f);
            head.setShouldScaleChildren(true);
            right_arm.setScale(1, feet, 1);
            left_arm.setScale(1, feet, 1);
            right_leg.setScale(1, feet, 1);
            left_leg.setScale(1, feet, 1);
            net.minecraft.client.renderer.GlStateManager.pushMatrix();
            net.minecraft.client.renderer.GlStateManager.scale(0.35F, 0.35F, 0.35F);
            net.minecraft.client.renderer.GlStateManager.translate(0.0F, 2.25F, 0.125F);
            for (ModelRenderer part : this.getParts()) {
                part.render(scale);
            }
            net.minecraft.client.renderer.GlStateManager.popMatrix();
            head.setScale(1, 1, 1);
            right_arm.setScale(1, 1, 1);
            left_arm.setScale(1, 1, 1);
            right_leg.setScale(1, 1, 1);
            left_leg.setScale(1, 1, 1);
        } else {
            for (ModelRenderer part : this.getParts()) {
                part.render(scale);
            }
        }
    }

    public Iterable<ModelRenderer> getParts() {
        return ImmutableList.of(root);
    }

    public void setRotationAngle(AdvancedModelBox box, float x, float y, float z) {
        box.rotateAngleX = x;
        box.rotateAngleY = y;
        box.rotateAngleZ = z;
    }
}
