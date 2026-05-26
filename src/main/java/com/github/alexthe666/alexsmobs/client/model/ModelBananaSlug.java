package com.github.alexthe666.alexsmobs.client.model;

import com.github.alexthe666.alexsmobs.entity.EntityBananaSlug;
import com.github.alexthe666.citadel.client.model.AdvancedEntityModel;
import com.github.alexthe666.citadel.client.model.AdvancedModelBox;
import com.google.common.collect.ImmutableList;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;

public class ModelBananaSlug extends AdvancedEntityModel<EntityBananaSlug> {

    private final AdvancedModelBox root;
    private final AdvancedModelBox body;
    private final AdvancedModelBox goo;
    private final AdvancedModelBox leftAntenna;
    private final AdvancedModelBox rightAntenna;
    private final AdvancedModelBox tail;

    public ModelBananaSlug() {
        textureWidth = 64;
        textureHeight = 64;

        root = new AdvancedModelBox(this, "root");
        root.setRotationPoint(0.0F, 24.0F, 0.0F);

        body = new AdvancedModelBox(this, "body");
        body.setRotationPoint(0.0F, -2.0F, -2.0F);
        root.addChild(body);
        body.setTextureOffset(18, 23).func_228303_a_(-2.5F, -2.0F, -4.0F, 5.0F, 4.0F, 7.0F, 0.0F, false);

        goo = new AdvancedModelBox(this, "goo");
        goo.setRotationPoint(0.0F, 2.0F, 0.0F);
        body.addChild(goo);
        goo.setTextureOffset(0, 0).func_228303_a_(-2.5F, -0.001F, 0.0F, 5.0F, 0.0F, 17.0F, 0.0F, false);

        leftAntenna = new AdvancedModelBox(this, "leftAntenna");
        leftAntenna.setRotationPoint(2.0F, -1.0F, -4.0F);
        body.addChild(leftAntenna);
        setRotationAngle(leftAntenna, 0.0F, 0.0F, -0.0873F);
        leftAntenna.setTextureOffset(0, 0).func_228303_a_(0.0F, -1.0F, -5.0F, 0.0F, 3.0F, 5.0F, 0.0F, false);

        rightAntenna = new AdvancedModelBox(this, "rightAntenna");
        rightAntenna.setRotationPoint(-2.0F, -1.0F, -4.0F);
        body.addChild(rightAntenna);
        setRotationAngle(rightAntenna, 0.0F, 0.0F, 0.0873F);
        rightAntenna.setTextureOffset(0, 0).func_228303_a_(0.0F, -1.0F, -5.0F, 0.0F, 3.0F, 5.0F, 0.0F, true);

        tail = new AdvancedModelBox(this, "tail");
        tail.setRotationPoint(0.0F, 0.0F, 3.0F);
        body.addChild(tail);
        tail.setTextureOffset(0, 18).func_228303_a_(-2.0F, -1.0F, 0.0F, 4.0F, 3.0F, 8.0F, 0.0F, false);
        this.updateDefaultPose();
    }

    public Iterable<ModelRenderer> getParts() {
        return ImmutableList.of(root);
    }

    @Override
    public Iterable<AdvancedModelBox> getAllParts() {
        return ImmutableList.of(root, body, goo, tail, leftAntenna, rightAntenna);
    }

    @Override
    public void render(Entity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
        if (this.isChild) {
            GlStateManager.pushMatrix();
            GlStateManager.scale(0.65F, 0.65F, 0.65F);
            GlStateManager.translate(0.0D, 0.8D, 0.125D);
            for (ModelRenderer part : this.getParts()) {
                part.render(scale);
            }
            GlStateManager.popMatrix();
        } else {
            for (ModelRenderer part : this.getParts()) {
                part.render(scale);
            }
        }
    }

    @Override
    public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale, Entity entityIn) {
        if (!(entityIn instanceof EntityBananaSlug)) {
            return;
        }
        EntityBananaSlug entity = (EntityBananaSlug) entityIn;
        this.resetToDefaultPose();
        float idleSpeed = 0.25F;
        float idleDegree = 0.25F;
        float walkSpeed = 1F;
        float walkDegree = 0.2F;
        float partialTick = ageInTicks - entity.ticksExisted;
        this.swing(leftAntenna, idleSpeed, idleDegree * 0.2F, true, 1, 0.1F, ageInTicks, 1);
        this.swing(rightAntenna, idleSpeed, idleDegree * 0.2F, false, 1, 0.1F, ageInTicks, 1);
        this.walk(leftAntenna, idleSpeed, idleDegree * 0.5F, true, 3, 0.1F, ageInTicks, 1);
        this.walk(rightAntenna, idleSpeed, idleDegree * 0.5F, true, 3, 0.1F, ageInTicks, 1);
        this.swing(tail, walkSpeed, walkDegree, true, 3F, 0F, limbSwing, limbSwingAmount);
        float antennaBack = -0.5F + (float) (Math.sin((double) (ageInTicks * idleSpeed) + 3)) * 0.2F;
        leftAntenna.rotationPointZ -= antennaBack;
        rightAntenna.rotationPointZ -= antennaBack;
        float stretch1 = (float) (Math.sin(limbSwing * -walkSpeed) * limbSwingAmount) + limbSwingAmount;
        float stretch2 = (float) (Math.sin(limbSwing * -walkSpeed + 1F) * limbSwingAmount) + limbSwingAmount;
        body.setScale(1, (1 - stretch1 * 0.025F), (1 + stretch1 * 0.25F));
        tail.setScale(1, (1 - stretch2 * 0.05F), (1 + stretch2 * 0.5F));
        body.setShouldScaleChildren(false);
        body.rotationPointZ -= stretch1 * 2F;
        leftAntenna.rotationPointZ -= stretch1;
        rightAntenna.rotationPointZ -= stretch1;
        leftAntenna.rotateAngleY += netHeadYaw * 0.6F * ((float) Math.PI / 180F);
        leftAntenna.rotateAngleX += headPitch * 0.3F * ((float) Math.PI / 180F);
        rightAntenna.rotateAngleY += netHeadYaw * 0.6F * ((float) Math.PI / 180F);
        rightAntenna.rotateAngleX += headPitch * 0.3F * ((float) Math.PI / 180F);
        float yaw = entity.prevRenderYawOffset + (entity.renderYawOffset - entity.prevRenderYawOffset) * partialTick;
        float slimeYaw = MathHelper.wrapDegrees(entity.prevTrailYaw + (entity.trailYaw - entity.prevTrailYaw) * partialTick - yaw) * 0.65F;
        goo.rotationPointX = (MathHelper.sin(limbSwing * -walkSpeed - 1F) * limbSwingAmount);
        goo.rotateAngleY += slimeYaw * ((float) Math.PI / 180F);
        tail.rotateAngleY += slimeYaw * 0.8F * ((float) Math.PI / 180F);
        goo.setScale(1, 0, (1 + limbSwingAmount));
    }

    public void setRotationAngle(AdvancedModelBox box, float x, float y, float z) {
        box.rotateAngleX = x;
        box.rotateAngleY = y;
        box.rotateAngleZ = z;
    }
}
