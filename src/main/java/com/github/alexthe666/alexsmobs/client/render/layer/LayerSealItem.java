package com.github.alexthe666.alexsmobs.client.render.layer;

import com.github.alexthe666.alexsmobs.client.model.ModelSeal;
import com.github.alexthe666.alexsmobs.client.render.RenderSeal;
import com.github.alexthe666.alexsmobs.entity.EntitySeal;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class LayerSealItem implements LayerRenderer<EntitySeal> {
    private final RenderSeal renderer;

    public LayerSealItem(RenderSeal render) {
        this.renderer = render;
    }

    @Override
    public void doRenderLayer(EntitySeal entitylivingbaseIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
        ItemStack itemstack = entitylivingbaseIn.getHeldItemMainhand();
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
        if (entitylivingbaseIn.isChild()) {
            GlStateManager.translate(0.0D, 0.0D, -0.1D);
        }
        GlStateManager.translate(-0.1F, 0.05F, -0.1F);
        GlStateManager.rotate(-45.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(-90.0F, 1.0F, 0.0F, 0.0F);
        Minecraft.getMinecraft().getRenderItem().renderItem(itemstack, ItemCameraTransforms.TransformType.GROUND);
        GlStateManager.popMatrix();
        GlStateManager.popMatrix();
    }

    protected void translateToHand(float scale) {
        ModelSeal model = (ModelSeal) this.renderer.getMainModel();
        model.root.postRender(scale);
        model.body.postRender(scale);
        model.head.postRender(scale);
    }

    @Override
    public boolean shouldCombineTextures() {
        return false;
    }
}
