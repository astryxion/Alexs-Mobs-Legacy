package com.github.alexthe666.alexsmobs.client.render.layer;

import com.github.alexthe666.alexsmobs.client.model.ModelElephant;
import com.github.alexthe666.alexsmobs.client.render.RenderElephant;
import com.github.alexthe666.alexsmobs.entity.EntityElephant;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class LayerElephantItem implements LayerRenderer<EntityElephant> {
    private final RenderElephant renderer;

    public LayerElephantItem(RenderElephant render) {
        this.renderer = render;
    }

    @Override
    public void doRenderLayer(EntityElephant entitylivingbaseIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
        ItemStack itemstack = entitylivingbaseIn.getHeldItemMainhand();
        if (itemstack.isEmpty()) {
            return;
        }
        GlStateManager.pushMatrix();
        if (entitylivingbaseIn.isChild()) {
            GlStateManager.scale(0.35F, 0.35F, 0.35F);
            GlStateManager.translate(0.0D, 2.8D, 0.0D);
        }
        GlStateManager.pushMatrix();
        translateToHand(scale);
        if (entitylivingbaseIn.isChild()) {
            GlStateManager.translate(0.0D, 0.2F, -0.22D);
        }
        GlStateManager.translate(0.0F, 1.0F, 0.15F);
        GlStateManager.rotate(180.0F, 1.0F, 0.0F, 0.0F);
        GlStateManager.scale(1.3F, 1.3F, 1.3F);
        IBakedModel baked = Minecraft.getMinecraft().getRenderItem().getItemModelWithOverrides(itemstack, entitylivingbaseIn.world, entitylivingbaseIn);
        if (baked.isGui3d()) {
            GlStateManager.translate(-0.05F, -0.1F, -0.15F);
            GlStateManager.scale(2.0F, 2.0F, 2.0F);
        }
        Minecraft.getMinecraft().getRenderItem().renderItem(itemstack, ItemCameraTransforms.TransformType.GROUND);
        GlStateManager.popMatrix();
        GlStateManager.popMatrix();
    }

    protected void translateToHand(float scale) {
        ModelElephant model = (ModelElephant) this.renderer.getMainModel();
        model.root.postRender(scale);
        model.body.postRender(scale);
        model.head.postRender(scale);
        model.trunk1.postRender(scale);
        model.trunk2.postRender(scale);
    }

    @Override
    public boolean shouldCombineTextures() {
        return false;
    }
}
