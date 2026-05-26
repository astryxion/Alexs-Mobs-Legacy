package com.github.alexthe666.alexsmobs.client.render.layer;

import com.github.alexthe666.alexsmobs.client.render.AMRenderTypes;
import com.github.alexthe666.alexsmobs.client.render.RenderMoose;
import com.github.alexthe666.alexsmobs.entity.EntityMoose;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class LayerMooseSnow implements LayerRenderer<EntityMoose> {
    private static final ResourceLocation TEXTURE_SNOWY = new ResourceLocation("alexsmobs:textures/entity/moose_snowy.png");
    private static final ResourceLocation TEXTURE_SNOWY_ANTLERED = new ResourceLocation("alexsmobs:textures/entity/moose_snowy_antlered.png");
    private final RenderMoose renderer;

    public LayerMooseSnow(RenderMoose render) {
        this.renderer = render;
    }

    @Override
    public void doRenderLayer(EntityMoose entitylivingbaseIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
        if (entitylivingbaseIn.isSnowy()) {
            this.renderer.bindTexture(entitylivingbaseIn.isAntlered() && !entitylivingbaseIn.isChild() ? TEXTURE_SNOWY_ANTLERED : TEXTURE_SNOWY);
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
