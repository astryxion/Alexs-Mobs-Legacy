package com.github.alexthe666.alexsmobs.client.render.layer;

import com.github.alexthe666.alexsmobs.client.model.ModelCentipedeHead;
import com.github.alexthe666.alexsmobs.client.render.AMRenderTypes;
import com.github.alexthe666.alexsmobs.client.render.RenderCentipedeHead;
import com.github.alexthe666.alexsmobs.entity.EntityCentipedeHead;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class LayerCentipedeHeadEyes implements LayerRenderer<EntityCentipedeHead> {
    private static final ResourceLocation TEXTURE = new ResourceLocation("alexsmobs:textures/entity/centipede_eyes.png");
    private final RenderCentipedeHead renderer;

    public LayerCentipedeHeadEyes(RenderCentipedeHead render) {
        this.renderer = render;
    }

    @Override
    public void doRenderLayer(EntityCentipedeHead entitylivingbaseIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
        this.renderer.bindTexture(TEXTURE);
        AMRenderTypes.beginEyesNoCull();
        ((ModelCentipedeHead) this.renderer.getMainModel()).render(entitylivingbaseIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
        AMRenderTypes.endEyesNoCull();
    }

    @Override
    public boolean shouldCombineTextures() {
        return true;
    }
}
