package com.github.alexthe666.alexsmobs.client.model;

import com.github.alexthe666.alexsmobs.entity.EntityCombJelly;
import com.github.alexthe666.citadel.client.model.AdvancedEntityModel;
import com.github.alexthe666.citadel.client.model.AdvancedModelBox;
import com.google.common.collect.ImmutableList;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;

public class ModelCombJelly extends AdvancedEntityModel<EntityCombJelly> {
    private final AdvancedModelBox root;
    private final AdvancedModelBox body;
    private final AdvancedModelBox innerBody;

    public ModelCombJelly(float inflate) {
        textureWidth = 64;
        textureHeight = 64;
        root = new AdvancedModelBox(this, "root");
        root.setRotationPoint(0.0F, 24.0F, 0.0F);
        body = new AdvancedModelBox(this, "body");
        body.setRotationPoint(0.0F, -13.0F, 0.0F);
        root.addChild(body);
        body.setTextureOffset(0, 0).func_228303_a_(-5.0F, -2.0F, -5.0F, 10.0F, 15.0F, 10.0F, inflate, false);
        innerBody = new AdvancedModelBox(this, "inner_body");
        innerBody.setRotationPoint(0.0F, -3.0F, 0.0F);
        body.addChild(innerBody);
        innerBody.setTextureOffset(40, 6).func_228303_a_(-3.0F, -1.0F, -3.0F, 6.0F, 13.0F, 6.0F, inflate, false);
        this.updateDefaultPose();
    }

    public Iterable<AdvancedModelBox> getAllParts() {
        return ImmutableList.of(root, body, innerBody);
    }

    public void setRotationAngles(EntityCombJelly entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.resetToDefaultPose();
        float partialTick = ageInTicks - entity.ticksExisted;
        float jellyPitch = entity.prevjellyPitch + (entity.getJellyPitch() - entity.prevjellyPitch) * partialTick;
        float landProgress = entity.prevOnLandProgress + (entity.onLandProgress - entity.prevOnLandProgress) * partialTick;
        float girateSpeed = 0.1F * ageInTicks * (1F - landProgress * 0.2F);
        float widthScale = 0.95F + (float) Math.sin(girateSpeed) * 0.1F;
        float heightScale = 0.95F + (float) Math.cos(girateSpeed) * 0.1F;
        float squishedScale = widthScale - 0.1F * landProgress;
        this.body.setScale(squishedScale, heightScale, widthScale);
        this.innerBody.setScale(squishedScale, heightScale, widthScale);
        this.body.rotateAngleX = jellyPitch * ((float) Math.PI / 180F) * (1F - landProgress * 0.2F);
        this.body.rotateAngleZ = landProgress * 0.2F * ((float) Math.PI / 2F);
        this.body.rotationPointY += landProgress * 1.85F;
        this.body.rotationPointY += Math.abs(jellyPitch * 0.07F);
        this.body.rotationPointX += landProgress;
        this.bob(body, 0.1F, 1F, false, ageInTicks, 1F - landProgress * 0.2F);
    }

    public Iterable<ModelRenderer> getParts() {
        return ImmutableList.of(root);
    }

    @Override
    public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor, Entity entityIn) {
        setRotationAngles((EntityCombJelly) entityIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
    }

    @Override
    public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5) {
        getParts().forEach(part -> part.render(f5));
    }
}
