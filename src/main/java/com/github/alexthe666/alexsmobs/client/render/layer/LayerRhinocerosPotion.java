package com.github.alexthe666.alexsmobs.client.render.layer;

import com.github.alexthe666.alexsmobs.client.render.AMRenderTypes;
import com.github.alexthe666.alexsmobs.client.render.RenderRhinoceros;
import com.github.alexthe666.alexsmobs.entity.EntityRhinoceros;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class LayerRhinocerosPotion implements LayerRenderer<EntityRhinoceros> {
    private static final ResourceLocation TEXTURE_POTION = new ResourceLocation("alexsmobs:textures/entity/rhinoceros_potion.png");
    private final RenderRhinoceros renderer;

    public LayerRhinocerosPotion(RenderRhinoceros renderer) {
        this.renderer = renderer;
    }

    @Override
    public void doRenderLayer(EntityRhinoceros rhino, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
        int color = rhino.getPotionColor();
        if (color != -1 && !rhino.isInvisible()) {
            float r = (float) (color >> 16 & 255) / 255.0F;
            float g = (float) (color >> 8 & 255) / 255.0F;
            float b = (float) (color & 255) / 255.0F;
            this.renderer.bindTexture(TEXTURE_POTION);
            AMRenderTypes.beginEntityCutoutNoCull();
            GlStateManager.color(r, g, b, 1.0F);
            this.renderer.getMainModel().render(rhino, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            AMRenderTypes.endEntityCutoutNoCull();
        }
    }

    @Override
    public boolean shouldCombineTextures() {
        return true;
    }
}
