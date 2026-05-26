package com.github.alexthe666.alexsmobs.client.render.layer;

import com.github.alexthe666.alexsmobs.client.model.ModelLaviathan;
import com.github.alexthe666.alexsmobs.client.render.RenderLaviathan;
import com.github.alexthe666.alexsmobs.entity.EntityLaviathan;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class LayerLaviathanOverlays implements LayerRenderer<EntityLaviathan> {
    private static final ResourceLocation TEXTURE_GLOW = new ResourceLocation("alexsmobs:textures/entity/laviathan_glow.png");
    private static final ResourceLocation TEXTURE_GEAR = new ResourceLocation("alexsmobs:textures/entity/laviathan_gear.png");
    private static final ResourceLocation TEXTURE_HELMET = new ResourceLocation("alexsmobs:textures/entity/laviathan_helmet.png");
    private final RenderLaviathan renderer;

    public LayerLaviathanOverlays(RenderLaviathan render) {
        this.renderer = render;
    }

    @Override
    public void doRenderLayer(EntityLaviathan laviathan, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
        if (!laviathan.isObsidian()) {
            this.renderer.bindTexture(TEXTURE_GLOW);
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
            GlStateManager.disableLighting();
            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0F, 240.0F);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            ((ModelLaviathan) this.renderer.getMainModel()).render(laviathan, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
            GlStateManager.enableLighting();
            GlStateManager.disableBlend();
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        }
        if (laviathan.hasBodyGear()) {
            this.renderer.bindTexture(TEXTURE_GEAR);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            ((ModelLaviathan) this.renderer.getMainModel()).render(laviathan, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
        }
        if (laviathan.hasHeadGear()) {
            this.renderer.bindTexture(TEXTURE_HELMET);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            ((ModelLaviathan) this.renderer.getMainModel()).render(laviathan, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
        }
    }

    @Override
    public boolean shouldCombineTextures() {
        return true;
    }
}
