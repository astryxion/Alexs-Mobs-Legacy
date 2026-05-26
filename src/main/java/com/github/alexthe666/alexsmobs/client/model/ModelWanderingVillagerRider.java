package com.github.alexthe666.alexsmobs.client.model;

import net.minecraft.client.model.ModelVillager;
import net.minecraft.entity.Entity;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ModelWanderingVillagerRider extends ModelVillager {

    public ModelWanderingVillagerRider() {
        super(0.0F);
    }

    @Override
    public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor, Entity entity) {
        super.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor, entity);
        boolean sitting = entity.isRiding();
        if (sitting) {
            this.rightVillagerLeg.rotateAngleX = -1.4137167F;
            this.rightVillagerLeg.rotateAngleY = 0.31415927F;
            this.rightVillagerLeg.rotateAngleZ = 0.07853982F;
            this.leftVillagerLeg.rotateAngleX = -1.4137167F;
            this.leftVillagerLeg.rotateAngleY = -0.31415927F;
            this.leftVillagerLeg.rotateAngleZ = -0.07853982F;
        } else {
            this.rightVillagerLeg.rotateAngleY = 0F;
            this.rightVillagerLeg.rotateAngleZ = 0F;
            this.leftVillagerLeg.rotateAngleY = 0F;
            this.leftVillagerLeg.rotateAngleZ = 0F;
        }
    }
}
