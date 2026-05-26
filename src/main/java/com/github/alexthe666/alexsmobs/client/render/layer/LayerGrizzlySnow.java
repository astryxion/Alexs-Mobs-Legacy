package com.github.alexthe666.alexsmobs.client.render.layer;

import com.github.alexthe666.alexsmobs.client.model.ModelGrizzlyBear;
import com.github.alexthe666.alexsmobs.client.render.AMRenderTypes;
import com.github.alexthe666.alexsmobs.client.render.RenderGrizzlyBear;
import com.github.alexthe666.alexsmobs.entity.EntityGrizzlyBear;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class LayerGrizzlySnow implements LayerRenderer<EntityGrizzlyBear> {
    private static final ResourceLocation TEXTURE_SNOWY = new ResourceLocation("alexsmobs:textures/entity/grizzly_bear_snowy.png");
    private final RenderGrizzlyBear renderer;

    public LayerGrizzlySnow(RenderGrizzlyBear renderer) {
        this.renderer = renderer;
    }

    @Override
    public void doRenderLayer(EntityGrizzlyBear entitylivingbaseIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
        if (entitylivingbaseIn.isSnowy()) {
            this.renderer.bindTexture(TEXTURE_SNOWY);
            AMRenderTypes.beginEntityCutoutNoCull();
            ((ModelGrizzlyBear) this.renderer.getMainModel()).render(entitylivingbaseIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
            AMRenderTypes.endEntityCutoutNoCull();
        }
    }

    @Override
    public boolean shouldCombineTextures() {
        return true;
    }
}
