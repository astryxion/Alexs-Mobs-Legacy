package com.github.alexthe666.alexsmobs.client.render.layer;

import com.github.alexthe666.alexsmobs.client.model.ModelAnteater;
import com.github.alexthe666.alexsmobs.client.model.ModelLeafcutterAnt;
import com.github.alexthe666.alexsmobs.client.render.RenderAnteater;
import com.github.alexthe666.alexsmobs.entity.EntityAnteater;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class LayerAnteaterTongueItem implements LayerRenderer<EntityAnteater> {

    private static final ResourceLocation TEXTURE = new ResourceLocation("alexsmobs:textures/entity/leafcutter_ant.png");
    private final RenderAnteater renderer;
    private final ModelLeafcutterAnt ANT_MODEL = new ModelLeafcutterAnt();

    public LayerAnteaterTongueItem(RenderAnteater render) {
        this.renderer = render;
    }

    @Override
    public void doRenderLayer(EntityAnteater anteater, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
        ItemStack itemstack = anteater.getHeldItemMainhand();
        if (!itemstack.isEmpty() || anteater.hasAntOnTongue()) {
            double tongueM = Math.min(Math.sin(ageInTicks * 0.15F), 0);
            float scaleItem = -0.2F * (float) tongueM * (anteater.prevTongueProgress + (anteater.tongueProgress - anteater.prevTongueProgress) * partialTicks * 0.2F);

            GlStateManager.pushMatrix();
            if (anteater.isChild()) {
                GlStateManager.scale(0.35F, 0.35F, 0.35F);
                GlStateManager.translate(0.0D, 2.8D, 0D);
            }
            GlStateManager.pushMatrix();
            translateToTongue(scale);
            if (anteater.isChild()) {
                GlStateManager.translate(0.0D, 0.2F, -0.22D);
            }
            GlStateManager.translate(-0.0, 0.0F, -0.35F);
            GlStateManager.scale(scaleItem, scaleItem, scaleItem);
            if (anteater.hasAntOnTongue()) {
                GlStateManager.pushMatrix();
                GlStateManager.translate(0F, -1.35F, -0.01F);
                ANT_MODEL.animateAnteater(anteater, partialTicks);
                Minecraft.getMinecraft().getRenderManager().renderEngine.bindTexture(TEXTURE);
                ANT_MODEL.render(anteater, 0, 0, partialTicks, 0, 0, 0.0625F);
                GlStateManager.popMatrix();
            } else {
                GlStateManager.rotate(90F, 1.0F, 0.0F, 0.0F);
                Minecraft.getMinecraft().getRenderItem().renderItem(itemstack, ItemCameraTransforms.TransformType.GROUND);
            }
            GlStateManager.popMatrix();
            GlStateManager.popMatrix();
        }
    }

    protected void translateToTongue(float scale) {
        ModelAnteater model = (ModelAnteater) this.renderer.getMainModel();
        model.root.postRender(scale);
        model.body.postRender(scale);
        model.head.postRender(scale);
        model.snout.postRender(scale);
        model.tongue1.postRender(scale);
        model.tongue2.postRender(scale);
    }

    @Override
    public boolean shouldCombineTextures() {
        return false;
    }
}
