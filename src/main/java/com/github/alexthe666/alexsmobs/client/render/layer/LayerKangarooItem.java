package com.github.alexthe666.alexsmobs.client.render.layer;

import com.github.alexthe666.alexsmobs.client.model.ModelKangaroo;
import com.github.alexthe666.alexsmobs.client.render.RenderKangaroo;
import com.github.alexthe666.alexsmobs.entity.EntityKangaroo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.util.EnumHandSide;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class LayerKangarooItem implements LayerRenderer<EntityKangaroo> {

    private final RenderKangaroo renderer;

    public LayerKangarooItem(RenderKangaroo render) {
        this.renderer = render;
    }

    @Override
    public void doRenderLayer(EntityKangaroo entitylivingbaseIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
        ItemStack itemstack = entitylivingbaseIn.getItemStackFromSlot(EntityEquipmentSlot.MAINHAND);
        boolean left = entitylivingbaseIn.getPrimaryHand() == EnumHandSide.LEFT;
        GlStateManager.pushMatrix();
        if (entitylivingbaseIn.isChild()) {
            GlStateManager.scale(0.5F, 0.5F, 0.5F);
            GlStateManager.translate(0.0D, 1.5D, 0.0D);
        }
        GlStateManager.pushMatrix();
        translateToHand(scale, left);
        GlStateManager.translate(0.0F, 0.75F, -0.125F);
        GlStateManager.rotate(-110.0F, 1.0F, 0.0F, 0.0F);
        GlStateManager.rotate(180.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.scale(0.8F, 0.8F, 0.8F);
        Minecraft.getMinecraft().getRenderItem().renderItem(itemstack, left ? ItemCameraTransforms.TransformType.THIRD_PERSON_LEFT_HAND : ItemCameraTransforms.TransformType.THIRD_PERSON_RIGHT_HAND);
        GlStateManager.popMatrix();
        GlStateManager.popMatrix();
    }

    protected void translateToHand(float scale, boolean left) {
        ModelKangaroo model = (ModelKangaroo) this.renderer.getMainModel();
        model.root.postRender(scale);
        model.body.postRender(scale);
        model.chest.postRender(scale);
        if (left) {
            model.arm_left.postRender(scale);
        } else {
            model.arm_right.postRender(scale);
        }
    }

    @Override
    public boolean shouldCombineTextures() {
        return false;
    }
}
