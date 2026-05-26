package com.github.alexthe666.alexsmobs.client.model;

import com.github.alexthe666.alexsmobs.entity.EntityBison;
import com.github.alexthe666.citadel.animation.IAnimatedEntity;
import com.github.alexthe666.citadel.client.model.AdvancedEntityModel;
import com.github.alexthe666.citadel.client.model.AdvancedModelBox;
import com.github.alexthe666.citadel.client.model.ModelAnimator;
import com.google.common.collect.ImmutableList;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

public class ModelBisonBaby extends AdvancedEntityModel<EntityBison> {
    private final AdvancedModelBox root;
    private final AdvancedModelBox body;
    private final AdvancedModelBox torso;
    private final AdvancedModelBox left_arm;
    private final AdvancedModelBox right_arm;
    private final AdvancedModelBox head;
    private final AdvancedModelBox left_ear;
    private final AdvancedModelBox right_ear;
    private final AdvancedModelBox tail;
    private final AdvancedModelBox left_leg;
    private final AdvancedModelBox right_leg;
    private final ModelAnimator animator;

    public ModelBisonBaby() {
        textureWidth = 64;
        textureHeight = 64;

        root = new AdvancedModelBox(this);
        root.setRotationPoint(0.0F, 24.0F, 0.0F);


        body = new AdvancedModelBox(this);
        body.setRotationPoint(0.0F, -13.5F, 0.0F);
        root.addChild(body);
        body.setTextureOffset(0, 19).func_228303_a_(-3.0F, -3.5F, -1.0F, 6.0F, 8.0F, 10.0F, 0.0F, false);

        torso = new AdvancedModelBox(this);
        torso.setRotationPoint(0.0F, -0.4F, 0.0F);
        body.addChild(torso);
        torso.setTextureOffset(0, 0).func_228303_a_(-3.5F, -4.0F, -9.0F, 7.0F, 9.0F, 9.0F, 0.0F, false);

        left_arm = new AdvancedModelBox(this);
        left_arm.setRotationPoint(2.4F, 3.9F, -6.5F);
        torso.addChild(left_arm);
        left_arm.setTextureOffset(0, 38).func_228303_a_(-1.0F, 1.0F, -1.5F, 2.0F, 9.0F, 3.0F, 0.0F, false);

        right_arm = new AdvancedModelBox(this);
        right_arm.setRotationPoint(-2.4F, 3.9F, -6.5F);
        torso.addChild(right_arm);
        right_arm.setTextureOffset(0, 38).func_228303_a_(-1.0F, 1.0F, -1.5F, 2.0F, 9.0F, 3.0F, 0.0F, true);

        head = new AdvancedModelBox(this);
        head.setRotationPoint(0.0F, -2.1F, -9.0F);
        torso.addChild(head);
        setRotationAngle(head, 0.6981F, 0.0F, 0.0F);
        head.setTextureOffset(24, 10).func_228303_a_(-2.5F, -3.0F, -8.0F, 5.0F, 6.0F, 9.0F, 0.0F, false);

        left_ear = new AdvancedModelBox(this);
        left_ear.setRotationPoint(2.5F, -2.6F, -0.3F);
        head.addChild(left_ear);
        setRotationAngle(left_ear, -0.8378F, -1.3875F, 0.8727F);
        left_ear.setTextureOffset(24, 0).func_228303_a_(0.0F, -1.0F, -0.5F, 4.0F, 2.0F, 1.0F, 0.0F, false);

        right_ear = new AdvancedModelBox(this);
        right_ear.setRotationPoint(-2.5F, -2.6F, -0.3F);
        head.addChild(right_ear);
        setRotationAngle(right_ear, -0.8378F, 1.3875F, -0.8727F);
        right_ear.setTextureOffset(24, 0).func_228303_a_(-4.0F, -1.0F, -0.5F, 4.0F, 2.0F, 1.0F, 0.0F, true);

        tail = new AdvancedModelBox(this);
        tail.setRotationPoint(0.0F, -1.5F, 9.0F);
        body.addChild(tail);
        setRotationAngle(tail, 0.2618F, 0.0F, 0.0F);
        tail.setTextureOffset(0, 0).func_228303_a_(-1.0F, -1.0F, 0.0F, 2.0F, 6.0F, 1.0F, 0.0F, false);

        left_leg = new AdvancedModelBox(this);
        left_leg.setRotationPoint(1.8F, 3.5F, 7.5F);
        body.addChild(left_leg);
        left_leg.setTextureOffset(33, 26).func_228303_a_(-1.0F, 1.0F, -1.5F, 2.0F, 9.0F, 3.0F, 0.0F, false);

        right_leg = new AdvancedModelBox(this);
        right_leg.setRotationPoint(-1.8F, 3.5F, 7.5F);
        body.addChild(right_leg);
        right_leg.setTextureOffset(33, 26).func_228303_a_(-1.0F, 1.0F, -1.5F, 2.0F, 9.0F, 3.0F, 0.0F, true);
        this.updateDefaultPose();
        animator = ModelAnimator.create();
    }

    public void setRotationAngle(AdvancedModelBox AdvancedModelBox, float x, float y, float z) {
        AdvancedModelBox.rotateAngleX = x;
        AdvancedModelBox.rotateAngleY = y;
        AdvancedModelBox.rotateAngleZ = z;
    }


    public void animate(IAnimatedEntity entity, float f, float f1, float f2, float f3, float f4) {
        this.resetToDefaultPose();
        animator.update(entity);
        animator.setAnimation(EntityBison.ANIMATION_EAT);
        animator.startKeyframe(5);
        eatPose();
        animator.endKeyframe();
        animator.startKeyframe(5);
        eatPose();
        animator.move(head, 0, 1, 1);
        animator.rotate(head, (float)Math.toRadians(10), 0, 0);
        animator.endKeyframe();
        animator.startKeyframe(5);
        eatPose();
        animator.endKeyframe();
        animator.startKeyframe(5);
        eatPose();
        animator.move(head, 0, 1, 1);
        animator.rotate(head, (float)Math.toRadians(10), 0, 0);
        animator.endKeyframe();
        animator.startKeyframe(5);
        eatPose();
        animator.endKeyframe();
        animator.startKeyframe(5);
        eatPose();
        animator.move(head, 0, 1, 1);
        animator.rotate(head, (float)Math.toRadians(10), 0, 0);
        animator.endKeyframe();
        animator.resetKeyframe(5);
    }

    private void eatPose(){
        animator.rotate(head, (float)Math.toRadians(-10), 0, 0);
        animator.rotate(torso, (float)Math.toRadians(15), 0, 0);
        animator.move(torso, 0, 0, 2);
        animator.rotate(left_arm, (float)Math.toRadians(-15), 0, 0);
        animator.rotate(right_arm, (float)Math.toRadians(-15), 0, 0);
        animator.move(left_arm, 0, -2.5F, 0);
        animator.move(right_arm, 0, -2.5F, 0);
        animator.move(head, 0, 4, 0);
    }

    @Override
    public Iterable<AdvancedModelBox> getAllParts() {
        return ImmutableList.of(root, body, torso, head, left_ear, right_ear, left_leg, right_leg, tail, right_arm, left_arm);
    }

    public Iterable<ModelRenderer> getParts() {
        return ImmutableList.of(root);
    }

    @Override
    public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale, Entity entityIn) {
        setRotationAngles((EntityBison) entityIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
    }

    public void setRotationAngles(EntityBison entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.resetToDefaultPose();
        this.animate(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
        float walkSpeed = 0.4F;
        float walkDegree = 0.7F;
        float idleSpeed = 0.1F;
        float idleDegree = 0.1F;
        this.walk(right_arm, walkSpeed, walkDegree, false, 0F, 0F, limbSwing, limbSwingAmount);
        this.walk(left_arm, walkSpeed, walkDegree, true, 0F, 0F, limbSwing, limbSwingAmount);
        this.walk(right_leg, walkSpeed, walkDegree, true, 0F, 0F, limbSwing, limbSwingAmount);
        this.walk(left_leg, walkSpeed, walkDegree, false, 0F, 0F, limbSwing, limbSwingAmount);
        this.walk(tail, walkSpeed, walkDegree * 0.1F, true, 1F, -0.6F, limbSwing, limbSwingAmount);
        this.bob(body, walkSpeed, walkDegree, true, limbSwing, limbSwingAmount);
        this.bob(head, walkSpeed, -walkDegree, false, limbSwing, limbSwingAmount);
        this.swing(left_ear, idleSpeed, idleDegree * 0.5F, true, 3F, -0.2F, ageInTicks, 1);
        this.swing(right_ear, idleSpeed, idleDegree * 0.5F, true, 3F, 0.2F, ageInTicks, 1);
        this.walk(tail, idleSpeed, idleDegree, false, 1F, 0.1F, ageInTicks, 1);
        this.bob(head, idleSpeed, idleDegree, false, ageInTicks, 1);
        this.head.rotateAngleY += netHeadYaw * 0.75F * (float)Math.PI / 180F;
        this.head.rotateAngleX += headPitch * (float)Math.PI / 180F;
    }

    @Override
    public void render(Entity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
        for (ModelRenderer part : this.getParts()) {
            part.render(scale);
        }
    }

}
