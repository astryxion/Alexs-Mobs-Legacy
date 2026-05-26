package com.github.alexthe666.alexsmobs.client.render.layer;

import com.github.alexthe666.alexsmobs.client.model.ModelGuster;
import com.github.alexthe666.alexsmobs.client.render.AMRenderTypes;
import com.github.alexthe666.alexsmobs.client.render.RenderGuster;
import com.github.alexthe666.alexsmobs.entity.EntityGuster;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class LayerGusterEyes implements LayerRenderer<EntityGuster> {
    private static final ResourceLocation TEXTURE_EYES = new ResourceLocation("alexsmobs:textures/entity/guster_eye.png");
    private static final ResourceLocation TEXTURE_SOUL_EYES = new ResourceLocation("alexsmobs:textures/entity/guster_eye_soul.png");
    private final RenderGuster renderer;

    public LayerGusterEyes(RenderGuster renderer) {
        this.renderer = renderer;
    }

    @Override
    public void doRenderLayer(EntityGuster entitylivingbaseIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
        if (!entitylivingbaseIn.isGooglyEyes()) {
            ResourceLocation eyes = entitylivingbaseIn.getVariant() == 2 ? TEXTURE_SOUL_EYES : TEXTURE_EYES;
            this.renderer.bindTexture(eyes);
            AMRenderTypes.beginEyesNoCull();
            ((ModelGuster) this.renderer.getMainModel()).render(entitylivingbaseIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
            AMRenderTypes.endEyesNoCull();
        }
    }

    @Override
    public boolean shouldCombineTextures() {
        return true;
    }
}
