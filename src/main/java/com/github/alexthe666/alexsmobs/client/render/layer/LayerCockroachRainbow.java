package com.github.alexthe666.alexsmobs.client.render.layer;

import com.github.alexthe666.alexsmobs.client.model.ModelCockroach;
import com.github.alexthe666.alexsmobs.client.render.AMRenderTypes;
import com.github.alexthe666.alexsmobs.client.render.RenderCockroach;
import com.github.alexthe666.alexsmobs.entity.EntityCockroach;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class LayerCockroachRainbow implements LayerRenderer<EntityCockroach> {
    private static final ResourceLocation RAINBOW_GLINT = new ResourceLocation("alexsmobs:textures/entity/rainbow_glint.png");
    private final RenderCockroach renderer;

    public LayerCockroachRainbow(RenderCockroach render) {
        this.renderer = render;
    }

    @Override
    public void doRenderLayer(EntityCockroach entitylivingbaseIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
        if (entitylivingbaseIn.isRainbow()) {
            this.renderer.bindTexture(RAINBOW_GLINT);
            AMRenderTypes.beginRainbowGlint();
            ((ModelCockroach) this.renderer.getMainModel()).render(entitylivingbaseIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
            AMRenderTypes.endRainbowGlint();
        }
    }

    @Override
    public boolean shouldCombineTextures() {
        return true;
    }
}
