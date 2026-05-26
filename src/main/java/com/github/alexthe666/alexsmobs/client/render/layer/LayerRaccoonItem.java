package com.github.alexthe666.alexsmobs.client.render.layer;

import com.github.alexthe666.alexsmobs.client.model.ModelRaccoon;
import com.github.alexthe666.alexsmobs.client.render.RenderRaccoon;
import com.github.alexthe666.alexsmobs.entity.EntityRaccoon;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class LayerRaccoonItem implements LayerRenderer<EntityRaccoon> {
    private final RenderRaccoon renderer;

    public LayerRaccoonItem(RenderRaccoon render) {
        this.renderer = render;
    }

    @Override
    public void doRenderLayer(EntityRaccoon entitylivingbaseIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
        ItemStack itemstack = entitylivingbaseIn.getHeldItemMainhand();
        if (itemstack.isEmpty()) {
            return;
        }
        boolean inHand = entitylivingbaseIn.begProgress > 0 || entitylivingbaseIn.standProgress > 0 || entitylivingbaseIn.washProgress > 0;
        GlStateManager.pushMatrix();
        if (entitylivingbaseIn.isChild()) {
            GlStateManager.scale(0.5F, 0.5F, 0.5F);
            GlStateManager.translate(0.0D, 1.5D, 0.0D);
        }
        GlStateManager.pushMatrix();
        translateToHand(inHand, scale);
        if (inHand) {
            GlStateManager.translate(0.2F, 0.4F, 0.0F);
            GlStateManager.rotate(90.0F * entitylivingbaseIn.washProgress * 0.2F, 1.0F, 0.0F, 0.0F);
        } else {
            GlStateManager.translate(0.0F, 0.1F, -0.35F);
        }
        GlStateManager.rotate(-2.5F, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(-90.0F, 1.0F, 0.0F, 0.0F);
        Minecraft.getMinecraft().getRenderItem().renderItem(itemstack, ItemCameraTransforms.TransformType.GROUND);
        GlStateManager.popMatrix();
        GlStateManager.popMatrix();
    }

    protected void translateToHand(boolean inHand, float scale) {
        ModelRaccoon model = (ModelRaccoon) this.renderer.getMainModel();
        if (inHand) {
            model.root.postRender(scale);
            model.body.postRender(scale);
            model.arm_right.postRender(scale);
        } else {
            model.root.postRender(scale);
            model.body.postRender(scale);
            model.head.postRender(scale);
        }
    }

    @Override
    public boolean shouldCombineTextures() {
        return false;
    }
}
