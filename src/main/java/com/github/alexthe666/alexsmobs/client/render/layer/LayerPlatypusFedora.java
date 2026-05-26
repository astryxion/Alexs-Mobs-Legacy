package com.github.alexthe666.alexsmobs.client.render.layer;

import com.github.alexthe666.alexsmobs.client.render.AMRenderTypes;
import com.github.alexthe666.alexsmobs.client.render.RenderPlatypus;
import com.github.alexthe666.alexsmobs.entity.EntityPlatypus;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class LayerPlatypusFedora implements LayerRenderer<EntityPlatypus> {
    private static final ResourceLocation TEXTURE = new ResourceLocation("alexsmobs:textures/entity/platypus_fedora.png");
    private final RenderPlatypus renderer;

    public LayerPlatypusFedora(RenderPlatypus renderPlatypus) {
        this.renderer = renderPlatypus;
    }

    @Override
    public void doRenderLayer(EntityPlatypus entitylivingbaseIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
        if (entitylivingbaseIn.hasFedora()) {
            this.renderer.bindTexture(TEXTURE);
            AMRenderTypes.beginEntityCutoutNoCull();
            this.renderer.getMainModel().render(entitylivingbaseIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
            AMRenderTypes.endEntityCutoutNoCull();
        }
    }

    @Override
    public boolean shouldCombineTextures() {
        return true;
    }
}
