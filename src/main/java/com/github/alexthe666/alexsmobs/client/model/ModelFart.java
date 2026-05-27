package com.github.alexthe666.alexsmobs.client.model;

import com.github.alexthe666.alexsmobs.entity.EntityFart;
import com.github.alexthe666.citadel.client.model.AdvancedEntityModel;
import com.github.alexthe666.citadel.client.model.AdvancedModelBox;
import com.google.common.collect.ImmutableList;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

public class ModelFart extends AdvancedEntityModel<EntityFart> {
    private final AdvancedModelBox main;
    private final AdvancedModelBox cube_r1;
    private final AdvancedModelBox cube_r2;

    public ModelFart() {
        textureWidth = 64;
        textureHeight = 64;

        main = new AdvancedModelBox(this, "main");
        main.setRotationPoint(0.0F, 0.0F, 0.0F);
        main.setTextureOffset(0, 0).func_228303_a_(-4.0F, -4.0F, -5.0F, 8.0F, 8.0F, 11.0F, 0.0F, false);

        cube_r1 = new AdvancedModelBox(this, "cube_r1");
        cube_r1.setRotationPoint(0.0F, 0.0F, 0.5F);
        main.addChild(cube_r1);
        setRotationAngle(cube_r1, 0.0F, 0.0F, -0.7854F);
        cube_r1.setTextureOffset(0, 20).func_228303_a_(0.0F, -4.0F, -2.5F, 0.01F, 8.0F, 11.0F, 0.0F, true);

        cube_r2 = new AdvancedModelBox(this, "cube_r2");
        cube_r2.setRotationPoint(0.0F, 0.0F, 0.5F);
        main.addChild(cube_r2);
        setRotationAngle(cube_r2, 0.0F, 0.0F, 0.7854F);
        cube_r2.setTextureOffset(0, 20).func_228303_a_(0.0F, -4.0F, -2.5F, 0.01F, 8.0F, 11.0F, 0.0F, false);
        this.updateDefaultPose();
    }

    public Iterable<ModelRenderer> getParts() {
        return ImmutableList.of(main);
    }

    @Override
    public Iterable<AdvancedModelBox> getAllParts() {
        return ImmutableList.of(main, cube_r1, cube_r2);
    }

    public void setRotationAngles(EntityFart entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.resetToDefaultPose();
        float f = Math.min(entityIn.ticksExisted + ageInTicks, 30F) / 30F;
        float expand = 1.5F * f;
        this.main.setScale(expand * 2F + 1F, expand * 2F + 1F, 1F);
        this.cube_r1.setScale(1F, 1F, expand * 1.5F + 1F);
        this.cube_r2.setScale(1F, 1F, expand * 1.5F + 1F);
        this.cube_r1.rotationPointZ += expand * 3F;
        this.cube_r2.rotationPointZ += expand * 3F;
    }

    public void setRotationAngle(AdvancedModelBox modelRenderer, float x, float y, float z) {
        modelRenderer.rotateAngleX = x;
        modelRenderer.rotateAngleY = y;
        modelRenderer.rotateAngleZ = z;
    }

    @Override
    public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor, Entity entityIn) {
        setRotationAngles((EntityFart) entityIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
    }

    @Override
    public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5) {
        getParts().forEach(part -> part.render(f5));
    }
}
