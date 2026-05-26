package com.github.alexthe666.alexsmobs.client.render.layer;

import com.github.alexthe666.alexsmobs.client.render.RenderCrimsonMosquito;
import com.github.alexthe666.alexsmobs.entity.EntityCrimsonMosquito;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class LayerCrimsonMosquitoBlood implements LayerRenderer<EntityCrimsonMosquito> {
    private static final ResourceLocation TEXTURE = new ResourceLocation("alexsmobs:textures/entity/crimson_mosquito_blood.png");
    private static final ResourceLocation TEXTURE_SICK = new ResourceLocation("alexsmobs:textures/entity/crimson_mosquito_blood_blue.png");
    private final RenderCrimsonMosquito renderer;

    public LayerCrimsonMosquitoBlood(RenderCrimsonMosquito renderCrimsonMosquito) {
        this.renderer = renderCrimsonMosquito;
    }

    @Override
    public void doRenderLayer(EntityCrimsonMosquito entitylivingbaseIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
        if (entitylivingbaseIn.getBloodLevel() > 0) {
            this.renderer.bindTexture(entitylivingbaseIn.isSick() ? TEXTURE_SICK : TEXTURE);
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
            GlStateManager.disableLighting();
            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0F, 240.0F);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            this.renderer.getMainModel().render(entitylivingbaseIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
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
