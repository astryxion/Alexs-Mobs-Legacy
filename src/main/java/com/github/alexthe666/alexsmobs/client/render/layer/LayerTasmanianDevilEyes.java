package com.github.alexthe666.alexsmobs.client.render.layer;

import com.github.alexthe666.alexsmobs.client.model.ModelTasmanianDevil;
import com.github.alexthe666.alexsmobs.client.render.AMRenderTypes;
import com.github.alexthe666.alexsmobs.client.render.RenderTasmanianDevil;
import com.github.alexthe666.alexsmobs.entity.EntityTasmanianDevil;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class LayerTasmanianDevilEyes implements LayerRenderer<EntityTasmanianDevil> {
    private static final ResourceLocation TEXTURE_EYES = new ResourceLocation("alexsmobs:textures/entity/tasmanian_devil_eyes.png");
    private final RenderTasmanianDevil renderer;

    public LayerTasmanianDevilEyes(RenderTasmanianDevil render) {
        this.renderer = render;
    }

    @Override
    public void doRenderLayer(EntityTasmanianDevil entitylivingbaseIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
        if (entitylivingbaseIn.getAnimation() == EntityTasmanianDevil.ANIMATION_HOWL && entitylivingbaseIn.getAnimationTick() < 34) {
            this.renderer.bindTexture(TEXTURE_EYES);
            AMRenderTypes.beginEyesNoCull();
            ((ModelTasmanianDevil) this.renderer.getMainModel()).render(entitylivingbaseIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
            AMRenderTypes.endEyesNoCull();
        }
    }

    @Override
    public boolean shouldCombineTextures() {
        return true;
    }
}
