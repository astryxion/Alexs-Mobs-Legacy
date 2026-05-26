package com.github.alexthe666.alexsmobs.client.model;

import com.github.alexthe666.citadel.client.model.AdvancedEntityModel;
import com.github.alexthe666.citadel.client.model.AdvancedModelBox;
import com.google.common.collect.ImmutableList;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

public class ModelSquidGrapple extends AdvancedEntityModel<Entity> {
    private final AdvancedModelBox root;
    private final AdvancedModelBox tentacle;

    public ModelSquidGrapple() {
        textureWidth = 128;
        textureHeight = 128;

        root = new AdvancedModelBox(this, "root");
        root.setRotationPoint(0.0F, 24.0F, 0.0F);
        tentacle = new AdvancedModelBox(this, "tentacle");
        tentacle.setRotationPoint(0.0F, -1.7F, 0.0F);
        root.addChild(tentacle);
        setRotationAngle(tentacle, -1.5708F, 0.0F, 0.0F);
        tentacle.setTextureOffset(54, 60).func_228303_a_(-3.0F, -7.0F, -1.3F, 6.0F, 14.0F, 3.0F, 0.0F, false);
        this.updateDefaultPose();
    }

    public Iterable<ModelRenderer> getParts() {
        return ImmutableList.of(root);
    }

    @Override
    public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale, Entity entityIn) {
        this.resetToDefaultPose();
    }

    @Override
    public Iterable<AdvancedModelBox> getAllParts() {
        return ImmutableList.of(root, tentacle);
    }

    @Override
    public void render(Entity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
        for (ModelRenderer part : this.getParts()) {
            part.render(scale);
        }
    }

    public void setRotationAngle(AdvancedModelBox modelRenderer, float x, float y, float z) {
        modelRenderer.rotateAngleX = x;
        modelRenderer.rotateAngleY = y;
        modelRenderer.rotateAngleZ = z;
    }
}
