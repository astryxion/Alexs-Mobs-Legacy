package com.github.alexthe666.alexsmobs.client.render.layer;

import com.github.alexthe666.alexsmobs.client.render.AMRenderTypes;
import com.github.alexthe666.alexsmobs.client.render.RenderBison;
import com.github.alexthe666.alexsmobs.entity.EntityBison;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class LayerBisonSnow implements LayerRenderer<EntityBison> {
    private static final ResourceLocation TEXTURE_BABY_SNOWY = new ResourceLocation("alexsmobs:textures/entity/bison_baby_snowy.png");
    private static final ResourceLocation TEXTURE_SNOWY = new ResourceLocation("alexsmobs:textures/entity/bison_snowy.png");
    private final RenderBison renderer;

    public LayerBisonSnow(RenderBison render) {
        this.renderer = render;
    }

    @Override
    public void doRenderLayer(EntityBison entitylivingbaseIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
        if (entitylivingbaseIn.isSnowy()) {
            this.renderer.bindTexture(entitylivingbaseIn.isChild() ? TEXTURE_BABY_SNOWY : TEXTURE_SNOWY);
            AMRenderTypes.beginEntityCutoutNoCull();
            this.renderer.getMainModel().render(entitylivingbaseIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
            AMRenderTypes.endEntityCutoutNoCull();
        }
    }

    @Override
    public boolean shouldCombineTextures() {
        return true;
    }
}
