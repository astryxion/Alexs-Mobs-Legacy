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
public class LayerGrizzlyHoney implements LayerRenderer<EntityGrizzlyBear> {
    private static final ResourceLocation TEXTURE = new ResourceLocation("alexsmobs:textures/entity/grizzly_bear_honey.png");
    private final RenderGrizzlyBear renderer;

    public LayerGrizzlyHoney(RenderGrizzlyBear renderGrizzlyBear) {
        this.renderer = renderGrizzlyBear;
    }

    @Override
    public void doRenderLayer(EntityGrizzlyBear entitylivingbaseIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
        if (entitylivingbaseIn.isHoneyed()) {
            this.renderer.bindTexture(TEXTURE);
            AMRenderTypes.beginEntityTranslucent();
            ((ModelGrizzlyBear) this.renderer.getMainModel()).render(entitylivingbaseIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
            AMRenderTypes.endEntityTranslucent();
        }
    }

    @Override
    public boolean shouldCombineTextures() {
        return true;
    }
}
