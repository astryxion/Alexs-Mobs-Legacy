package com.github.alexthe666.alexsmobs.client.render;

import com.github.alexthe666.alexsmobs.client.model.ModelFart;
import com.github.alexthe666.alexsmobs.entity.EntityFart;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderFart extends Render<EntityFart> {
    private static final ResourceLocation FART_TEXTURE = new ResourceLocation("alexsmobs:textures/entity/fart.png");
    private static final ModelFart MODEL = new ModelFart();

    public RenderFart(RenderManager renderManagerIn) {
        super(renderManagerIn);
    }

    @Override
    public void doRender(EntityFart entityIn, double x, double y, double z, float entityYaw, float partialTicks) {
        float f = Math.min(entityIn.ticksExisted + partialTicks, 30F) / 30F;
        float alpha = 1F - f;
        GlStateManager.pushMatrix();
        GlStateManager.translate((float) x, (float) (y + 0.15D), (float) z);
        GlStateManager.rotate((entityIn.prevRotationYaw + (entityIn.rotationYaw - entityIn.prevRotationYaw) * partialTicks) - 180.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate((entityIn.prevRotationPitch + (entityIn.rotationPitch - entityIn.prevRotationPitch) * partialTicks), 1.0F, 0.0F, 0.0F);
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        GlStateManager.color(1.0F, 1.0F, 1.0F, alpha);
        this.bindEntityTexture(entityIn);
        MODEL.setRotationAngles(0, 0, partialTicks, 0, 0, 0.0625F, entityIn);
        MODEL.render(entityIn, 0, 0, partialTicks, 0, 0, 0.0625F);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
        super.doRender(entityIn, x, y, z, entityYaw, partialTicks);
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityFart entity) {
        return FART_TEXTURE;
    }
}
