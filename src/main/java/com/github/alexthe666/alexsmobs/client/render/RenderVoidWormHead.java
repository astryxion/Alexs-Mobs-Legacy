package com.github.alexthe666.alexsmobs.client.render;

import com.github.alexthe666.alexsmobs.client.model.ModelVoidWorm;
import com.github.alexthe666.alexsmobs.entity.EntityVoidWorm;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderVoidWormHead extends RenderLiving<EntityVoidWorm> {

    private static final ResourceLocation TEXTURE = new ResourceLocation("alexsmobs:textures/entity/void_worm_head.png");
    private static final ResourceLocation TEXTURE_GLOW = new ResourceLocation("alexsmobs:textures/entity/void_worm_head_glow.png");

    public RenderVoidWormHead(RenderManager renderManagerIn) {
        super(renderManagerIn, new ModelVoidWorm(), 1.0F);
        this.addLayer(new LayerHeadGlow(this));
    }

    @Override
    public boolean shouldRender(EntityVoidWorm worm, ICamera camera, double camX, double camY, double camZ) {
        return worm.getPortalTicks() <= 0 && super.shouldRender(worm, camera, camX, camY, camZ);
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityVoidWorm entity) {
        return TEXTURE;
    }

    @SideOnly(Side.CLIENT)
    private static class LayerHeadGlow implements LayerRenderer<EntityVoidWorm> {
        private final RenderVoidWormHead renderer;

        LayerHeadGlow(RenderVoidWormHead renderer) {
            this.renderer = renderer;
        }

        @Override
        public void doRenderLayer(EntityVoidWorm entity, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
            this.renderer.bindTexture(TEXTURE_GLOW);
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
            GlStateManager.disableLighting();
            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0F, 240.0F);
            this.renderer.getMainModel().render(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
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
