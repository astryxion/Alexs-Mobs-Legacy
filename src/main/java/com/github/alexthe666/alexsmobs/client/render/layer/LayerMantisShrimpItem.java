package com.github.alexthe666.alexsmobs.client.render.layer;

import com.github.alexthe666.alexsmobs.client.model.ModelMantisShrimp;
import com.github.alexthe666.alexsmobs.client.render.RenderMantisShrimp;
import com.github.alexthe666.alexsmobs.entity.EntityMantisShrimp;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class LayerMantisShrimpItem implements LayerRenderer<EntityMantisShrimp> {
    private final RenderMantisShrimp renderer;

    public LayerMantisShrimpItem(RenderMantisShrimp render) {
        this.renderer = render;
    }

    @Override
    public void doRenderLayer(EntityMantisShrimp entitylivingbaseIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
        ItemStack itemstack = entitylivingbaseIn.getItemStackFromSlot(EntityEquipmentSlot.MAINHAND);
        if (itemstack.isEmpty()) {
            return;
        }
        boolean left = entitylivingbaseIn.isLeftHanded();
        GlStateManager.pushMatrix();
        if (entitylivingbaseIn.isChild()) {
            GlStateManager.scale(0.5F, 0.5F, 0.5F);
            GlStateManager.translate(0.0D, 1.5D, 0.0D);
        }
        GlStateManager.pushMatrix();
        translateToHand(scale, left);
        GlStateManager.translate(left ? 0.075F : -0.075F, 0.45F, -0.125F);
        IBakedModel baked = Minecraft.getMinecraft().getRenderItem().getItemModelWithOverrides(itemstack, entitylivingbaseIn.world, entitylivingbaseIn);
        if (!baked.isGui3d()) {
            GlStateManager.translate(0.0F, 0.0F, 0.05F);
            GlStateManager.rotate(left ? -40.0F : 40.0F, 0.0F, 0.0F, 1.0F);
        }
        GlStateManager.rotate(-2.5F, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(-180.0F, 1.0F, 0.0F, 0.0F);
        GlStateManager.rotate(180.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.scale(1.2F, 1.2F, 1.2F);
        Minecraft.getMinecraft().getRenderItem().renderItem(itemstack, ItemCameraTransforms.TransformType.GROUND);
        GlStateManager.popMatrix();
        GlStateManager.popMatrix();
    }

    protected void translateToHand(float scale, boolean left) {
        ModelMantisShrimp model = (ModelMantisShrimp) this.renderer.getMainModel();
        model.root.postRender(scale);
        model.body.postRender(scale);
        model.head.postRender(scale);
        if (left) {
            model.arm_left.postRender(scale);
            model.fist_left.postRender(scale);
        } else {
            model.arm_right.postRender(scale);
            model.fist_right.postRender(scale);
        }
    }

    @Override
    public boolean shouldCombineTextures() {
        return false;
    }
}
