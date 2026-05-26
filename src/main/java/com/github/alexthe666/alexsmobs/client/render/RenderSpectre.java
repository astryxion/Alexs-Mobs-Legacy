package com.github.alexthe666.alexsmobs.client.render;

import com.github.alexthe666.alexsmobs.client.model.ModelSpectre;
import com.github.alexthe666.alexsmobs.entity.EntitySpectre;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderSpectre extends RenderLiving<EntitySpectre> {
    private static final ResourceLocation TEXTURE_BONE = new ResourceLocation("alexsmobs:textures/entity/spectre_bone.png");
    private static final ResourceLocation TEXTURE = new ResourceLocation("alexsmobs:textures/entity/spectre.png");
    private static final ResourceLocation TEXTURE_EYES = new ResourceLocation("alexsmobs:textures/entity/spectre_glow.png");
    private static final ResourceLocation TEXTURE_LEAD = new ResourceLocation("alexsmobs:textures/entity/spectre_lead.png");

    public RenderSpectre(RenderManager renderManagerIn) {
        super(renderManagerIn, new ModelSpectre(), 0.5F);
        this.addLayer(new SpectreEyesLayer(this));
        this.addLayer(new SpectreMembraneLayer(this));
    }

    @Override
    protected void preRenderCallback(EntitySpectre entitylivingbaseIn, float partialTickTime) {
        GlStateManager.scale(1.3F, 1.3F, 1.3F);
    }

    @Override
    public void doRender(EntitySpectre entity, double x, double y, double z, float entityYaw, float partialTicks) {
        int brightness = 15728880;
        int j = brightness % 65536;
        int k = brightness / 65536;
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, (float) j, (float) k);
        super.doRender(entity, x, y, z, entityYaw, partialTicks);
    }

    @Override
    protected ResourceLocation getEntityTexture(EntitySpectre entity) {
        return TEXTURE_BONE;
    }

    public float getAlphaForRender(EntitySpectre entityIn, float partialTicks) {
        return ((float) Math.sin((entityIn.ticksExisted + partialTicks) * 0.1F) + 1.5F) * 0.1F + 0.5F;
    }

    @SideOnly(Side.CLIENT)
    class SpectreEyesLayer implements LayerRenderer<EntitySpectre> {
        private final RenderSpectre renderer;

        SpectreEyesLayer(RenderSpectre render) {
            this.renderer = render;
        }

        @Override
        public void doRenderLayer(EntitySpectre entitylivingbaseIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
            this.renderer.bindTexture(TEXTURE_EYES);
            AMRenderTypes.beginEyesNoCull();
            this.renderer.getMainModel().render(entitylivingbaseIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
            AMRenderTypes.endEyesNoCull();
        }

        @Override
        public boolean shouldCombineTextures() {
            return true;
        }
    }

    @SideOnly(Side.CLIENT)
    class SpectreMembraneLayer implements LayerRenderer<EntitySpectre> {
        private final RenderSpectre renderer;

        SpectreMembraneLayer(RenderSpectre render) {
            this.renderer = render;
        }

        @Override
        public void doRenderLayer(EntitySpectre entitylivingbaseIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
            float alpha = getAlphaForRender(entitylivingbaseIn, partialTicks);
            this.renderer.bindTexture(TEXTURE);
            AMRenderTypes.beginGhost();
            GlStateManager.color(1.0F, 1.0F, 1.0F, alpha);
            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0F, 240.0F);
            this.renderer.getMainModel().render(entitylivingbaseIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
            if (entitylivingbaseIn.getLeashed()) {
                this.renderer.bindTexture(TEXTURE_LEAD);
                AMRenderTypes.beginEntityCutoutNoCull();
                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                this.renderer.getMainModel().render(entitylivingbaseIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
                AMRenderTypes.endEntityCutoutNoCull();
            }
            AMRenderTypes.endGhost();
        }

        @Override
        public boolean shouldCombineTextures() {
            return true;
        }
    }
}
