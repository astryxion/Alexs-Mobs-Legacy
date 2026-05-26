package com.github.alexthe666.alexsmobs.client.render.layer;

import com.github.alexthe666.alexsmobs.client.model.ModelMimicube;
import com.github.alexthe666.alexsmobs.client.render.AMRenderTypes;
import com.github.alexthe666.alexsmobs.client.render.RenderMimicube;
import com.github.alexthe666.alexsmobs.entity.EntityMimicube;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class LayerMimicubeTexture implements LayerRenderer<EntityMimicube> {
    private static final ResourceLocation TEXTURE = new ResourceLocation("alexsmobs:textures/entity/mimicube_outer.png");
    private final RenderMimicube renderer;

    public LayerMimicubeTexture(RenderMimicube render) {
        this.renderer = render;
    }

    @Override
    public void doRenderLayer(EntityMimicube entitylivingbaseIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
        this.renderer.bindTexture(TEXTURE);
        AMRenderTypes.beginTransparentMimicube();
        ((ModelMimicube) this.renderer.getMainModel()).render(entitylivingbaseIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
        AMRenderTypes.endTransparentMimicube();
    }

    @Override
    public boolean shouldCombineTextures() {
        return true;
    }
}
