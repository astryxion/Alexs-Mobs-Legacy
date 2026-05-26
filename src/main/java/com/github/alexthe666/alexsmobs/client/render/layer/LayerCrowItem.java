package com.github.alexthe666.alexsmobs.client.render.layer;

import com.github.alexthe666.alexsmobs.client.model.ModelCrow;
import com.github.alexthe666.alexsmobs.client.render.RenderCrow;
import com.github.alexthe666.alexsmobs.entity.EntityCrow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class LayerCrowItem implements LayerRenderer<EntityCrow> {
    private final RenderCrow renderer;

    public LayerCrowItem(RenderCrow render) {
        this.renderer = render;
    }

    @Override
    public void doRenderLayer(EntityCrow entitylivingbaseIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
        ItemStack itemstack = entitylivingbaseIn.getItemStackFromSlot(EntityEquipmentSlot.MAINHAND);
        if (itemstack.isEmpty()) {
            return;
        }
        GlStateManager.pushMatrix();
        if (entitylivingbaseIn.isChild()) {
            GlStateManager.scale(0.5F, 0.5F, 0.5F);
            GlStateManager.translate(0.0D, 1.5D, 0.0D);
        }
        GlStateManager.pushMatrix();
        translateToHand(scale);
        GlStateManager.translate(0.0F, -0.09F, -0.125F);
        GlStateManager.rotate(-2.5F, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(-90.0F, 1.0F, 0.0F, 0.0F);
        GlStateManager.scale(0.75F, 0.75F, 0.75F);
        Minecraft.getMinecraft().getRenderItem().renderItem(itemstack, ItemCameraTransforms.TransformType.GROUND);
        GlStateManager.popMatrix();
        GlStateManager.popMatrix();
    }

    protected void translateToHand(float scale) {
        ModelCrow model = (ModelCrow) this.renderer.getMainModel();
        model.root.postRender(scale);
        model.body.postRender(scale);
        model.head.postRender(scale);
    }

    @Override
    public boolean shouldCombineTextures() {
        return false;
    }
}
