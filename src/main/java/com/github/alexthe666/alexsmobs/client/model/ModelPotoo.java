package com.github.alexthe666.alexsmobs.client.model;

import com.github.alexthe666.alexsmobs.entity.EntityPotoo;
import com.github.alexthe666.alexsmobs.entity.util.Maths;
import com.github.alexthe666.citadel.client.model.AdvancedEntityModel;
import com.github.alexthe666.citadel.client.model.AdvancedModelBox;
import com.google.common.collect.ImmutableList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;

public class ModelPotoo extends AdvancedEntityModel<EntityPotoo> {

    private final AdvancedModelBox root;
    private final AdvancedModelBox body;
    private final AdvancedModelBox tail;
    private final AdvancedModelBox left_wing;
    private final AdvancedModelBox right_wing;
    private final AdvancedModelBox left_foot;
    private final AdvancedModelBox right_foot;
    private final AdvancedModelBox head;
    private final AdvancedModelBox left_eye;
    private final AdvancedModelBox left_pupil;
    private final AdvancedModelBox right_eye;
    private final AdvancedModelBox right_pupil;

    public ModelPotoo() {
        textureWidth = 64;
        textureHeight = 64;

        root = new AdvancedModelBox(this);
        root.setRotationPoint(0.0F, 24.0F, 0.0F);

        body = new AdvancedModelBox(this);
        body.setRotationPoint(0.0F, -5.0F, 0.0F);
        root.addChild(body);
        body.setTextureOffset(0, 0).func_228303_a_(-3.5F, -4.0F, -3.0F, 7.0F, 8.0F, 6.0F, 0.0F, false);

        tail = new AdvancedModelBox(this);
        tail.setRotationPoint(0.0F, 4.0F, 2.0F);
        body.addChild(tail);
        tail.setTextureOffset(0, 26).func_228303_a_(-2.5F, 0.0F, -1.0F, 5.0F, 8.0F, 2.0F, 0.0F, false);

        left_wing = new AdvancedModelBox(this);
        left_wing.setRotationPoint(3.5F, -2.0F, 0.0F);
        body.addChild(left_wing);
        left_wing.setTextureOffset(22, 21).func_228303_a_(0.0F, -1.0F, -2.0F, 1.0F, 8.0F, 5.0F, 0.0F, false);

        right_wing = new AdvancedModelBox(this);
        right_wing.setRotationPoint(-3.5F, -2.0F, 0.0F);
        body.addChild(right_wing);
        right_wing.setTextureOffset(22, 21).func_228303_a_(-1.0F, -1.0F, -2.0F, 1.0F, 8.0F, 5.0F, 0.0F, true);

        left_foot = new AdvancedModelBox(this);
        left_foot.setRotationPoint(2.5F, 3.9F, -2.0F);
        body.addChild(left_foot);
        left_foot.setTextureOffset(21, 0).func_228303_a_(-1.5F, 0.0F, -2.0F, 3.0F, 2.0F, 3.0F, 0.0F, false);

        right_foot = new AdvancedModelBox(this);
        right_foot.setRotationPoint(-2.5F, 3.9F, -2.0F);
        body.addChild(right_foot);
        right_foot.setTextureOffset(21, 0).func_228303_a_(-1.5F, 0.0F, -2.0F, 3.0F, 2.0F, 3.0F, 0.0F, true);

        head = new AdvancedModelBox(this);
        head.setRotationPoint(0.0F, -4.0F, 3.0F);
        body.addChild(head);
        head.setTextureOffset(0, 15).func_228303_a_(-3.5F, -4.0F, -6.0F, 7.0F, 4.0F, 6.0F, 0.0F, false);
        head.setTextureOffset(21, 9).func_228303_a_(-3.5F, -0.7F, -6.0F, 7.0F, 0.0F, 6.0F, 0.0F, false);
        head.setTextureOffset(0, 0).func_228303_a_(-0.5F, -1.0F, -7.0F, 1.0F, 2.0F, 1.0F, 0.0F, false);

        left_eye = new AdvancedModelBox(this);
        left_eye.setRotationPoint(4.0F, -2.4F, -4.4F);
        head.addChild(left_eye);
        left_eye.setTextureOffset(30, 16).func_228303_a_(-1.0F, -1.5F, -1.5F, 2.0F, 3.0F, 3.0F, 0.0F, false);

        left_pupil = new AdvancedModelBox(this);
        left_pupil.setRotationPoint(0.1F, 0.0F, 0.0F);
        left_eye.addChild(left_pupil);
        left_pupil.setTextureOffset(21, 16).func_228303_a_(-1.08F, -0.5F, -0.5F, 2.0F, 1.0F, 1.0F, 0.0F, false);

        right_eye = new AdvancedModelBox(this);
        right_eye.setRotationPoint(-4.0F, -2.4F, -4.4F);
        head.addChild(right_eye);
        right_eye.setTextureOffset(30, 16).func_228303_a_(-1.0F, -1.5F, -1.5F, 2.0F, 3.0F, 3.0F, 0.0F, true);

        right_pupil = new AdvancedModelBox(this);
        right_pupil.setRotationPoint(-0.1F, 0.0F, 0.0F);
        right_eye.addChild(right_pupil);
        right_pupil.setTextureOffset(21, 16).func_228303_a_(-0.92F, -0.5F, -0.5F, 2.0F, 1.0F, 1.0F, 0.0F, true);
        this.updateDefaultPose();
    }

    @Override
    public Iterable<AdvancedModelBox> getAllParts() {
        return ImmutableList.of(root, body, tail, left_wing, right_wing, head, left_foot, left_pupil, left_eye, right_foot, right_pupil, right_eye);
    }

    public Iterable<ModelRenderer> getParts() {
        return ImmutableList.of(root);
    }

    @Override
    public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor, Entity entityIn) {
        setRotationAngles((EntityPotoo) entityIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
    }

    public void setRotationAngles(EntityPotoo entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.resetToDefaultPose();
        float partialTick = Minecraft.getMinecraft().getRenderPartialTicks();
        float flyProgress = entity.prevFlyProgress + (entity.flyProgress - entity.prevFlyProgress) * partialTick;
        float perchProgress = entity.prevPerchProgress + (entity.perchProgress - entity.prevPerchProgress) * partialTick;
        float mouthProgress = entity.prevMouthProgress + (entity.mouthProgress - entity.prevMouthProgress) * partialTick;
        float flapSpeed = 0.8F;
        float flapDegree = 0.2F;
        float walkSpeed = 1.6f;
        float walkDegree = 0.8f;
        float eyeScale = MathHelper.clamp((15F - entity.getEyeScale(10, partialTick)) / 15F, 0F, 1F);
        this.left_pupil.setScale(0.5F, 1.0F + eyeScale * 2.1F, 1.0F + eyeScale * 2.1F);
        this.left_pupil.rotationPointX += 0.5F;
        this.right_pupil.setScale(0.5F, 1.0F + eyeScale * 2.1F, 1.0F + eyeScale * 2.1F);
        this.right_pupil.rotationPointX -= 0.5F;
        if (entity.isSleeping()) {
            this.right_eye.showModel = false;
            this.right_pupil.showModel = false;
            this.left_eye.showModel = false;
            this.left_pupil.showModel = false;
        } else {
            this.right_eye.showModel = true;
            this.right_pupil.showModel = true;
            this.left_eye.showModel = true;
            this.left_pupil.showModel = true;
        }
        float walkAmount = (5F - flyProgress) * 0.2F;
        float walkSwingAmount = limbSwingAmount * walkAmount;

        progressRotationPrev(body, Math.min(walkSwingAmount, 0.5F), Maths.rad(20), 0, 0, 0.5F);
        progressRotationPrev(left_foot, Math.min(walkSwingAmount, 0.5F), Maths.rad(-20), 0, 0, 0.5F);
        progressRotationPrev(right_foot, Math.min(walkSwingAmount, 0.5F), Maths.rad(-20), 0, 0, 0.5F);
        progressRotationPrev(tail, 5F - perchProgress, Maths.rad(85), 0, 0, 5f);
        progressRotationPrev(body, flyProgress * limbSwingAmount, Maths.rad(80), 0, 0, 5F);
        progressRotationPrev(right_wing, flyProgress, Maths.rad(-90), Maths.rad(90), 0, 5F);
        progressRotationPrev(left_wing, flyProgress, Maths.rad(-90), Maths.rad(-90), 0, 5F);
        progressRotationPrev(tail, flyProgress, Maths.rad(-60), 0, 0, 5F);
        progressRotationPrev(head, mouthProgress, Maths.rad(-70), 0F, 0, 5F);
        progressPositionPrev(head, mouthProgress, 0F, 0.5F, -0.5F, 5F);
        this.flap(body, walkSpeed, walkDegree * 0.2F, true, 0F, 0F, limbSwing, walkSwingAmount);
        if (flyProgress > 0) {
            this.bob(body, flapSpeed * 0.5F, flapDegree * 4, true, ageInTicks, 1);
            this.swing(right_wing, flapSpeed, flapDegree * 5, true, 0F, 0F, ageInTicks, 1);
            this.swing(left_wing, flapSpeed, flapDegree * 5, false, 0F, 0F, ageInTicks, 1);
        }
        this.left_foot.rotationPointZ += 2 * (float) (Math.sin((double) (limbSwing * walkSpeed) - 2.5F) * (double) walkSwingAmount * (double) walkDegree - (double) (walkSwingAmount * walkDegree));
        this.right_foot.rotationPointZ += 2 * (float) (Math.sin(-(double) (limbSwing * walkSpeed) + 2.5F) * (double) walkSwingAmount * (double) walkDegree - (double) (walkSwingAmount * walkDegree));
        this.left_foot.rotationPointY += (float) (Math.sin((double) (limbSwing * walkSpeed) - 2.5F) * (double) walkSwingAmount * (double) walkDegree - (double) (walkSwingAmount * walkDegree));
        this.right_foot.rotationPointY += (float) (Math.sin(-(double) (limbSwing * walkSpeed) + 2.5F) * (double) walkSwingAmount * (double) walkDegree - (double) (walkSwingAmount * walkDegree));
    }

    @Override
    public void render(Entity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
        if (this.isChild) {
            float f = 1.25F;
            right_eye.setScale(f, f, f);
            left_eye.setScale(f, f, f);
            right_eye.setShouldScaleChildren(true);
            left_eye.setShouldScaleChildren(true);
            GlStateManager.pushMatrix();
            GlStateManager.scale(0.5F, 0.5F, 0.5F);
            GlStateManager.translate(0.0D, 1.5D, 0.125D);
            for (ModelRenderer part : this.getParts()) {
                part.render(scale);
            }
            GlStateManager.popMatrix();
            right_eye.setScale(1, 1, 1);
            left_eye.setScale(1, 1, 1);
        } else {
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
