package com.github.alexthe666.alexsmobs.client.render.layer;

import com.github.alexthe666.alexsmobs.client.model.ModelFrilledShark;
import com.github.alexthe666.alexsmobs.client.render.AMRenderTypes;
import com.github.alexthe666.alexsmobs.client.render.RenderFrilledShark;
import com.github.alexthe666.alexsmobs.entity.EntityFrilledShark;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class LayerFrilledSharkTeeth implements LayerRenderer<EntityFrilledShark> {
    private static final ResourceLocation TEXTURE_TEETH = new ResourceLocation("alexsmobs:textures/entity/frilled_shark_teeth.png");
    private final RenderFrilledShark renderer;

    public LayerFrilledSharkTeeth(RenderFrilledShark render) {
        this.renderer = render;
    }

    @Override
    public void doRenderLayer(EntityFrilledShark entitylivingbaseIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
        this.renderer.bindTexture(TEXTURE_TEETH);
        AMRenderTypes.beginFrilledSharkTeethGlint();
        ((ModelFrilledShark) this.renderer.getMainModel()).render(entitylivingbaseIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
        AMRenderTypes.endFrilledSharkTeethGlint();
    }

    @Override
    public boolean shouldCombineTextures() {
        return true;
    }
}
