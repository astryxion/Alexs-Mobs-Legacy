package com.github.alexthe666.alexsmobs.client.model;

import com.github.alexthe666.alexsmobs.entity.EntityRainFrog;
import com.github.alexthe666.citadel.client.model.AdvancedEntityModel;
import com.github.alexthe666.citadel.client.model.AdvancedModelBox;
import com.google.common.collect.ImmutableList;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;

public class ModelRainFrog extends AdvancedEntityModel<EntityRainFrog> {
    private final AdvancedModelBox root;
    private final AdvancedModelBox body;
    private final AdvancedModelBox tongue;
    private final AdvancedModelBox left_arm;
    private final AdvancedModelBox right_arm;
    private final AdvancedModelBox left_leg;
    private final AdvancedModelBox right_leg;
    private final AdvancedModelBox left_eye;
    private final AdvancedModelBox right_eye;

    public ModelRainFrog() {
        textureWidth = 64;
        textureHeight = 64;

        root = new AdvancedModelBox(this, "root");
        root.setRotationPoint(0.0F, 24.0F, 0.0F);


        body = new AdvancedModelBox(this, "body");
        body.setRotationPoint(0.0F, -3.0F, -0.5F);
        root.addChild(body);
        body.setTextureOffset(0, 0).func_228303_a_(-3.5F, -3.0F, -4.5F, 7.0F, 5.0F, 9.0F, 0.0F, false);

        tongue = new AdvancedModelBox(this, "tongue");
        tongue.setRotationPoint(0.0F, -1.0F, -4.5F);
        body.addChild(tongue);
        tongue.setTextureOffset(0, 0).func_228303_a_(-1.0F, 0.0F, -2.0F, 2.0F, 0.0F, 2.0F, 0.0F, false);

        left_arm = new AdvancedModelBox(this, "left_arm");
        left_arm.setRotationPoint(3.5F, 0.0F, -3.0F);
        body.addChild(left_arm);
        left_arm.setTextureOffset(0, 15).func_228303_a_(-3.0F, -0.01F, -2.0F, 4.0F, 3.0F, 4.0F, 0.0F, false);

        right_arm = new AdvancedModelBox(this, "right_arm");
        right_arm.setRotationPoint(-3.5F, 0.0F, -3.0F);
        body.addChild(right_arm);
        right_arm.setTextureOffset(0, 15).func_228303_a_(-1.0F, -0.01F, -2.0F, 4.0F, 3.0F, 4.0F, 0.0F, true);

        left_leg = new AdvancedModelBox(this, "left_leg");
        left_leg.setRotationPoint(2.5F, 1.25F, 2.25F);
        body.addChild(left_leg);
        left_leg.setTextureOffset(15, 21).func_228303_a_(-0.5F, -1.25F, -0.25F, 2.0F, 3.0F, 2.0F, 0.0F, false);
        left_leg.setTextureOffset(17, 15).func_228303_a_(-0.5F, 1.749F, -2.25F, 4.0F, 0.0F, 4.0F, 0.0F, false);

        right_leg = new AdvancedModelBox(this, "right_leg");
        right_leg.setRotationPoint(-2.5F, 1.25F, 2.25F);
        body.addChild(right_leg);
        right_leg.setTextureOffset(15, 21).func_228303_a_(-1.5F, -1.25F, -0.25F, 2.0F, 3.0F, 2.0F, 0.0F, true);
        right_leg.setTextureOffset(17, 15).func_228303_a_(-3.5F, 1.749F, -2.25F, 4.0F, 0.0F, 4.0F, 0.0F, true);

        left_eye = new AdvancedModelBox(this, "left_eye");
        left_eye.setRotationPoint(1.5F, -3.0F, -2.5F);
        body.addChild(left_eye);
        left_eye.setTextureOffset(0, 23).func_228303_a_(-1.0F, -1.0F, -2.0F, 2.0F, 1.0F, 3.0F, 0.0F, false);

        right_eye = new AdvancedModelBox(this, "right_eye");
        right_eye.setRotationPoint(-1.5F, -3.0F, -2.5F);
        body.addChild(right_eye);
        right_eye.setTextureOffset(0, 23).func_228303_a_(-1.0F, -1.0F, -2.0F, 2.0F, 1.0F, 3.0F, 0.0F, true);
        this.updateDefaultPose();
    }

    

    @Override
    public Iterable<AdvancedModelBox> getAllParts() {
        return ImmutableList.of(root, body, right_eye, left_eye, right_arm, left_arm, right_leg, left_leg, tongue);
    }
    
    public void setRotationAngles(EntityRainFrog entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch){
        this.resetToDefaultPose();
        float walkSpeed = entity.isChild() ? 1F : 2.3F;
        float walkDegree = 1;
        float digSpeed = 0.8f;
        float digDegree = 0.5f;
        float danceSpeed = 0.8f;
        float danceDegree = 0.5f;
        float partialTick = ageInTicks - entity.ticksExisted;
        float burrowProgress = entity.prevBurrowProgress + (entity.burrowProgress - entity.prevBurrowProgress) * partialTick;
        float danceProgress = entity.prevDanceProgress + (entity.danceProgress - entity.prevDanceProgress) * partialTick;
        float attackProgress = entity.prevAttackProgress + (entity.attackProgress - entity.prevAttackProgress) * partialTick;
        float stanceProgress = entity.prevStanceProgress + (entity.stanceProgress - entity.prevStanceProgress) * partialTick;
        float blinkAmount = Math.max(0, ((float)Math.sin(ageInTicks * 0.1F) - 0.5F) * 2F) * (5f - stanceProgress) * 0.2F;
        float digAmount = MathHelper.clamp((float)Math.sin(burrowProgress * Math.PI / 5F), 0, 1F);
        progressPositionPrev(right_eye, blinkAmount, 0F, 0.9F, 0.1F, 1f);
        progressPositionPrev(left_eye, blinkAmount, 0F, 0.9F, 0.1F, 1f);
        progressPositionPrev(body, danceProgress, 0F, -2F, 0F, 5f);
        progressPositionPrev(right_leg, danceProgress, 0F, 0.7F, 0F, 5f);
        progressPositionPrev(left_leg, danceProgress, 0F, 0.7F, 0F, 5f);
        progressPositionPrev(body, burrowProgress, 0F, 5F, 0F, 5f);
        progressPositionPrev(tongue, 5f - attackProgress, 0F, 0, 3F, 5f);
        progressPositionPrev(body, attackProgress, 0F, 0, -2F, 5f);
        progressRotationPrev(body, attackProgress, (float)Math.toRadians(-10F), 0, 0, 5f);
        progressRotationPrev(right_leg, attackProgress, (float)Math.toRadians(10F), 0, 0, 5f);
        progressRotationPrev(left_leg, attackProgress, (float)Math.toRadians(10F), 0, 0, 5f);
        progressRotationPrev(left_arm, attackProgress, (float)Math.toRadians(10F), 0, 0, 5f);
        progressRotationPrev(right_arm, attackProgress, (float)Math.toRadians(10F), 0, 0, 5f);
        progressPositionPrev(left_leg, attackProgress, 0F, -1, 0F, 5f);
        progressPositionPrev(right_leg, attackProgress, 0F, -1, 0F, 5f);
        progressPositionPrev(body, stanceProgress, 0F, -2F, 0F, 5f);
        progressPositionPrev(left_leg, stanceProgress, 0F, 2F, 0F, 5f);
        progressPositionPrev(right_leg, stanceProgress, 0F, 2F, 0F, 5f);
        progressPositionPrev(left_arm, stanceProgress, 0F, 2F, 0F, 5f);
        progressPositionPrev(right_arm, stanceProgress, 0F, 2F, 0F, 5f);
        progressPositionPrev(left_eye, stanceProgress, 0F, -1, 0F, 5f);
        progressPositionPrev(right_eye, stanceProgress, 0F, -1, 0F, 5f);
        this.body.setScale(1F + stanceProgress * 0.025F, 1F + stanceProgress * 0.075F, 1F + stanceProgress * 0.025F);
        this.swing(body, digSpeed, digDegree * 0.5F, false, 3F, 0F, ageInTicks, digAmount);
        this.walk(right_arm, digSpeed, digDegree, false, -1.5F, -0.2F, ageInTicks, digAmount);
        this.walk(left_arm, digSpeed, digDegree, false, -1.5F, -0.2F, ageInTicks, digAmount);
        this.walk(right_leg, digSpeed, digDegree, false, -1.5F, 0.2F, ageInTicks, digAmount);
        this.walk(left_leg, digSpeed, digDegree, false, -1.5F, 0.2F, ageInTicks, digAmount);
        this.flap(body, walkSpeed, walkDegree * 0.35F, false, 0F, 0F, limbSwing, limbSwingAmount);
        this.swing(body, walkSpeed, walkDegree * 0.35F, false, 1F, 0F, limbSwing, limbSwingAmount);
        this.walk(left_arm, walkSpeed, walkDegree * 1.2F, false, -2.5F, -0.2F, limbSwing, limbSwingAmount);
        this.walk(right_arm, walkSpeed, walkDegree * 1.2F, true, -2.5F, 0.2F, limbSwing, limbSwingAmount);
        this.walk(right_leg, walkSpeed, walkDegree, false, -2.5F, 0.3F, limbSwing, limbSwingAmount);
        this.walk(left_leg, walkSpeed, walkDegree, true, -2.5F, -0.3F, limbSwing, limbSwingAmount);
        float leftLegS = (float) (Math.sin((double) (limbSwing * walkSpeed) - 2.5F) * (double) limbSwingAmount * (double) walkDegree - (double) (limbSwingAmount * walkDegree));
        float rightLegS = (float) (Math.sin(-(double) (limbSwing * walkSpeed) + 2.5F) * (double) limbSwingAmount * (double) walkDegree - (double) (limbSwingAmount * walkDegree));
        this.left_leg.rotationPointY += 1.5F * leftLegS;
        this.right_leg.rotationPointY += 1.5F * rightLegS;
        this.left_leg.rotationPointZ -= 2F * leftLegS;
        this.right_leg.rotationPointZ -= 2F * rightLegS;
        this.right_arm.rotationPointY += 1.5F * leftLegS;
        this.left_arm.rotationPointY += 1.5F * rightLegS;
        this.right_arm.rotationPointZ += 1F * leftLegS;
        this.left_arm.rotationPointZ += 1F * rightLegS;
        this.swing(body, danceSpeed, danceDegree * 0.5F, false, 1F, 0F, ageInTicks, danceProgress * 0.2F);
        this.walk(body, danceSpeed, danceDegree * 0.5F, false, 3F, -0.4F, ageInTicks, danceProgress * 0.2F);
        this.flap(right_arm, danceSpeed, danceDegree, false, 0F, 0.3F, ageInTicks, danceProgress * 0.2F);
        this.flap(left_arm, danceSpeed, danceDegree, true, 0F, 0.3F, ageInTicks, danceProgress * 0.2F);
        this.left_leg.rotateAngleX -= 1 * body.rotateAngleX;
        this.left_leg.rotateAngleY -= 1 * body.rotateAngleY;
        this.left_leg.rotateAngleZ -= 1 * body.rotateAngleZ;
        this.right_leg.rotateAngleX -= 1 * body.rotateAngleX;
        this.right_leg.rotateAngleY -= 1 * body.rotateAngleY;
        this.right_leg.rotateAngleZ -= 1 * body.rotateAngleZ;
    }



    public Iterable<ModelRenderer> getParts() {
        return ImmutableList.of(root);
    }

    @Override
    public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor, Entity entityIn) {
        setRotationAngles((EntityRainFrog) entityIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
    }

    @Override
    public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5) {
        getParts().forEach(part -> part.render(f5));
    }
}