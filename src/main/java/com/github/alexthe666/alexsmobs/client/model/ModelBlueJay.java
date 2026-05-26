package com.github.alexthe666.alexsmobs.client.model;

import com.github.alexthe666.alexsmobs.entity.EntityBlueJay;
import com.github.alexthe666.citadel.client.model.AdvancedEntityModel;
import com.github.alexthe666.citadel.client.model.AdvancedModelBox;
import com.google.common.collect.ImmutableList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;

public class ModelBlueJay extends AdvancedEntityModel<EntityBlueJay> {

    private final AdvancedModelBox root;
    private final AdvancedModelBox body;
    private final AdvancedModelBox leftLeg;
    private final AdvancedModelBox rightLeg;
    private final AdvancedModelBox tail;
    private final AdvancedModelBox leftWing;
    private final AdvancedModelBox rightWing;
    private final AdvancedModelBox head;
    private final AdvancedModelBox crest;

    public ModelBlueJay() {
        textureWidth = 64;
        textureHeight = 64;

        root = new AdvancedModelBox(this);
        root.setRotationPoint(0.0F, 24.0F, 0.0F);

        body = new AdvancedModelBox(this);
        body.setRotationPoint(0.0F, -3.2F, 0.0F);
        root.addChild(body);
        setRotationAngle(body, -0.1309F, 0.0F, 0.0F);
        body.setTextureOffset(0, 0).func_228303_a_(-2.0F, -4.0F, -4.0F, 4.0F, 4.0F, 7.0F, 0.0F, false);

        leftLeg = new AdvancedModelBox(this);
        leftLeg.setRotationPoint(1.5F, 0.0F, 1.0F);
        body.addChild(leftLeg);
        setRotationAngle(leftLeg, 0.1309F, 0.0F, 0.0F);
        leftLeg.setTextureOffset(26, 10).func_228303_a_(-1.5F, 0.0F, -2.0F, 3.0F, 3.0F, 2.0F, 0.0F, false);

        rightLeg = new AdvancedModelBox(this);
        rightLeg.setRotationPoint(-1.5F, 0.0F, 1.0F);
        body.addChild(rightLeg);
        setRotationAngle(rightLeg, 0.1309F, 0.0F, 0.0F);
        rightLeg.setTextureOffset(26, 10).func_228303_a_(-1.5F, 0.0F, -2.0F, 3.0F, 3.0F, 2.0F, 0.0F, true);

        tail = new AdvancedModelBox(this);
        tail.setRotationPoint(0.0F, -3.0F, 3.0F);
        body.addChild(tail);
        tail.setTextureOffset(0, 22).func_228303_a_(-1.5F, 0.0F, 0.0F, 3.0F, 1.0F, 7.0F, 0.0F, false);

        leftWing = new AdvancedModelBox(this);
        leftWing.setRotationPoint(2.0F, -3.0F, -2.0F);
        body.addChild(leftWing);
        leftWing.setTextureOffset(15, 14).func_228303_a_(0.0F, -1.0F, -1.0F, 1.0F, 3.0F, 8.0F, 0.0F, false);

        rightWing = new AdvancedModelBox(this);
        rightWing.setRotationPoint(-2.0F, -3.0F, -2.0F);
        body.addChild(rightWing);
        rightWing.setTextureOffset(15, 14).func_228303_a_(-1.0F, -1.0F, -1.0F, 1.0F, 3.0F, 8.0F, 0.0F, true);

        head = new AdvancedModelBox(this);
        head.setRotationPoint(0.0F, -3.0F, -4.0F);
        body.addChild(head);
        setRotationAngle(head, 0.2182F, 0.0F, 0.0F);
        head.setTextureOffset(23, 0).func_228303_a_(-2.5F, -3.0F, -3.0F, 5.0F, 4.0F, 5.0F, 0.0F, false);
        head.setTextureOffset(26, 16).func_228303_a_(-1.0F, -1.0F, -6.0F, 2.0F, 2.0F, 3.0F, 0.0F, false);

        crest = new AdvancedModelBox(this);
        crest.setRotationPoint(0.0F, -2.0F, -1.0F);
        head.addChild(crest);
        setRotationAngle(crest, 0.3491F, 0.0F, 0.0F);
        crest.setTextureOffset(0, 12).func_228303_a_(-2.5F, -1.0F, 0.0F, 5.0F, 3.0F, 6.0F, -0.01F, false);
        this.updateDefaultPose();
    }

    public Iterable<ModelRenderer> getParts() {
        return ImmutableList.of(root);
    }

    @Override
    public Iterable<AdvancedModelBox> getAllParts() {
        return ImmutableList.of(root, body, crest, head, tail, leftLeg, rightLeg, leftWing, rightWing);
    }

    @Override
    public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale, Entity entityIn) {
        setRotationAngles((EntityBlueJay) entityIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
    }

    public void setRotationAngles(EntityBlueJay entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.resetToDefaultPose();
        float flapSpeed = 0.6F;
        float flapDegree = 0.2F;
        float walkSpeed = 0.95F;
        float walkDegree = 0.6F;
        float idleSpeed = 0.1F;
        float idleDegree = 0.1F;
        float partialTick = Minecraft.getMinecraft().getRenderPartialTicks();
        float flyProgress = entity.prevFlyProgress + (entity.flyProgress - entity.prevFlyProgress) * partialTick;
        float flapAmount = flyProgress * 0.2F * (entity.prevFlapAmount + (entity.flapAmount - entity.prevFlapAmount) * partialTick);
        float crestAmount = entity.prevCrestAmount + (entity.crestAmount - entity.prevCrestAmount) * partialTick;
        float biteProgress = entity.prevAttackProgress + (entity.attackProgress - entity.prevAttackProgress) * partialTick;
        float birdPitch = entity.prevBirdPitch + (entity.birdPitch - entity.prevBirdPitch) * partialTick;
        progressRotationPrev(rightWing, flyProgress, (float) Math.toRadians(-20), 0, (float) Math.toRadians(20), 5F);
        progressRotationPrev(leftWing, flyProgress, (float) Math.toRadians(-20), 0, (float) Math.toRadians(-20), 5F);
        progressRotationPrev(body, flyProgress, (float) Math.toRadians(10), 0, 0, 5F);
        progressRotationPrev(leftLeg, flyProgress, (float) Math.toRadians(40), 0, 0, 5F);
        progressRotationPrev(rightLeg, flyProgress, (float) Math.toRadians(40), 0, 0, 5F);
        progressPositionPrev(head, flyProgress, 0, 1F, -1F, 5f);
        progressPositionPrev(body, flyProgress, 0, 1F, 0F, 5f);
        progressPositionPrev(rightWing, flyProgress, 0, 1F, 1F, 5f);
        progressPositionPrev(leftWing, flyProgress, 0, 1F, 1F, 5f);
        progressPositionPrev(rightLeg, flyProgress, 0, -2F, 0F, 5f);
        progressPositionPrev(leftLeg, flyProgress, 0, -2F, 0F, 5f);
        progressRotationPrev(rightWing, flapAmount, (float) Math.toRadians(-70), 0, (float) Math.toRadians(70), 1F);
        progressRotationPrev(leftWing, flapAmount, (float) Math.toRadians(-70), 0, (float) Math.toRadians(-70), 1F);
        progressRotationPrev(crest, crestAmount, (float) Math.toRadians(20), 0, 0, 1F);
        progressRotationPrev(head, biteProgress, (float) Math.toRadians(60), 0, 0, 5F);
        leftWing.setScale(1F + flyProgress * 0.1F, 1F + flyProgress * 0.1F, 1F + flyProgress * 0.1F);
        rightWing.setScale(1F + flyProgress * 0.1F, 1F + flyProgress * 0.1F, 1F + flyProgress * 0.1F);
        this.flap(leftWing, flapSpeed, flapDegree * 5, true, -1F, 0F, ageInTicks, flapAmount);
        this.flap(rightWing, flapSpeed, flapDegree * 5, false, -1F, 0F, ageInTicks, flapAmount);
        this.swing(leftWing, flapSpeed, flapDegree * 2, false, 0F, 0F, ageInTicks, flapAmount);
        this.swing(rightWing, flapSpeed, flapDegree * 2, false, 0F, 0F, ageInTicks, flapAmount);
        this.walk(leftWing, flapSpeed, flapDegree * 2, false, 1F, 0F, ageInTicks, flapAmount);
        this.walk(rightWing, flapSpeed, flapDegree * 2, false, 1F, 0F, ageInTicks, flapAmount);
        this.walk(tail, flapSpeed, flapDegree * 0.3F, false, -3F, -0.1F, ageInTicks, flapAmount);
        this.bob(body, flapSpeed, flapDegree * 10, false, ageInTicks, flapAmount);
        this.bob(head, flapSpeed, flapDegree * -6, false, ageInTicks, flapAmount);
        if (flyProgress <= 0.0F) {
            this.bob(body, walkSpeed * 1F, walkDegree * 1.3F, true, limbSwing, limbSwingAmount);
            this.walk(rightLeg, walkSpeed, walkDegree * 1.85F, false, 0F, 0.2F, limbSwing, limbSwingAmount);
            this.walk(leftLeg, walkSpeed, walkDegree * 1.85F, true, 0F, 0.2F, limbSwing, limbSwingAmount);
            this.walk(head, walkSpeed, walkDegree * 0.2F, false, 2F, -0.01F, limbSwing, limbSwingAmount);
            this.walk(tail, walkSpeed, walkDegree * 0.5F, false, 1F, 0F, limbSwing, limbSwingAmount);
        }
        this.walk(tail, idleSpeed, idleDegree, false, 1F, 0F, ageInTicks, 1);
        this.walk(crest, idleSpeed, idleDegree, false, 2F, 0F, ageInTicks, 1);
        this.bob(head, idleSpeed, idleDegree * 1.5F, true, ageInTicks, 1);
        this.faceTarget(netHeadYaw, headPitch, 1.3F, head);
        this.body.rotateAngleX += birdPitch * flyProgress * 0.2F * ((float) Math.PI / 180F);
        if (entity.getFeedTime() > 0) {
            this.flap(head, 0.4F, 0.4F, false, 1F, 0F, ageInTicks, 1);
        }
        if (entity.getSingTime() > 0) {
            this.flap(head, 0.4F, 0.4F, false, 1F, 0F, ageInTicks, 1);
            this.walk(crest, 0.4F, 0.3F, false, 1F, 0.1F, ageInTicks, 1);
            this.swing(head, 0.4F, 0.4F, false, 2F, 0F, ageInTicks, 1);
            head.rotationPointZ += (float) (Math.sin(ageInTicks * -0.4 - 1F));
        }
    }

    @Override
    public void render(Entity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
        if (this.isChild) {
            float f = 1.35F;
            head.setScale(f, f, f);
            head.setShouldScaleChildren(true);
            net.minecraft.client.renderer.GlStateManager.pushMatrix();
            net.minecraft.client.renderer.GlStateManager.scale(0.5F, 0.5F, 0.5F);
            net.minecraft.client.renderer.GlStateManager.translate(0.0D, 1.5D, 0.0D);
            for (ModelRenderer part : this.getParts()) {
                part.render(scale);
            }
            net.minecraft.client.renderer.GlStateManager.popMatrix();
            this.head.setScale(1F, 1F, 1F);
            this.head.setShouldScaleChildren(false);
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
