package com.github.alexthe666.alexsmobs.client.render.layer;

import com.github.alexthe666.alexsmobs.client.render.AMRenderTypes;
import com.github.alexthe666.alexsmobs.client.render.RenderTusklin;
import com.github.alexthe666.alexsmobs.entity.EntityTusklin;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class LayerTusklinGear implements LayerRenderer<EntityTusklin> {
    private static final ResourceLocation TEXTURE_SADDLE = new ResourceLocation("alexsmobs:textures/entity/tusklin_saddle.png");
    private static final ResourceLocation TEXTURE_SHOES = new ResourceLocation("alexsmobs:textures/entity/tusklin_hooves.png");
    private final RenderTusklin renderer;

    public LayerTusklinGear(RenderTusklin render) {
        this.renderer = render;
    }

    @Override
    public void doRenderLayer(EntityTusklin entity, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
        if (entity.isSaddled()) {
            this.renderer.bindTexture(TEXTURE_SADDLE);
            AMRenderTypes.beginEntityCutoutNoCull();
            this.renderer.getMainModel().render(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
            AMRenderTypes.endEntityCutoutNoCull();
        }
        if (!entity.getShoeStack().isEmpty()) {
            this.renderer.bindTexture(TEXTURE_SHOES);
            if (entity.getShoeStack().hasEffect()) {
                GlStateManager.enableBlend();
                GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_COLOR, GlStateManager.DestFactor.ONE, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
            }
            AMRenderTypes.beginEntityCutoutNoCull();
            this.renderer.getMainModel().render(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
            AMRenderTypes.endEntityCutoutNoCull();
            if (entity.getShoeStack().hasEffect()) {
                GlStateManager.disableBlend();
            }
        }
    }

    @Override
    public boolean shouldCombineTextures() {
        return false;
    }
}
