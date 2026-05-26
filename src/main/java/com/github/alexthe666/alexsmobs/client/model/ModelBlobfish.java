package com.github.alexthe666.alexsmobs.client.model;

import net.minecraft.entity.Entity;

import com.github.alexthe666.alexsmobs.entity.EntityBlobfish;
import com.github.alexthe666.citadel.client.model.AdvancedEntityModel;
import com.github.alexthe666.citadel.client.model.AdvancedModelBox;
import com.google.common.collect.ImmutableList;
import net.minecraft.client.model.ModelRenderer;

public class ModelBlobfish extends AdvancedEntityModel<EntityBlobfish> {
    private final AdvancedModelBox root;
    private final AdvancedModelBox body;
    private final AdvancedModelBox nose;
    private final AdvancedModelBox tail;
    private final AdvancedModelBox tail_fin;
    private final AdvancedModelBox fin_left;
    private final AdvancedModelBox fin_right;

    public ModelBlobfish() {
        textureWidth = 32;
        textureHeight = 32;

        root = new AdvancedModelBox(this);
        root.setRotationPoint(0.0F, 24.0F, 0.0F);


        body = new AdvancedModelBox(this);
        body.setRotationPoint(0.0F, -2.5F, 1.0F);
        root.addChild(body);
        body.setTextureOffset(0, 0).func_228303_a_(-3.0F, -2.5F, -8.0F, 6.0F, 5.0F, 8.0F, 0.0F, false);

        nose = new AdvancedModelBox(this);
        nose.setRotationPoint(0.0F, -0.5F, -8.0F);
        body.addChild(nose);
        nose.setTextureOffset(0, 19).func_228303_a_(-2.0F, -1.0F, -1.0F, 4.0F, 2.0F, 1.0F, 0.0F, false);

        tail = new AdvancedModelBox(this);
        tail.setRotationPoint(0.0F, 0.25F, 0.0F);
        body.addChild(tail);
        tail.setTextureOffset(11, 14).func_228303_a_(-2.0F, -2.25F, 0.0F, 4.0F, 4.0F, 4.0F, 0.0F, false);

        tail_fin = new AdvancedModelBox(this);
        tail_fin.setRotationPoint(0.0F, -1.45F, 0.0F);
        tail.addChild(tail_fin);
        tail_fin.setTextureOffset(0, 14).func_228303_a_(0.0F, -2.0F, -1.0F, 0.0F, 6.0F, 10.0F, 0.0F, false);

        fin_left = new AdvancedModelBox(this);
        fin_left.setRotationPoint(3.0F, 1.0F, -4.0F);
        body.addChild(fin_left);
        setRotationAngle(fin_left, 0.0F, -0.6109F, 0.0F);
        fin_left.setTextureOffset(0, 0).func_228303_a_(0.0F, -1.5F, 0.0F, 3.0F, 3.0F, 0.0F, 0.0F, false);

        fin_right = new AdvancedModelBox(this);
        fin_right.setRotationPoint(-3.0F, 1.0F, -4.0F);
        body.addChild(fin_right);
        setRotationAngle(fin_right, 0.0F, 0.6109F, 0.0F);
        fin_right.setTextureOffset(0, 0).func_228303_a_(-3.0F, -1.5F, 0.0F, 3.0F, 3.0F, 0.0F, 0.0F, true);
        this.updateDefaultPose();
    }
    @Override
    public void render(net.minecraft.entity.Entity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
        for (net.minecraft.client.model.ModelRenderer part : this.getParts()) {
            part.render(scale);
        }
    }


        public Iterable<ModelRenderer> getParts() {
        return ImmutableList.of(root);
    }

    @Override
    public Iterable<AdvancedModelBox> getAllParts() {
        return ImmutableList.of(root, body, fin_left, fin_right, tail, tail_fin, nose);
    }

    @Override
    public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale, Entity entityIn) {
        setRotationAngles((EntityBlobfish) entityIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
    }

    public void setRotationAngles(EntityBlobfish entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.resetToDefaultPose();
        this.body.rotateAngleX = headPitch * ((float)Math.PI / 180F);
        float swimSpeed = 1.5F;
        float swimDegree = 0.85F;
        this.swing(tail, swimSpeed, swimDegree, false, 0, 0, limbSwing, limbSwingAmount);
        this.swing(tail_fin, swimSpeed, swimDegree * 0.3F, false, 0, 0, limbSwing, limbSwingAmount);
        this.swing(fin_left, swimSpeed, swimDegree, false, 3F, -0.3F, limbSwing, limbSwingAmount);
        this.swing(fin_right, swimSpeed, swimDegree, true, 3F, -0.3F, limbSwing, limbSwingAmount);

    }

    public void setRotationAngle(AdvancedModelBox AdvancedModelBox, float x, float y, float z) {
        AdvancedModelBox.rotateAngleX = x;
        AdvancedModelBox.rotateAngleY = y;
        AdvancedModelBox.rotateAngleZ = z;
    }
}