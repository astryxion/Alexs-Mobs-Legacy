package com.github.alexthe666.alexsmobs.client.render;

import com.github.alexthe666.alexsmobs.entity.EntityGuster;
import com.github.alexthe666.alexsmobs.entity.EntitySandShot;
import net.minecraft.client.model.ModelLlamaSpit;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderSandShot extends Render<EntitySandShot> {

    private static final ResourceLocation SAND_SHOT = new ResourceLocation("alexsmobs:textures/entity/sand_shot.png");
    private final ModelLlamaSpit model = new ModelLlamaSpit();

    public RenderSandShot(RenderManager renderManagerIn) {
        super(renderManagerIn);
    }

    @Override
    public void doRender(EntitySandShot entityIn, double x, double y, double z, float entityYaw, float partialTicks) {
        GlStateManager.pushMatrix();
        GlStateManager.translate((float) x, (float) (y + 0.15D), (float) z);
        GlStateManager.rotate((entityIn.prevRotationYaw + (entityIn.rotationYaw - entityIn.prevRotationYaw) * partialTicks) - 90.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate((entityIn.prevRotationPitch + (entityIn.rotationPitch - entityIn.prevRotationPitch) * partialTicks), 0.0F, 0.0F, 1.0F);
        GlStateManager.scale(1.2F, 1.2F, 1.2F);
        int i = EntityGuster.getColorForVariant(entityIn.getVariant());
        float r = (float) (i >> 16 & 255) / 255.0F;
        float g = (float) (i >> 8 & 255) / 255.0F;
        float b = (float) (i & 255) / 255.0F;
        GlStateManager.color(r, g, b, 1.0F);
        this.bindTexture(SAND_SHOT);
        this.model.render(entityIn, 0.0F, 0.0F, partialTicks, 0.0F, 0.0F, 0.0625F);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.popMatrix();
        super.doRender(entityIn, x, y, z, entityYaw, partialTicks);
    }

    @Override
    protected ResourceLocation getEntityTexture(EntitySandShot entity) {
        return SAND_SHOT;
    }
}
