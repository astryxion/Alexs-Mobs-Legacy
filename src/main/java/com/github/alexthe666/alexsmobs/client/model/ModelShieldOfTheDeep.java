package com.github.alexthe666.alexsmobs.client.model;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

public class ModelShieldOfTheDeep extends ModelBase {
    private final ModelRenderer shield;
    private final ModelRenderer handle;

    public ModelShieldOfTheDeep() {
        this.textureWidth = 64;
        this.textureHeight = 64;

        shield = new ModelRenderer(this);
        shield.setRotationPoint(-2.0F, 16.0F, 0.0F);
        shield.setTextureOffset(0, 0).addBox(-1.0F, -4.0F, -6.0F, 1, 12, 12, 0.0F);
        shield.setTextureOffset(17, 15).addBox(-3.0F, -3.0F, -5.0F, 2, 10, 10, 0.0F);
        shield.setTextureOffset(27, 0).addBox(-4.0F, -1.0F, -3.0F, 3, 6, 6, 0.0F);

        handle = new ModelRenderer(this);
        handle.setRotationPoint(8.0F, 8.0F, -8.0F);
        shield.addChild(handle);
        handle.setTextureOffset(0, 25).addBox(-8.0F, -8.5F, 7.0F, 5, 5, 2, 0.0F);
    }

    @Override
    public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor, Entity entityIn) {
    }

    @Override
    public void render(Entity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
        this.shield.render(scale);
    }

    public void setRotationAngle(ModelRenderer modelRenderer, float x, float y, float z) {
        modelRenderer.rotateAngleX = x;
        modelRenderer.rotateAngleY = y;
        modelRenderer.rotateAngleZ = z;
    }
}
