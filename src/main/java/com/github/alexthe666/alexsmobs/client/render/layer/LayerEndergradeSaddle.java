package com.github.alexthe666.alexsmobs.client.render.layer;

import com.github.alexthe666.alexsmobs.client.model.ModelEndergrade;
import com.github.alexthe666.alexsmobs.client.render.RenderEndergrade;
import com.github.alexthe666.alexsmobs.entity.EntityEndergrade;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class LayerEndergradeSaddle implements LayerRenderer<EntityEndergrade> {
    private static final ResourceLocation TEXTURE = new ResourceLocation("alexsmobs:textures/entity/endergrade_saddle.png");
    private final RenderEndergrade renderer;

    public LayerEndergradeSaddle(RenderEndergrade render) {
        this.renderer = render;
    }

    @Override
    public void doRenderLayer(EntityEndergrade entitylivingbaseIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
        if (entitylivingbaseIn.isSaddled()) {
            this.renderer.bindTexture(TEXTURE);
            ((ModelEndergrade) this.renderer.getMainModel()).render(entitylivingbaseIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
        }
    }

    @Override
    public boolean shouldCombineTextures() {
        return true;
    }
}
