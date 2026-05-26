package com.github.alexthe666.alexsmobs.client.render.layer;

import com.github.alexthe666.alexsmobs.client.model.ModelGorilla;
import com.github.alexthe666.alexsmobs.client.render.RenderGorilla;
import com.github.alexthe666.alexsmobs.entity.EntityGorilla;
import com.github.alexthe666.alexsmobs.item.AMItemRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class LayerGorillaItem implements LayerRenderer<EntityGorilla> {
    private final RenderGorilla renderer;

    public LayerGorillaItem(RenderGorilla render) {
        this.renderer = render;
    }

    @Override
    public void doRenderLayer(EntityGorilla entitylivingbaseIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
        ItemStack itemstack = entitylivingbaseIn.getItemStackFromSlot(EntityEquipmentSlot.MAINHAND);
        String name = entitylivingbaseIn.getName().toLowerCase();
        if (name.contains("harambe")) {
            ItemStack haloStack = new ItemStack(AMItemRegistry.HALO);
            GlStateManager.pushMatrix();
            translateToHead(scale);
            float f = 0.1F * (float) Math.sin((entitylivingbaseIn.ticksExisted + partialTicks) * 0.1F) + (entitylivingbaseIn.isChild() ? 0.2F : 0.0F);
            GlStateManager.translate(0.0F, -0.7F - f, -0.2F);
            GlStateManager.rotate(90.0F, 1.0F, 0.0F, 0.0F);
            GlStateManager.scale(1.3F, 1.3F, 1.3F);
            Minecraft.getMinecraft().getRenderItem().renderItem(haloStack, ItemCameraTransforms.TransformType.GROUND);
            GlStateManager.popMatrix();
        }
        GlStateManager.pushMatrix();
        if (entitylivingbaseIn.isChild()) {
            GlStateManager.scale(0.35F, 0.35F, 0.35F);
            GlStateManager.translate(-0.1D, 2.0D, -1.15D);
            translateToHand(false, scale);
            GlStateManager.translate(-0.4F, 0.75F, 0.0F);
            GlStateManager.scale(2.8F, 2.8F, 2.8F);
        } else {
            translateToHand(false, scale);
            GlStateManager.translate(-0.4F, 0.75F, 0.0F);
        }
        GlStateManager.rotate(-2.5F, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(-90.0F, 1.0F, 0.0F, 0.0F);
        if (itemstack.getItem() instanceof ItemBlock) {
            GlStateManager.scale(2.0F, 2.0F, 2.0F);
        }
        Minecraft.getMinecraft().getRenderItem().renderItem(itemstack, ItemCameraTransforms.TransformType.GROUND);
        GlStateManager.popMatrix();
    }

    protected void translateToHand(boolean left, float scale) {
        ModelGorilla model = (ModelGorilla) this.renderer.getMainModel();
        model.root.postRender(scale);
        model.body.postRender(scale);
        model.chest.postRender(scale);
        model.leftArm.postRender(scale);
    }

    protected void translateToHead(float scale) {
        ModelGorilla model = (ModelGorilla) this.renderer.getMainModel();
        model.root.postRender(scale);
        model.body.postRender(scale);
        model.chest.postRender(scale);
        model.head.postRender(scale);
    }

    @Override
    public boolean shouldCombineTextures() {
        return false;
    }
}
