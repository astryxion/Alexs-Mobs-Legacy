package com.github.alexthe666.alexsmobs.client.render;

import com.github.alexthe666.alexsmobs.client.model.ModelSeagull;
import com.github.alexthe666.alexsmobs.entity.EntitySeagull;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderSeagull extends RenderLiving<EntitySeagull> {
    private static final ResourceLocation TEXTURE = new ResourceLocation("alexsmobs:textures/entity/seagull.png");
    private static final ResourceLocation TEXTURE_WINGULL = new ResourceLocation("alexsmobs:textures/entity/seagull_wingull.png");

    public RenderSeagull(RenderManager renderManagerIn) {
        super(renderManagerIn, new ModelSeagull(), 0.2F);
        this.addLayer(new LayerHeldItem(this));
    }

    @Override
    protected ResourceLocation getEntityTexture(EntitySeagull entity) {
        return entity.isWingull() ? TEXTURE_WINGULL : TEXTURE;
    }

    @SideOnly(Side.CLIENT)
    class LayerHeldItem implements LayerRenderer<EntitySeagull> {
        private final RenderSeagull renderer;

        LayerHeldItem(RenderSeagull render) {
            this.renderer = render;
        }

        @Override
        public void doRenderLayer(EntitySeagull entitylivingbaseIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
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
            GlStateManager.translate(0.0F, -0.24F, -0.25F);
            GlStateManager.rotate(-2.5F, 0.0F, 1.0F, 0.0F);
            GlStateManager.rotate(-90.0F, 1.0F, 0.0F, 0.0F);
            Minecraft.getMinecraft().getRenderItem().renderItem(itemstack, ItemCameraTransforms.TransformType.GROUND);
            GlStateManager.popMatrix();
            GlStateManager.popMatrix();
        }

        protected void translateToHand(float scale) {
            ModelSeagull model = (ModelSeagull) this.renderer.getMainModel();
            model.root.postRender(scale);
            model.body.postRender(scale);
            model.head.postRender(scale);
        }

        @Override
        public boolean shouldCombineTextures() {
            return false;
        }
    }
}
