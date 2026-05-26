package com.github.alexthe666.alexsmobs.client.render.layer;

import com.github.alexthe666.alexsmobs.client.render.AMRenderTypes;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.entity.EntityLiving;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class LayerBasicGlow<T extends EntityLiving> implements LayerRenderer<T> {
    private final RenderLiving<T> renderer;
    private final ResourceLocation texture;

    public LayerBasicGlow(RenderLiving<T> renderer, ResourceLocation texture) {
        this.renderer = renderer;
        this.texture = texture;
    }

    @Override
    public void doRenderLayer(T entitylivingbaseIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
        this.renderer.bindTexture(this.texture);
        AMRenderTypes.beginEyesNoCull();
        ((ModelBase) this.renderer.getMainModel()).render(entitylivingbaseIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
        AMRenderTypes.endEyesNoCull();
    }

    @Override
    public boolean shouldCombineTextures() {
        return true;
    }
}
