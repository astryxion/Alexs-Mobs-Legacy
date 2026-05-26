package com.github.alexthe666.alexsmobs.client.render.layer;

import com.github.alexthe666.alexsmobs.client.model.ModelBlueJay;
import com.github.alexthe666.alexsmobs.client.render.RenderBlueJay;
import com.github.alexthe666.alexsmobs.entity.EntityBlueJay;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class LayerBlueJayShiny implements LayerRenderer<EntityBlueJay> {
    private static final ResourceLocation TEXTURE_SHINY = new ResourceLocation("alexsmobs:textures/entity/blue_jay_shiny.png");
    private final RenderBlueJay renderer;

    public LayerBlueJayShiny(RenderBlueJay renderer) {
        this.renderer = renderer;
    }

    @Override
    public void doRenderLayer(EntityBlueJay entity, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
        if (entity.getFeedTime() > 0) {
            this.renderer.bindTexture(TEXTURE_SHINY);
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
            float alpha = (float) (1F + Math.sin(ageInTicks * 0.3F)) * 0.1F + 0.8F;
            GlStateManager.color(1.0F, 1.0F, 1.0F, alpha);
            ((ModelBlueJay) this.renderer.getMainModel()).render(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            GlStateManager.disableBlend();
        }
    }

    @Override
    public boolean shouldCombineTextures() {
        return true;
    }
}
