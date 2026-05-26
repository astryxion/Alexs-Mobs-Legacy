package com.github.alexthe666.alexsmobs.client.render;

import com.github.alexthe666.alexsmobs.client.model.ModelPlatypus;
import com.github.alexthe666.alexsmobs.client.render.layer.LayerPlatypusFedora;
import com.github.alexthe666.alexsmobs.entity.EntityPlatypus;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderPlatypus extends RenderLiving<EntityPlatypus> {
    private static final ResourceLocation TEXTURE = new ResourceLocation("alexsmobs:textures/entity/platypus.png");
    private static final ResourceLocation TEXTURE_PERRY = new ResourceLocation("alexsmobs:textures/entity/platypus_perry.png");

    public RenderPlatypus(RenderManager renderManagerIn) {
        super(renderManagerIn, new ModelPlatypus(), 0.45F);
        this.addLayer(new LayerPlatypusFedora(this));
    }

    @Override
    protected void preRenderCallback(EntityPlatypus entitylivingbaseIn, float partialTickTime) {
        GlStateManager.scale(0.9F, 0.9F, 0.9F);
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityPlatypus entity) {
        return entity.isPerry() ? TEXTURE_PERRY : TEXTURE;
    }
}
