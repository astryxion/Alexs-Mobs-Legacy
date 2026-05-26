package com.github.alexthe666.alexsmobs.client.render;

import com.github.alexthe666.alexsmobs.client.model.ModelAlligatorSnappingTurtle;
import com.github.alexthe666.alexsmobs.entity.EntityAlligatorSnappingTurtle;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.util.ResourceLocation;

public class RenderAlligatorSnappingTurtle extends RenderLiving<EntityAlligatorSnappingTurtle> {
    private static final ResourceLocation TEXTURE_MOSS = new ResourceLocation("alexsmobs:textures/entity/alligator_snapping_turtle_moss.png");
    private static final ResourceLocation TEXTURE = new ResourceLocation("alexsmobs:textures/entity/alligator_snapping_turtle.png");

    public RenderAlligatorSnappingTurtle(RenderManager renderManagerIn) {
        super(renderManagerIn, new ModelAlligatorSnappingTurtle(), 0.75F);
        this.addLayer(new AlligatorSnappingTurtleMossLayer(this));
    }

    @Override
    protected void preRenderCallback(EntityAlligatorSnappingTurtle entitylivingbaseIn, float partialTickTime) {
        float d = entitylivingbaseIn.getTurtleScale() < 0.01F ? 1F : entitylivingbaseIn.getTurtleScale();
        GlStateManager.scale(d, d, d);
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityAlligatorSnappingTurtle entity) {
        return TEXTURE;
    }

    private class AlligatorSnappingTurtleMossLayer implements LayerRenderer<EntityAlligatorSnappingTurtle> {

        private final RenderAlligatorSnappingTurtle renderer;

        AlligatorSnappingTurtleMossLayer(RenderAlligatorSnappingTurtle parent) {
            this.renderer = parent;
        }

        @Override
        public void doRenderLayer(
                EntityAlligatorSnappingTurtle entitylivingbaseIn,
                float limbSwing,
                float limbSwingAmount,
                float partialTicks,
                float ageInTicks,
                float netHeadYaw,
                float headPitch,
                float scale) {
            if (entitylivingbaseIn.getMoss() <= 0) {
                return;
            }
            this.renderer.bindTexture(TEXTURE_MOSS);
            int moss = entitylivingbaseIn.getMoss();
            float clamped = moss < 0 ? 0 : (moss > 10 ? 10 : moss);
            float mossAlpha = 0.15F * clamped;
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(
                    GlStateManager.SourceFactor.SRC_ALPHA,
                    GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                    GlStateManager.SourceFactor.ONE,
                    GlStateManager.DestFactor.ZERO);
            GlStateManager.color(1.0F, 1.0F, 1.0F, Math.min(1.0F, mossAlpha));
            ((ModelAlligatorSnappingTurtle) this.renderer.getMainModel()).render(entitylivingbaseIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
            GlStateManager.disableBlend();
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            this.renderer.bindTexture(this.renderer.getEntityTexture(entitylivingbaseIn));
        }

        @Override
        public boolean shouldCombineTextures() {
            return true;
        }
    }
}
