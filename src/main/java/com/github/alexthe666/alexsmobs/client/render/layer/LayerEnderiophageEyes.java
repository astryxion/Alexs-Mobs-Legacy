package com.github.alexthe666.alexsmobs.client.render.layer;

import com.github.alexthe666.alexsmobs.client.render.AMRenderTypes;
import com.github.alexthe666.alexsmobs.client.render.RenderEnderiophage;
import com.github.alexthe666.alexsmobs.entity.EntityEnderiophage;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class LayerEnderiophageEyes implements LayerRenderer<EntityEnderiophage> {
    private static final ResourceLocation TEXTURE_GLOW = new ResourceLocation("alexsmobs:textures/entity/enderiophage_glow.png");
    private static final ResourceLocation TEXTURE_OVERWORLD_GLOW = new ResourceLocation("alexsmobs:textures/entity/enderiophage_overworld_glow.png");
    private static final ResourceLocation TEXTURE_NETHER_GLOW = new ResourceLocation("alexsmobs:textures/entity/enderiophage_nether_glow.png");
    private final RenderEnderiophage renderer;

    public LayerEnderiophageEyes(RenderEnderiophage render) {
        this.renderer = render;
    }

    @Override
    public void doRenderLayer(EntityEnderiophage entitylivingbaseIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
        ResourceLocation glow = entitylivingbaseIn.getVariant() == 2 ? TEXTURE_NETHER_GLOW : entitylivingbaseIn.getVariant() == 1 ? TEXTURE_OVERWORLD_GLOW : TEXTURE_GLOW;
        this.renderer.bindTexture(glow);
        AMRenderTypes.beginGhost();
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0F, 240.0F);
        this.renderer.getMainModel().render(entitylivingbaseIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
        AMRenderTypes.endGhost();
    }

    @Override
    public boolean shouldCombineTextures() {
        return true;
    }
}
