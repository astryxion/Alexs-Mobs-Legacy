package com.github.alexthe666.alexsmobs.client.render.layer;

import com.github.alexthe666.alexsmobs.client.model.ModelMimicube;
import com.github.alexthe666.alexsmobs.client.render.RenderMimicube;
import com.github.alexthe666.alexsmobs.entity.EntityMimicube;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemShield;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class LayerMimicubeHeldItem implements LayerRenderer<EntityMimicube> {
    private final RenderMimicube renderer;

    public LayerMimicubeHeldItem(RenderMimicube render) {
        this.renderer = render;
    }

    @Override
    public void doRenderLayer(EntityMimicube entitylivingbaseIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
        ItemStack itemRight = entitylivingbaseIn.getHeldItemMainhand();
        ItemStack itemLeft = entitylivingbaseIn.getHeldItemOffhand();
        float rightSwap = (entitylivingbaseIn.prevRightSwapProgress + (entitylivingbaseIn.rightSwapProgress - entitylivingbaseIn.prevRightSwapProgress) * partialTicks) * 0.2F;
        float leftSwap = (entitylivingbaseIn.prevLeftSwapProgress + (entitylivingbaseIn.leftSwapProgress - entitylivingbaseIn.prevLeftSwapProgress) * partialTicks) * 0.2F;
        float attackprogress = (entitylivingbaseIn.prevAttackProgress + (entitylivingbaseIn.attackProgress - entitylivingbaseIn.prevAttackProgress) * partialTicks);
        double bob1 = Math.cos(ageInTicks * 0.1F) * 0.1F + 0.1F;
        double bob2 = Math.sin(ageInTicks * 0.1F) * 0.1F + 0.1F;
        if (!itemRight.isEmpty()) {
            GlStateManager.pushMatrix();
            translateToHand(false, scale);
            GlStateManager.translate(-0.5F, (float) (0.1F - bob1), -0.1F);
            GlStateManager.scale(0.9F * (1.0F - rightSwap), 0.9F * (1.0F - rightSwap), 0.9F * (1.0F - rightSwap));
            GlStateManager.rotate(180.0F, 1.0F, 0.0F, 0.0F);
            GlStateManager.rotate(180.0F, 0.0F, 1.0F, 0.0F);
            if (itemRight.getItem() instanceof ItemShield) {
                GlStateManager.translate(-0.1F, 0.0F, -0.4F);
                GlStateManager.rotate(90.0F, 0.0F, 1.0F, 0.0F);
            }
            GlStateManager.rotate(-10.0F, 0.0F, 0.0F, 1.0F);
            GlStateManager.rotate(360.0F * rightSwap, 1.0F, 0.0F, 0.0F);
            GlStateManager.rotate(-40.0F * attackprogress, 1.0F, 0.0F, 0.0F);
            if (rightSwap > 0.0F) {
                OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 0.0F, 0.0F);
            }
            Minecraft.getMinecraft().getRenderItem().renderItem(itemRight, ItemCameraTransforms.TransformType.THIRD_PERSON_RIGHT_HAND);
            GlStateManager.popMatrix();
        }
        if (!itemLeft.isEmpty()) {
            GlStateManager.pushMatrix();
            translateToHand(false, scale);
            GlStateManager.translate(0.45F, (float) (0.1F - bob2), -0.1F);
            GlStateManager.scale(0.9F * (1.0F - leftSwap), 0.9F * (1.0F - leftSwap), 0.9F * (1.0F - leftSwap));
            GlStateManager.rotate(180.0F, 1.0F, 0.0F, 0.0F);
            GlStateManager.rotate(180.0F, 0.0F, 1.0F, 0.0F);
            if (itemLeft.getItem() instanceof ItemShield) {
                GlStateManager.translate(-0.2F, 0.0F, -0.4F);
                GlStateManager.rotate(90.0F, 0.0F, 1.0F, 0.0F);
            }
            GlStateManager.rotate(10.0F, 0.0F, 0.0F, 1.0F);
            GlStateManager.rotate(360.0F * leftSwap, 1.0F, 0.0F, 0.0F);
            if (leftSwap > 0.0F) {
                OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 0.0F, 0.0F);
            }
            Minecraft.getMinecraft().getRenderItem().renderItem(itemLeft, ItemCameraTransforms.TransformType.THIRD_PERSON_RIGHT_HAND);
            GlStateManager.popMatrix();
        }
    }

    protected void translateToHand(boolean left, float scale) {
        ModelMimicube model = (ModelMimicube) this.renderer.getMainModel();
        model.root.postRender(scale);
        model.innerbody.postRender(scale);
    }

    @Override
    public boolean shouldCombineTextures() {
        return false;
    }
}
