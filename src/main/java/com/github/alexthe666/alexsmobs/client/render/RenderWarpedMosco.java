package com.github.alexthe666.alexsmobs.client.render;

import com.github.alexthe666.alexsmobs.client.model.ModelWarpedMosco;
import com.github.alexthe666.alexsmobs.entity.EntityWarpedMosco;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderWarpedMosco extends RenderLiving<EntityWarpedMosco> {
    private static final ResourceLocation TEXTURE = new ResourceLocation("alexsmobs:textures/entity/warped_mosco.png");
    private static final ResourceLocation TEXTURE_EYES = new ResourceLocation("alexsmobs:textures/entity/warped_mosco_glow.png");

    public RenderWarpedMosco(RenderManager renderManagerIn) {
        super(renderManagerIn, new ModelWarpedMosco(), 1F);
        this.addLayer(new WarpedMoscoGlowLayer(this));
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityWarpedMosco entity) {
        return TEXTURE;
    }

    @SideOnly(Side.CLIENT)
    private static class WarpedMoscoGlowLayer implements LayerRenderer<EntityWarpedMosco> {
        private final RenderWarpedMosco renderer;

        WarpedMoscoGlowLayer(RenderWarpedMosco renderer) {
            this.renderer = renderer;
        }

        @Override
        public void doRenderLayer(EntityWarpedMosco entitylivingbaseIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
            this.renderer.bindTexture(TEXTURE_EYES);
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
            GlStateManager.disableLighting();
            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0F, 240.0F);
            float alpha = 0.5F + (MathHelper.cos(ageInTicks * 0.2F) + 1F) * 0.2F;
            GlStateManager.color(0.5F, 1.0F, 1.0F, alpha);
            this.renderer.getMainModel().render(entitylivingbaseIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
            GlStateManager.enableLighting();
            GlStateManager.disableBlend();
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        }

        @Override
        public boolean shouldCombineTextures() {
            return true;
        }
    }
}
