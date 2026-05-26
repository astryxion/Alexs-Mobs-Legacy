package com.github.alexthe666.alexsmobs.client.render.layer;

import com.github.alexthe666.alexsmobs.client.model.ModelAncientDart;
import com.github.alexthe666.alexsmobs.client.model.ModelCapuchinMonkey;
import com.github.alexthe666.alexsmobs.client.render.RenderCapuchinMonkey;
import com.github.alexthe666.alexsmobs.entity.EntityCapuchinMonkey;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class LayerCapuchinItem implements LayerRenderer<EntityCapuchinMonkey> {

    public static final ResourceLocation DART_TEXTURE = new ResourceLocation("alexsmobs:textures/entity/ancient_dart.png");
    public static final ModelAncientDart DART_MODEL = new ModelAncientDart();

    private final RenderCapuchinMonkey renderer;

    public LayerCapuchinItem(RenderCapuchinMonkey render) {
        this.renderer = render;
    }

    @Override
    public void doRenderLayer(EntityCapuchinMonkey entitylivingbaseIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
        if (entitylivingbaseIn.hasDart()) {
            GlStateManager.pushMatrix();
            if (entitylivingbaseIn.isChild()) {
                GlStateManager.scale(0.35F, 0.35F, 0.35F);
                GlStateManager.translate(0.5D, 2.6D, 0.15D);
                translateToHand(scale);
                GlStateManager.translate(-0.65F, -0.75F, -0.1F);
                GlStateManager.scale(2.8F, 2.8F, 2.8F);
            } else {
                translateToHand(scale);
            }
            float f = 0.0F;
            if (entitylivingbaseIn.getAnimation() == EntityCapuchinMonkey.ANIMATION_THROW) {
                if (entitylivingbaseIn.getAnimationTick() < 6) {
                    f = Math.min(3, entitylivingbaseIn.getAnimationTick() + partialTicks) * 60;
                } else {
                    f = (12 - (entitylivingbaseIn.getAnimationTick() + partialTicks)) * 30;
                }
            }
            GlStateManager.translate(0, 0.5F, 0F);
            GlStateManager.scale(1.2F, 1.2F, 1.2F);
            GlStateManager.pushMatrix();
            GlStateManager.rotate(f, 1.0F, 0.0F, 0.0F);
            this.renderer.bindTexture(DART_TEXTURE);
            DART_MODEL.render(entitylivingbaseIn, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, scale);
            GlStateManager.popMatrix();
            GlStateManager.popMatrix();
        } else if (entitylivingbaseIn.getAnimation() == EntityCapuchinMonkey.ANIMATION_THROW && entitylivingbaseIn.getAnimationTick() <= 5) {
            ItemStack itemstack = new ItemStack(net.minecraft.init.Blocks.COBBLESTONE);
            GlStateManager.pushMatrix();
            if (entitylivingbaseIn.isChild()) {
                GlStateManager.scale(0.35F, 0.35F, 0.35F);
                GlStateManager.translate(0.5D, 2.6D, 0.15D);
                translateToHand(scale);
                GlStateManager.translate(-0.4F, 0.75F, 0.0F);
                GlStateManager.scale(2.8F, 2.8F, 2.8F);
            } else {
                translateToHand(scale);
                GlStateManager.translate(0.125F, 0.5F, 0.1F);
            }
            GlStateManager.rotate(-2.5F, 0.0F, 1.0F, 0.0F);
            GlStateManager.rotate(-90.0F, 1.0F, 0.0F, 0.0F);
            Minecraft.getMinecraft().getRenderItem().renderItem(itemstack, ItemCameraTransforms.TransformType.GROUND);
            GlStateManager.popMatrix();
        }
    }

    protected void translateToHand(float scale) {
        ModelCapuchinMonkey model = (ModelCapuchinMonkey) this.renderer.getMainModel();
        model.root.postRender(scale);
        model.body.postRender(scale);
        model.arm_right.postRender(scale);
    }

    @Override
    public boolean shouldCombineTextures() {
        return false;
    }
}
