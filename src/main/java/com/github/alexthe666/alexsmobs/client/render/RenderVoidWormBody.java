package com.github.alexthe666.alexsmobs.client.render;

import com.github.alexthe666.alexsmobs.client.model.ModelVoidWormBody;
import com.github.alexthe666.alexsmobs.client.model.ModelVoidWormTail;
import com.github.alexthe666.alexsmobs.entity.EntityVoidWormPart;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderVoidWormBody extends RenderLivingBase<EntityVoidWormPart> {

    private static final ResourceLocation TEXTURE_BODY = new ResourceLocation("alexsmobs:textures/entity/void_worm_body.png");
    private static final ResourceLocation TEXTURE_BODY_HURT = new ResourceLocation("alexsmobs:textures/entity/void_worm_body_hurt.png");
    private static final ResourceLocation TEXTURE_BODY_GLOW = new ResourceLocation("alexsmobs:textures/entity/void_worm_body_glow.png");
    private static final ResourceLocation TEXTURE_TAIL = new ResourceLocation("alexsmobs:textures/entity/void_worm_tail.png");
    private static final ResourceLocation TEXTURE_TAIL_HURT = new ResourceLocation("alexsmobs:textures/entity/void_worm_tail_hurt.png");
    private static final ResourceLocation TEXTURE_TAIL_GLOW = new ResourceLocation("alexsmobs:textures/entity/void_worm_tail_glow.png");

    private final ModelVoidWormBody bodyModel = new ModelVoidWormBody();
    private final ModelVoidWormTail tailModel = new ModelVoidWormTail();

    public RenderVoidWormBody(RenderManager renderManagerIn) {
        super(renderManagerIn, new ModelVoidWormBody(), 1.0F);
        this.addLayer(new LayerGlow(this));
    }

    @Override
    public boolean shouldRender(EntityVoidWormPart worm, ICamera camera, double camX, double camY, double camZ) {
        return worm.getPortalTicks() <= 0 && super.shouldRender(worm, camera, camX, camY, camZ);
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityVoidWormPart entity) {
        if (entity.isHurt()) {
            return entity.isTail() ? TEXTURE_TAIL_HURT : TEXTURE_BODY_HURT;
        }
        return entity.isTail() ? TEXTURE_TAIL : TEXTURE_BODY;
    }

    @Override
    protected void applyRotations(EntityVoidWormPart entityLiving, float ageInTicks, float rotationYaw, float partialTicks) {
        GlStateManager.rotate(180.0F - entityLiving.getWormYaw(partialTicks), 0.0F, 1.0F, 0.0F);
        if (entityLiving.deathTime > 0) {
            float f = ((float) entityLiving.deathTime + partialTicks - 1.0F) / 20.0F * 1.6F;
            f = MathHelper.sqrt(f);
            if (f > 1.0F) {
                f = 1.0F;
            }
            GlStateManager.rotate(f * this.getDeathMaxRotation(entityLiving), 0.0F, 0.0F, 1.0F);
        }
    }

    @Override
    protected void preRenderCallback(EntityVoidWormPart entitylivingbaseIn, float partialTickTime) {
        this.mainModel = entitylivingbaseIn.isTail() ? this.tailModel : this.bodyModel;
        GlStateManager.scale(entitylivingbaseIn.getWormScale(), entitylivingbaseIn.getWormScale(), entitylivingbaseIn.getWormScale());
    }

    @Override
    protected boolean canRenderName(EntityVoidWormPart entity) {
        return super.canRenderName(entity) && (entity.getAlwaysRenderNameTagForRender() || entity.hasCustomName() && entity == this.renderManager.pointedEntity);
    }

    @SideOnly(Side.CLIENT)
    private static class LayerGlow implements LayerRenderer<EntityVoidWormPart> {
        private final RenderVoidWormBody renderer;

        LayerGlow(RenderVoidWormBody renderer) {
            this.renderer = renderer;
        }

        @Override
        public void doRenderLayer(EntityVoidWormPart worm, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
            if (worm.isHurt()) {
                return;
            }
            float alpha = (float) MathHelper.clamp((worm.getHealth() - (float) worm.getHealthThreshold()) / (worm.getMaxHealth() - (float) worm.getHealthThreshold()), 0.0F, 1.0F);
            this.renderer.bindTexture(worm.isTail() ? TEXTURE_TAIL_GLOW : TEXTURE_BODY_GLOW);
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
            GlStateManager.disableLighting();
            GlStateManager.color(1.0F, 1.0F, 1.0F, alpha);
            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0F, 240.0F);
            ModelBase model = this.renderer.getMainModel();
            model.render(worm, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
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
