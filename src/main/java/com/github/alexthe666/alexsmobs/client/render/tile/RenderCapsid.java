package com.github.alexthe666.alexsmobs.client.render.tile;

import com.github.alexthe666.alexsmobs.tileentity.TileEntityCapsid;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.util.Random;

/**
 * 1.12 {@link TileEntitySpecialRenderer} port of the 1.16 MatrixStack / buffer item render path.
 */
public class RenderCapsid extends TileEntitySpecialRenderer<TileEntityCapsid> {

    private final Random random = new Random();

    @Override
    public void render(TileEntityCapsid entity, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        ItemStack stack = entity.getStackInSlot(0);
        if (stack.isEmpty()) {
            return;
        }
        int i = Item.getIdFromItem(stack.getItem()) + stack.getMetadata();
        this.random.setSeed((long) i);
        float floatProgress = entity.prevFloatUpProgress + (entity.floatUpProgress - entity.prevFloatUpProgress) * partialTicks;
        float yaw = entity.prevYawSwitchProgress + (entity.yawSwitchProgress - entity.prevYawSwitchProgress) * partialTicks;
        int j = this.getModelCount(stack);

        GlStateManager.pushMatrix();
        GlStateManager.translate((float) x + 0.5F, (float) y + 0.5F + floatProgress, (float) z + 0.5F);
        GlStateManager.rotate(entity.getBlockAngle() + yaw, 0.0F, 1.0F, 0.0F);
        GlStateManager.pushMatrix();
        GlStateManager.translate(0.0F, -0.1F, 0.0F);
        if (entity.vibrating && entity.getWorld() != null) {
            float vibrate = 0.05F;
            GlStateManager.translate((entity.getWorld().rand.nextFloat() - 0.5F) * vibrate, (entity.getWorld().rand.nextFloat() - 0.5F) * vibrate, (entity.getWorld().rand.nextFloat() - 0.5F) * vibrate);
        }
        GlStateManager.scale(1.3F, 1.3F, 1.3F);

        int light = entity.getWorld() != null ? entity.getWorld().getCombinedLight(entity.getPos(), 0) : 0;
        int lm = light % 65536;
        int br = light / 65536;
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, (float) lm, (float) br);

        IBakedModel ibakedmodel = Minecraft.getMinecraft().getRenderItem().getItemModelWithOverrides(stack, entity.getWorld(), (EntityLivingBase) null);
        boolean flag = ibakedmodel.isGui3d();

        if (!flag) {
            float f7 = -0.0F * (float) (j - 1) * 0.5F;
            float f8 = -0.0F * (float) (j - 1) * 0.5F;
            float f9 = -0.09375F * (float) (j - 1) * 0.5F;
            GlStateManager.translate((double) f7, (double) f8, (double) f9);
        }

        for (int k = 0; k < j; ++k) {
            GlStateManager.pushMatrix();
            if (k > 0) {
                if (flag) {
                    float f11 = (this.random.nextFloat() * 2.0F - 1.0F) * 0.15F;
                    float f13 = (this.random.nextFloat() * 2.0F - 1.0F) * 0.15F;
                    float f10 = (this.random.nextFloat() * 2.0F - 1.0F) * 0.15F;
                    GlStateManager.translate(f11, f13, f10);
                } else {
                    float f12 = (this.random.nextFloat() * 2.0F - 1.0F) * 0.15F * 0.5F;
                    float f14 = (this.random.nextFloat() * 2.0F - 1.0F) * 0.15F * 0.5F;
                    GlStateManager.translate(f12, f14, 0.0D);
                }
            }

            Minecraft.getMinecraft().getRenderItem().renderItem(stack, ItemCameraTransforms.TransformType.GROUND);

            GlStateManager.popMatrix();
            if (!flag) {
                GlStateManager.translate(0.0, 0.0, 0.09375F);
            }
        }

        GlStateManager.popMatrix();
        GlStateManager.popMatrix();
    }

    protected int getModelCount(ItemStack stack) {
        int i = 1;
        if (stack.getCount() > 48) {
            i = 5;
        } else if (stack.getCount() > 32) {
            i = 4;
        } else if (stack.getCount() > 16) {
            i = 3;
        } else if (stack.getCount() > 1) {
            i = 2;
        }
        return i;
    }
}
