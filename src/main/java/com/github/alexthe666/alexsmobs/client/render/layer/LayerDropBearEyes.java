package com.github.alexthe666.alexsmobs.client.render.layer;

import com.github.alexthe666.alexsmobs.client.model.ModelDropBear;
import com.github.alexthe666.alexsmobs.client.render.AMRenderTypes;
import com.github.alexthe666.alexsmobs.client.render.RenderDropBear;
import com.github.alexthe666.alexsmobs.entity.EntityDropBear;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class LayerDropBearEyes implements LayerRenderer<EntityDropBear> {
    private static final ResourceLocation TEXTURE_EYES = new ResourceLocation("alexsmobs:textures/entity/dropbear_eyes.png");
    private final RenderDropBear renderer;

    public LayerDropBearEyes(RenderDropBear render) {
        this.renderer = render;
    }

    @Override
    public void doRenderLayer(EntityDropBear entitylivingbaseIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
        this.renderer.bindTexture(TEXTURE_EYES);
        AMRenderTypes.beginEyesNoCull();
        ((ModelDropBear) this.renderer.getMainModel()).render(entitylivingbaseIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
        AMRenderTypes.endEyesNoCull();
    }

    @Override
    public boolean shouldCombineTextures() {
        return true;
    }
}
