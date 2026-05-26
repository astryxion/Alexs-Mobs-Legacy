package com.github.alexthe666.alexsmobs.client.render.layer;

import com.github.alexthe666.alexsmobs.client.model.ModelSoulVulture;
import com.github.alexthe666.alexsmobs.client.render.AMRenderTypes;
import com.github.alexthe666.alexsmobs.client.render.RenderSoulVulture;
import com.github.alexthe666.alexsmobs.entity.EntitySoulVulture;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class LayerSoulVultureGlow implements LayerRenderer<EntitySoulVulture> {
    private static final ResourceLocation TEXTURE = new ResourceLocation("alexsmobs:textures/entity/soul_vulture_glow.png");
    private final RenderSoulVulture renderer;

    public LayerSoulVultureGlow(RenderSoulVulture renderSoulVulture) {
        this.renderer = renderSoulVulture;
    }

    @Override
    public void doRenderLayer(EntitySoulVulture entitylivingbaseIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
        float alpha = 0.75F + (MathHelper.cos(ageInTicks * 0.2F) + 1.0F) * 0.125F;
        this.renderer.bindTexture(TEXTURE);
        AMRenderTypes.beginEyesFlickering(alpha);
        ((ModelSoulVulture) this.renderer.getMainModel()).render(entitylivingbaseIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
        AMRenderTypes.endEyesFlickering();
    }

    @Override
    public boolean shouldCombineTextures() {
        return true;
    }
}
