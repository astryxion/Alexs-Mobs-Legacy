package com.github.alexthe666.alexsmobs.client.render;

import com.github.alexthe666.alexsmobs.client.model.ModelAncientDart;
import com.github.alexthe666.alexsmobs.entity.EntityTossedItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderTossedItem extends Render<EntityTossedItem> {
    public static final ResourceLocation DART_TEXTURE = new ResourceLocation("alexsmobs:textures/entity/ancient_dart.png");
    public static final ModelAncientDart DART_MODEL = new ModelAncientDart();

    public RenderTossedItem(RenderManager renderManager) {
        super(renderManager);
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityTossedItem entity) {
        return DART_TEXTURE;
    }

    @Override
    public void doRender(EntityTossedItem entityIn, double x, double y, double z, float entityYaw, float partialTicks) {
        GlStateManager.pushMatrix();
        GlStateManager.translate((float) x, (float) y, (float) z);
        if (entityIn.isDart()) {
            GlStateManager.translate(0.0D, -0.15D, 0.0D);
            GlStateManager.rotate((entityIn.prevRotationYaw + (entityIn.rotationYaw - entityIn.prevRotationYaw) * partialTicks) - 180.0F, 0.0F, 1.0F, 0.0F);
            GlStateManager.pushMatrix();
            GlStateManager.rotate((entityIn.prevRotationPitch + (entityIn.rotationPitch - entityIn.prevRotationPitch) * partialTicks), 1.0F, 0.0F, 0.0F);
            GlStateManager.translate(0.0F, 0.5F, 0.0F);
            GlStateManager.scale(1.0F, 1.0F, 1.0F);
            this.bindTexture(DART_TEXTURE);
            DART_MODEL.render(entityIn, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F);
            GlStateManager.popMatrix();
        } else {
            GlStateManager.rotate((entityIn.prevRotationYaw + (entityIn.rotationYaw - entityIn.prevRotationYaw) * partialTicks) - 90.0F, 0.0F, 1.0F, 0.0F);
            GlStateManager.rotate((entityIn.prevRotationPitch + (entityIn.rotationPitch - entityIn.prevRotationPitch) * partialTicks), 0.0F, 0.0F, 1.0F);
            GlStateManager.translate(0.0F, 0.5F, 0.0F);
            GlStateManager.scale(1.0F, 1.0F, 1.0F);
            GlStateManager.rotate(0.0F, 0.0F, 1.0F, 0.0F);
            GlStateManager.rotate((entityIn.ticksExisted + partialTicks) * 30.0F, 0.0F, 0.0F, -1.0F);
            GlStateManager.translate(0.0F, -0.15F, 0.0F);
            Minecraft.getMinecraft().getRenderItem().renderItem(entityIn.getItemStack(), ItemCameraTransforms.TransformType.GROUND);
        }
        GlStateManager.popMatrix();
        super.doRender(entityIn, x, y, z, entityYaw, partialTicks);
    }
}
