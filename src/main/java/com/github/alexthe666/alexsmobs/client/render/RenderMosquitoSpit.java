package com.github.alexthe666.alexsmobs.client.render;

import com.github.alexthe666.alexsmobs.entity.EntityMosquitoSpit;
import net.minecraft.client.model.ModelLlamaSpit;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderMosquitoSpit extends Render<EntityMosquitoSpit> {

    private static final ResourceLocation SPIT_TEXTURE = new ResourceLocation("alexsmobs:textures/entity/mosquito_spit.png");
    private final ModelLlamaSpit model = new ModelLlamaSpit();

    public RenderMosquitoSpit(RenderManager renderManagerIn) {
        super(renderManagerIn);
    }

    @Override
    public void doRender(EntityMosquitoSpit entityIn, double x, double y, double z, float entityYaw, float partialTicks) {
        GlStateManager.pushMatrix();
        GlStateManager.translate((float) x, (float) (y + 0.15D), (float) z);
        GlStateManager.rotate((entityIn.prevRotationYaw + (entityIn.rotationYaw - entityIn.prevRotationYaw) * partialTicks) - 90.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate((entityIn.prevRotationPitch + (entityIn.rotationPitch - entityIn.prevRotationPitch) * partialTicks), 0.0F, 0.0F, 1.0F);
        this.bindTexture(SPIT_TEXTURE);
        this.model.render(entityIn, 0.0F, 0.0F, partialTicks, 0.0F, 0.0F, 0.0625F);
        GlStateManager.popMatrix();
        super.doRender(entityIn, x, y, z, entityYaw, partialTicks);
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityMosquitoSpit entity) {
        return SPIT_TEXTURE;
    }
}
