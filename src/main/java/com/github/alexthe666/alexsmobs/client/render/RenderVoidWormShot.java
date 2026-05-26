package com.github.alexthe666.alexsmobs.client.render;

import com.github.alexthe666.alexsmobs.client.model.ModelVoidWormShot;
import com.github.alexthe666.alexsmobs.entity.EntityVoidWormShot;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderVoidWormShot extends Render<EntityVoidWormShot> {

    private static final ResourceLocation TEXTURE = new ResourceLocation("alexsmobs:textures/entity/void_worm_shot.png");
    private static final ResourceLocation TEXTURE_PORTAL = new ResourceLocation("alexsmobs:textures/entity/void_worm_shot_portal.png");
    private static final ModelVoidWormShot MODEL = new ModelVoidWormShot();

    public RenderVoidWormShot(RenderManager renderManager) {
        super(renderManager);
    }

    @Override
    public void doRender(EntityVoidWormShot entityIn, double x, double y, double z, float entityYaw, float partialTicks) {
        GlStateManager.pushMatrix();
        GlStateManager.translate((float) x, (float) (y - 1.5D), (float) z);
        GlStateManager.rotate(180.0F, 1.0F, 0.0F, 0.0F);
        GlStateManager.rotate((entityIn.prevRotationYaw + (entityIn.rotationYaw - entityIn.prevRotationYaw) * partialTicks), 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate((entityIn.prevRotationPitch + (entityIn.rotationPitch - entityIn.prevRotationPitch) * partialTicks), 1.0F, 0.0F, 0.0F);
        MODEL.animate(entityIn, entityIn.ticksExisted + partialTicks);
        this.bindTexture(entityIn.isPortalType() ? TEXTURE_PORTAL : TEXTURE);
        GlStateManager.disableLighting();
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0F, 240.0F);
        MODEL.render(entityIn, 0.0F, 0.0F, partialTicks, 0.0F, 0.0F, 0.0625F);
        GlStateManager.enableLighting();
        GlStateManager.popMatrix();
        super.doRender(entityIn, x, y, z, entityYaw, partialTicks);
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityVoidWormShot entity) {
        return entity.isPortalType() ? TEXTURE_PORTAL : TEXTURE;
    }
}
