package com.github.alexthe666.alexsmobs.client.model;

import com.github.alexthe666.alexsmobs.entity.util.FlyingFishBootsUtil;
import com.github.alexthe666.alexsmobs.entity.util.Maths;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ModelFlyingFishBoots extends ModelBiped {

    private final ModelRenderer rightFish;
    private final ModelRenderer leftFish;
    private final ModelRenderer rightWingOuter;
    private final ModelRenderer leftWingOuter;
    private final ModelRenderer rightWingInner;
    private final ModelRenderer leftWingInner;

    public ModelFlyingFishBoots(float size) {
        super(size, 0.0F, 64, 64);
        this.rightFish = new ModelRenderer(this);
        this.rightFish.setRotationPoint(-0.1F, 10.0F, 0.0F);
        this.rightFish.setTextureOffset(18, 12).addBox(-1.9F, -3.0F, -2.0F, 4, 3, 4, 0.3F);
        this.rightFish.setTextureOffset(0, 25).addBox(0.0F, -2.0F, 2.0F, 0, 4, 5, 0.0F);
        this.rightFish.setTextureOffset(0, 0).addBox(-2.5F, 0.0F, -5.0F, 5, 2, 9, 0.0F);
        this.rightFish.mirror = true;
        this.bipedRightLeg.addChild(this.rightFish);

        this.rightWingOuter = new ModelRenderer(this, 9, 47);
        this.rightWingOuter.mirror = true;
        this.rightWingOuter.setRotationPoint(-2.5F, 1.0F, -3.0F);
        this.rightWingOuter.addBox(0.0F, -3.0F, 0.0F, 0, 4, 8, 0.0F);
        this.rightWingOuter.rotateAngleY = -0.5672F;
        this.rightFish.addChild(this.rightWingOuter);

        this.rightWingInner = new ModelRenderer(this, 0, 42);
        this.rightWingInner.mirror = true;
        this.rightWingInner.setRotationPoint(2.5F, 1.0F, -3.0F);
        this.rightWingInner.addBox(0.0F, -3.0F, 0.0F, 0, 4, 8, 0.0F);
        this.rightWingInner.rotateAngleY = 0.5672F;
        this.rightFish.addChild(this.rightWingInner);

        this.leftFish = new ModelRenderer(this);
        this.leftFish.setRotationPoint(0.1F, 10.0F, 0.0F);
        this.leftFish.setTextureOffset(18, 12).addBox(-2.1F, -3.0F, -2.0F, 4, 3, 4, 0.3F);
        this.leftFish.setTextureOffset(0, 25).addBox(0.0F, -2.0F, 2.0F, 0, 4, 5, 0.0F);
        this.leftFish.setTextureOffset(0, 0).addBox(-2.5F, 0.0F, -5.0F, 5, 2, 9, 0.0F);
        this.bipedLeftLeg.addChild(this.leftFish);

        this.leftWingOuter = new ModelRenderer(this, 9, 47);
        this.leftWingOuter.setRotationPoint(2.5F, 1.0F, -3.0F);
        this.leftWingOuter.addBox(0.0F, -3.0F, 0.0F, 0, 4, 8, 0.0F);
        this.leftWingOuter.rotateAngleY = 0.5672F;
        this.leftFish.addChild(this.leftWingOuter);

        this.leftWingInner = new ModelRenderer(this, 0, 42);
        this.leftWingInner.setRotationPoint(-2.5F, 1.0F, -3.0F);
        this.leftWingInner.addBox(0.0F, -3.0F, 0.0F, 0, 4, 8, 0.0F);
        this.leftWingInner.rotateAngleY = -0.5672F;
        this.leftFish.addChild(this.leftWingInner);
    }

    public ModelFlyingFishBoots withAnimations(EntityLivingBase entity) {
        if (entity != null) {
            float ageInTicks = entity.ticksExisted;
            float fly = (float) Math.cos(ageInTicks * 0.2F) * 0.1F;
            float fly2 = fly * 0.35F;
            boolean flying = FlyingFishBootsUtil.getBoostTicks(entity) > 0;
            if (flying) {
                fly = (1.0F + (float) Math.sin(ageInTicks * 1.2F)) * 0.8F;
                fly2 = fly;
            }
            this.rightWingOuter.rotateAngleY = -0.5672F - fly;
            this.leftWingOuter.rotateAngleY = 0.5672F + fly;
            this.rightWingInner.rotateAngleY = 0.5672F + fly2;
            this.leftWingInner.rotateAngleY = -0.5672F - fly2;
            if (flying || entity.isInWater()) {
                this.leftFish.rotateAngleX = Maths.rad(-45);
                this.rightFish.rotateAngleX = Maths.rad(-45);
                this.rightFish.rotationPointY = 11.0F;
                this.leftFish.rotationPointY = 11.0F;
                this.rightFish.rotationPointZ = -1.5F;
                this.leftFish.rotationPointZ = -1.5F;
            } else if (entity.isSneaking()) {
                this.leftFish.rotateAngleX = 0.0F;
                this.rightFish.rotateAngleX = 0.0F;
                this.rightFish.rotationPointY = 8.0F;
                this.leftFish.rotationPointY = 8.0F;
                this.rightFish.rotationPointZ = 0.0F;
                this.leftFish.rotationPointZ = 0.0F;
            } else {
                this.leftFish.rotateAngleX = 0.0F;
                this.rightFish.rotateAngleX = 0.0F;
                this.rightFish.rotationPointY = 10.0F;
                this.leftFish.rotationPointY = 10.0F;
                this.rightFish.rotationPointZ = 0.0F;
                this.leftFish.rotationPointZ = 0.0F;
            }
        }
        return this;
    }

    @Override
    public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor, Entity entityIn) {
        if (entityIn instanceof EntityArmorStand) {
            EntityArmorStand stand = (EntityArmorStand) entityIn;
            this.bipedHead.rotateAngleX = 0.017453292F * stand.getHeadRotation().getX();
            this.bipedHead.rotateAngleY = 0.017453292F * stand.getHeadRotation().getY();
            this.bipedHead.rotateAngleZ = 0.017453292F * stand.getHeadRotation().getZ();
            this.bipedBody.rotateAngleX = 0.017453292F * stand.getBodyRotation().getX();
            this.bipedBody.rotateAngleY = 0.017453292F * stand.getBodyRotation().getY();
            this.bipedBody.rotateAngleZ = 0.017453292F * stand.getBodyRotation().getZ();
            this.bipedLeftLeg.rotateAngleX = 0.017453292F * stand.getLeftLegRotation().getX();
            this.bipedLeftLeg.rotateAngleY = 0.017453292F * stand.getLeftLegRotation().getY();
            this.bipedLeftLeg.rotateAngleZ = 0.017453292F * stand.getLeftLegRotation().getZ();
            this.bipedLeftLeg.setRotationPoint(1.9F, 11.0F, 0.0F);
            this.bipedRightLeg.rotateAngleX = 0.017453292F * stand.getRightLegRotation().getX();
            this.bipedRightLeg.rotateAngleY = 0.017453292F * stand.getRightLegRotation().getY();
            this.bipedRightLeg.rotateAngleZ = 0.017453292F * stand.getRightLegRotation().getZ();
            this.bipedRightLeg.setRotationPoint(-1.9F, 11.0F, 0.0F);
            copyModelAngles(this.bipedHead, this.bipedHeadwear);
        } else {
            super.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor, entityIn);
        }
    }

    public static void copyModelAngles(ModelRenderer from, ModelRenderer to) {
        to.rotateAngleX = from.rotateAngleX;
        to.rotateAngleY = from.rotateAngleY;
        to.rotateAngleZ = from.rotateAngleZ;
    }
}
