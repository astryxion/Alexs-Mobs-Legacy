package com.github.alexthe666.alexsmobs.client.render.layer;

import com.github.alexthe666.alexsmobs.client.model.ModelGrizzlyBear;
import com.github.alexthe666.alexsmobs.client.render.RenderGrizzlyBear;
import com.github.alexthe666.alexsmobs.entity.EntityGrizzlyBear;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class LayerGrizzlyItem implements LayerRenderer<EntityGrizzlyBear> {
    private final RenderGrizzlyBear renderer;

    public LayerGrizzlyItem(RenderGrizzlyBear renderGrizzlyBear) {
        this.renderer = renderGrizzlyBear;
    }

    @Override
    public void doRenderLayer(EntityGrizzlyBear entitylivingbaseIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
        ItemStack itemstack = entitylivingbaseIn.getItemStackFromSlot(EntityEquipmentSlot.MAINHAND);
        if (itemstack.isEmpty()) {
            return;
        }
        GlStateManager.pushMatrix();
        if (entitylivingbaseIn.isChild()) {
            GlStateManager.scale(0.35F, 0.35F, 0.35F);
            GlStateManager.translate(0.0D, 2.75D, 0.125D);
            translateToHand(false, scale);
            GlStateManager.translate(0.2F, 0.7F, -0.4F);
            GlStateManager.scale(2.8F, 2.8F, 2.8F);
        } else {
            translateToHand(false, scale);
            GlStateManager.translate(0.2F, 0.7F, -0.4F);
        }
        GlStateManager.rotate(10.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(100.0F, 1.0F, 0.0F, 0.0F);
        Minecraft.getMinecraft().getRenderItem().renderItem(itemstack, ItemCameraTransforms.TransformType.GROUND);
        GlStateManager.popMatrix();
    }

    protected void translateToHand(boolean left, float scale) {
        ModelGrizzlyBear model = (ModelGrizzlyBear) this.renderer.getMainModel();
        model.root.postRender(scale);
        model.midbody.postRender(scale);
        model.body.postRender(scale);
        model.right_arm.postRender(scale);
    }

    @Override
    public boolean shouldCombineTextures() {
        return false;
    }
}
