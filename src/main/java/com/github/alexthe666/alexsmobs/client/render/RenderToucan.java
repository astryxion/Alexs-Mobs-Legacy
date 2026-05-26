package com.github.alexthe666.alexsmobs.client.render;

import com.github.alexthe666.alexsmobs.client.model.ModelToucan;
import com.github.alexthe666.alexsmobs.entity.EntityToucan;
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
public class RenderToucan extends RenderLiving<EntityToucan> {
    private static final ResourceLocation TEXTURE_0 = new ResourceLocation("alexsmobs:textures/entity/toucan/toucan_0.png");
    private static final ResourceLocation TEXTURE_1 = new ResourceLocation("alexsmobs:textures/entity/toucan/toucan_1.png");
    private static final ResourceLocation TEXTURE_2 = new ResourceLocation("alexsmobs:textures/entity/toucan/toucan_2.png");
    private static final ResourceLocation TEXTURE_3 = new ResourceLocation("alexsmobs:textures/entity/toucan/toucan_3.png");
    private static final ResourceLocation TEXTURE_GOLDEN = new ResourceLocation("alexsmobs:textures/entity/toucan/toucan_gold.png");
    private static final ResourceLocation TEXTURE_SAM = new ResourceLocation("alexsmobs:textures/entity/toucan/toucan_sam.png");

    public RenderToucan(RenderManager renderManagerIn) {
        super(renderManagerIn, new ModelToucan(), 0.2F);
        this.addLayer(new LayerHeldItem(this));
    }

    @Override
    protected void preRenderCallback(EntityToucan entitylivingbaseIn, float partialTickTime) {
        GlStateManager.scale(0.9F, 0.9F, 0.9F);
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityToucan entity) {
        if (entity.isSam()) {
            return TEXTURE_SAM;
        }
        if (entity.isGolden()) {
            return TEXTURE_GOLDEN;
        }
        switch (entity.getVariant()) {
            case 3:
                return TEXTURE_3;
            case 2:
                return TEXTURE_2;
            case 1:
                return TEXTURE_1;
            default:
                return TEXTURE_0;
        }
    }

    @SideOnly(Side.CLIENT)
    class LayerHeldItem implements LayerRenderer<EntityToucan> {
        private final RenderToucan renderer;

        LayerHeldItem(RenderToucan render) {
            this.renderer = render;
        }

        @Override
        public void doRenderLayer(EntityToucan entitylivingbaseIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
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
            GlStateManager.translate(-0.07F, -0.1F, -0.25F);
            GlStateManager.rotate(-45.0F, 0.0F, 1.0F, 0.0F);
            GlStateManager.rotate(-90.0F, 1.0F, 0.0F, 0.0F);
            Minecraft.getMinecraft().getRenderItem().renderItem(itemstack, ItemCameraTransforms.TransformType.GROUND);
            GlStateManager.popMatrix();
            GlStateManager.popMatrix();
        }

        protected void translateToHand(float scale) {
            ModelToucan model = (ModelToucan) this.renderer.getMainModel();
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
