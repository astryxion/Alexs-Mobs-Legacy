package com.github.alexthe666.alexsmobs.client.render;

import com.github.alexthe666.alexsmobs.client.model.ModelGuster;
import com.github.alexthe666.alexsmobs.entity.EntityGust;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderGust extends Render<EntityGust> {
    private static final ResourceLocation TEXTURE = new ResourceLocation("alexsmobs:textures/entity/guster.png");
    private final ModelGuster model = new ModelGuster();

    public RenderGust(RenderManager renderManagerIn) {
        super(renderManagerIn);
    }

    @Override
    public void doRender(EntityGust entityIn, double x, double y, double z, float entityYaw, float partialTicks) {
        if (true || Minecraft.getMinecraft().gameSettings.particleSetting != 2) {
            GlStateManager.pushMatrix();
            GlStateManager.translate(x, y + 0.5D, z);
            if (!entityIn.getVertical()) {
                GlStateManager.rotate(180.0F, 1.0F, 0.0F, 0.0F);
            } else {
                GlStateManager.rotate(-180.0F, 1.0F, 0.0F, 0.0F);
            }
            GlStateManager.rotate((entityIn.prevRotationYaw + (entityIn.rotationYaw - entityIn.prevRotationYaw) * partialTicks) - 90.0F, 0.0F, 1.0F, 0.0F);
            GlStateManager.scale(0.5F, 0.5F, 0.5F);
            this.bindTexture(TEXTURE);
            AMRenderTypes.beginEntityTranslucent();
            this.model.hideEyes();
            this.model.render(entityIn, 0.0F, 0.0F, entityIn.ticksExisted + partialTicks, 0.0F, 0.0F, 0.5F);
            this.model.animateGust(entityIn, 0.0F, 0.0F, entityIn.ticksExisted + partialTicks);
            this.model.showEyes();
            AMRenderTypes.endEntityTranslucent();
            GlStateManager.popMatrix();
        }
        super.doRender(entityIn, x, y, z, entityYaw, partialTicks);
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityGust entity) {
        return TEXTURE;
    }
}
