package com.github.alexthe666.alexsmobs.client.render;

import com.github.alexthe666.alexsmobs.client.model.ModelTiger;
import com.github.alexthe666.alexsmobs.client.render.layer.LayerTigerEyes;
import com.github.alexthe666.alexsmobs.entity.EntityTiger;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderTiger extends RenderLiving<EntityTiger> {
    private static final ResourceLocation TEXTURE = new ResourceLocation("alexsmobs:textures/entity/tiger/tiger.png");
    private static final ResourceLocation TEXTURE_ANGRY = new ResourceLocation("alexsmobs:textures/entity/tiger/tiger_angry.png");
    private static final ResourceLocation TEXTURE_SLEEPING = new ResourceLocation("alexsmobs:textures/entity/tiger/tiger_sleeping.png");
    private static final ResourceLocation TEXTURE_WHITE = new ResourceLocation("alexsmobs:textures/entity/tiger/tiger_white.png");
    private static final ResourceLocation TEXTURE_ANGRY_WHITE = new ResourceLocation("alexsmobs:textures/entity/tiger/tiger_white_angry.png");
    private static final ResourceLocation TEXTURE_SLEEPING_WHITE = new ResourceLocation("alexsmobs:textures/entity/tiger/tiger_white_sleeping.png");

    public RenderTiger(RenderManager renderManagerIn) {
        super(renderManagerIn, new ModelTiger(), 0.6F);
        this.addLayer(new LayerTigerEyes(this));
    }

    @Override
    protected void preRenderCallback(EntityTiger entity, float partialTickTime) {
        float stealthLevel = entity.prevStealthProgress + (entity.stealthProgress - entity.prevStealthProgress) * partialTickTime;
        this.shadowSize = 0.6F * (1.0F - stealthLevel * 0.1F);
        if (entity.isStealth()) {
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
            float alpha = MathHelper.clamp(1.0F - stealthLevel * 0.1F, 0.0F, 1.0F);
            GlStateManager.color(1.0F, 1.0F, 1.0F, alpha);
        }
    }

    @Override
    public void doRender(EntityTiger entity, double x, double y, double z, float entityYaw, float partialTicks) {
        super.doRender(entity, x, y, z, entityYaw, partialTicks);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.disableBlend();
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityTiger entity) {
        if (entity.isSleeping()) {
            return entity.isWhite() ? TEXTURE_SLEEPING_WHITE : TEXTURE_SLEEPING;
        }
        if (entity.getAngerTime() > 0) {
            return entity.isWhite() ? TEXTURE_ANGRY_WHITE : TEXTURE_ANGRY;
        }
        return entity.isWhite() ? TEXTURE_WHITE : TEXTURE;
    }
}
