package com.github.alexthe666.alexsmobs.client.model;

import com.github.alexthe666.alexsmobs.tileentity.TileEntityVoidWormBeak;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;

/**
 * 1.12 port of 1.16 {@code ModelVoidWormBeak}. Uses vanilla {@link ModelRenderer} display lists so the
 * block TESR path renders reliably (Citadel {@code AdvancedModelBox} immediate-mode draws are unstable in TESR).
 * Geometry, pivot, and {@link #renderBeak} animation math match the 1.16 source 1:1.
 */
public class ModelVoidWormBeak extends ModelBase {
    private final ModelRenderer root;
    private final ModelRenderer left;
    private final ModelRenderer right;

    public ModelVoidWormBeak() {
        textureWidth = 64;
        textureHeight = 64;
        root = new ModelRenderer(this);
        root.setRotationPoint(0.0F, 24.0F, 0.0F);
        left = new ModelRenderer(this, 0, 0);
        left.setRotationPoint(0.0F, 0.0F, 0.0F);
        root.addChild(left);
        left.addBox(-0.1F, -12.9F, -3.5F, 7, 13, 7, -0.1F);
        right = new ModelRenderer(this, 0, 21);
        right.setRotationPoint(0.0F, 0.0F, 0.0F);
        root.addChild(right);
        right.addBox(-7.0F, -13.0F, -3.5F, 7, 13, 7, 0.0F);
    }

    @Override
    public void render(Entity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
        root.render(scale);
    }

    public void renderBeak(TileEntityVoidWormBeak beak, float partialTick) {
        left.rotateAngleX = 0.0F;
        left.rotateAngleY = 0.0F;
        left.rotateAngleZ = 0.0F;
        right.rotateAngleX = 0.0F;
        right.rotateAngleY = 0.0F;
        right.rotateAngleZ = 0.0F;
        left.rotationPointX = 0.0F;
        left.rotationPointY = 0.0F;
        left.rotationPointZ = 0.0F;
        right.rotationPointX = 0.0F;
        right.rotationPointY = 0.0F;
        right.rotationPointZ = 0.0F;

        float amount = beak.getChompProgress(partialTick) * 0.2F;
        float ageInTicks = beak.ticksExisted + partialTick;
        left.rotateAngleZ += MathHelper.cos(ageInTicks * 0.5F) * 0.5F * amount + 0.3F * amount;
        right.rotateAngleZ += MathHelper.cos(ageInTicks * 0.5F) * -0.5F * amount + -0.3F * amount;
        float rotation = MathHelper.cos(ageInTicks * 0.5F) * 0.5F * amount + 0.3F * amount;
        left.rotationPointY -= rotation * 4.5F;
        right.rotationPointY -= rotation * 4.5F;
    }
}
