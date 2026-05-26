package com.github.alexthe666.alexsmobs.client.render.layer;

import com.github.alexthe666.alexsmobs.client.model.ModelCombJelly;
import com.github.alexthe666.alexsmobs.client.render.AMRenderTypes;
import com.github.alexthe666.alexsmobs.client.render.RenderCombJelly;
import com.github.alexthe666.alexsmobs.entity.EntityCombJelly;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class LayerCombJellyRainbow implements LayerRenderer<EntityCombJelly> {
    private static final ResourceLocation TEXTURE_OVERLAY = new ResourceLocation("alexsmobs:textures/entity/comb_jelly_overlay.png");
    private static final ResourceLocation RAINBOW_GLINT = new ResourceLocation("alexsmobs:textures/entity/rainbow_jelly_overlays/glint_rainbow.png");
    private static final ModelCombJelly STRIPES_MODEL = new ModelCombJelly(0.05F);
    private final RenderCombJelly renderer;

    public LayerCombJellyRainbow(RenderCombJelly render) {
        this.renderer = render;
    }

    @Override
    public void doRenderLayer(EntityCombJelly entity, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
        STRIPES_MODEL.setRotationAngles(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);

        this.renderer.bindTexture(RAINBOW_GLINT);
        AMRenderTypes.beginCombJellyRainbowGlint();
        STRIPES_MODEL.render(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
        AMRenderTypes.endRainbowGlint();

        this.renderer.bindTexture(TEXTURE_OVERLAY);
        AMRenderTypes.beginEntityCutoutNoCull();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        STRIPES_MODEL.render(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
        AMRenderTypes.endEntityCutoutNoCull();
    }

    @Override
    public boolean shouldCombineTextures() {
        return true;
    }
}
