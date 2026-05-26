package com.github.alexthe666.alexsmobs.client.render;

import com.github.alexthe666.alexsmobs.client.model.ModelStraddler;
import com.github.alexthe666.alexsmobs.client.model.ModelStradpole;
import com.github.alexthe666.alexsmobs.entity.EntityStraddler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderStraddler extends RenderLiving<EntityStraddler> {

    private static final ResourceLocation TEXTURE = new ResourceLocation("alexsmobs:textures/entity/straddler.png");
    private static final ModelStradpole STRADPOLE_MODEL = new ModelStradpole();

    public RenderStraddler(RenderManager renderManagerIn) {
        super(renderManagerIn, new ModelStraddler(), 0.6F);
        this.addLayer(new LayerStradpoleCharge(this));
    }

    @Override
    protected void preRenderCallback(EntityStraddler entitylivingbaseIn, float partialTickTime) {
        GlStateManager.scale(1.2F, 1.2F, 1.2F);
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityStraddler entity) {
        return TEXTURE;
    }

    @SideOnly(Side.CLIENT)
    static class LayerStradpoleCharge implements LayerRenderer<EntityStraddler> {

        private final RenderStraddler render;

        LayerStradpoleCharge(RenderStraddler render) {
            this.render = render;
        }

        @Override
        public void doRenderLayer(EntityStraddler straddler, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
            int t = straddler.getAnimationTick();
            if (straddler.getAnimation() == EntityStraddler.ANIMATION_LAUNCH && t < 20 && t > 6) {
                GlStateManager.pushMatrix();
                ModelStraddler model = (ModelStraddler) this.render.getMainModel();
                model.root.postRender(scale);
                model.body.postRender(scale);
                float back = t <= 15 ? (t - 6) * 0.05F : 0.25F;
                GlStateManager.translate(0.0F, -2.5F + back * 0.5F, 0.35F + back);
                Minecraft.getMinecraft().renderEngine.bindTexture(RenderStradpole.TEXTURE);
                GlStateManager.enableBlend();
                GlStateManager.color(1.0F, 1.0F, 1.0F, 0.92F);
                STRADPOLE_MODEL.renderStraddlerLayer(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                GlStateManager.disableBlend();
                GlStateManager.popMatrix();
            }
        }

        @Override
        public boolean shouldCombineTextures() {
            return false;
        }
    }
}
