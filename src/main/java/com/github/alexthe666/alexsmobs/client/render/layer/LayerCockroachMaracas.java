package com.github.alexthe666.alexsmobs.client.render.layer;

import com.github.alexthe666.alexsmobs.client.model.ModelCockroach;
import com.github.alexthe666.alexsmobs.client.model.ModelSombrero;
import com.github.alexthe666.alexsmobs.client.render.AMRenderTypes;
import com.github.alexthe666.alexsmobs.client.render.RenderCockroach;
import com.github.alexthe666.alexsmobs.entity.EntityCockroach;
import com.github.alexthe666.alexsmobs.item.AMItemRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class LayerCockroachMaracas implements LayerRenderer<EntityCockroach> {

    private final ItemStack stack;
    private final ModelSombrero sombrero;
    private static final ResourceLocation SOMBRERO_TEX = new ResourceLocation("alexsmobs:textures/armor/sombrero.png");
    private final RenderCockroach renderer;

    public LayerCockroachMaracas(RenderCockroach render) {
        this.renderer = render;
        this.stack = new ItemStack(AMItemRegistry.MARACA);
        this.sombrero = new ModelSombrero(0.0F);
    }

    @Override
    public void doRenderLayer(EntityCockroach entitylivingbaseIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
        if (entitylivingbaseIn.hasMaracas()) {
            GlStateManager.pushMatrix();
            if (entitylivingbaseIn.isChild()) {
                GlStateManager.scale(0.65F, 0.65F, 0.65F);
                GlStateManager.translate(0.0D, 0.815D, 0.125D);
            }
            GlStateManager.pushMatrix();
            translateToHand(0, scale);
            GlStateManager.translate(-0.25F, 0.0F, 0.0F);
            GlStateManager.scale(1.4F, 1.4F, 1.4F);
            GlStateManager.rotate(-90.0F, 1.0F, 0.0F, 0.0F);
            GlStateManager.rotate(60.0F, 0.0F, 0.0F, 1.0F);
            Minecraft.getMinecraft().getRenderItem().renderItem(stack, ItemCameraTransforms.TransformType.GROUND);
            GlStateManager.popMatrix();
            GlStateManager.pushMatrix();
            translateToHand(1, scale);
            GlStateManager.translate(0.25F, 0.0F, 0.0F);
            GlStateManager.scale(1.4F, 1.4F, 1.4F);
            GlStateManager.rotate(90.0F, 1.0F, 0.0F, 0.0F);
            GlStateManager.rotate(-120.0F, 0.0F, 0.0F, 1.0F);
            Minecraft.getMinecraft().getRenderItem().renderItem(stack, ItemCameraTransforms.TransformType.GROUND);
            GlStateManager.popMatrix();
            GlStateManager.pushMatrix();
            translateToHand(2, scale);
            GlStateManager.translate(-0.35F, 0.0F, 0.0F);
            GlStateManager.scale(1.4F, 1.4F, 1.4F);
            GlStateManager.rotate(-90.0F, 1.0F, 0.0F, 0.0F);
            GlStateManager.rotate(60.0F, 0.0F, 0.0F, 1.0F);
            Minecraft.getMinecraft().getRenderItem().renderItem(stack, ItemCameraTransforms.TransformType.GROUND);
            GlStateManager.popMatrix();
            GlStateManager.pushMatrix();
            translateToHand(3, scale);
            GlStateManager.translate(0.35F, 0.0F, 0.0F);
            GlStateManager.scale(1.4F, 1.4F, 1.4F);
            GlStateManager.rotate(90.0F, 1.0F, 0.0F, 0.0F);
            GlStateManager.rotate(-120.0F, 0.0F, 0.0F, 1.0F);
            Minecraft.getMinecraft().getRenderItem().renderItem(stack, ItemCameraTransforms.TransformType.GROUND);
            GlStateManager.popMatrix();
            if (!entitylivingbaseIn.isHeadless()) {
                GlStateManager.pushMatrix();
                translateToHand(4, scale);
                GlStateManager.translate(0.0F, -0.4F, -0.01F);
                GlStateManager.translate(0.0F, entitylivingbaseIn.danceProgress * 0.045F, entitylivingbaseIn.danceProgress * -0.09F);
                GlStateManager.scale(0.8F, 0.8F, 0.8F);
                GlStateManager.rotate(60.0F * entitylivingbaseIn.danceProgress * 0.2F, 1.0F, 0.0F, 0.0F);
                this.renderer.bindTexture(SOMBRERO_TEX);
                AMRenderTypes.beginEntityCutoutNoCull();
                sombrero.render(entitylivingbaseIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
                AMRenderTypes.endEntityCutoutNoCull();
                GlStateManager.popMatrix();
            }
            GlStateManager.popMatrix();
        }
    }

    protected void translateToHand(int handIndex, float scale) {
        ModelCockroach model = (ModelCockroach) this.renderer.getMainModel();
        model.root.postRender(scale);
        model.abdomen.postRender(scale);
        if (handIndex == 0) {
            model.right_leg_front.postRender(scale);
        } else if (handIndex == 1) {
            model.left_leg_front.postRender(scale);
        } else if (handIndex == 2) {
            model.right_leg_mid.postRender(scale);
        } else if (handIndex == 3) {
            model.left_leg_mid.postRender(scale);
        } else {
            model.neck.postRender(scale);
            model.head.postRender(scale);
        }
    }

    @Override
    public boolean shouldCombineTextures() {
        return false;
    }
}
