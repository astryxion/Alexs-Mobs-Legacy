package com.github.alexthe666.alexsmobs.client.render.layer;

import com.github.alexthe666.alexsmobs.client.model.ModelWarpedToad;
import com.github.alexthe666.alexsmobs.client.render.RenderWarpedToad;
import com.github.alexthe666.alexsmobs.entity.EntityWarpedToad;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class LayerWarpedToadGlow implements LayerRenderer<EntityWarpedToad> {
    private static final ResourceLocation TEXTURE = new ResourceLocation("alexsmobs:textures/entity/warped_toad_glow.png");
    private static final ResourceLocation TEXTURE_BLINKING = new ResourceLocation("alexsmobs:textures/entity/warped_toad_glow_blink.png");
    private final RenderWarpedToad renderer;

    public LayerWarpedToadGlow(RenderWarpedToad renderWarpedToad) {
        this.renderer = renderWarpedToad;
    }

    @Override
    public void doRenderLayer(EntityWarpedToad entitylivingbaseIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
        if (!entitylivingbaseIn.isBased()) {
            this.renderer.bindTexture(entitylivingbaseIn.isBlinking() ? TEXTURE_BLINKING : TEXTURE);
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
            GlStateManager.disableLighting();
            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0F, 240.0F);
            float alpha = 0.75F + (MathHelper.cos(ageInTicks * 0.2F) + 1F) * 0.125F;
            GlStateManager.color(1.0F, 1.0F, 1.0F, alpha);
            ((ModelWarpedToad) this.renderer.getMainModel()).render(entitylivingbaseIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
            GlStateManager.enableLighting();
            GlStateManager.disableBlend();
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        }
    }

    @Override
    public boolean shouldCombineTextures() {
        return true;
    }
}
