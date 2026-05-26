package com.github.alexthe666.alexsmobs.client.render;

import com.github.alexthe666.alexsmobs.client.model.ModelCockroach;
import com.github.alexthe666.alexsmobs.client.render.layer.LayerCockroachMaracas;
import com.github.alexthe666.alexsmobs.client.render.layer.LayerCockroachRainbow;
import com.github.alexthe666.alexsmobs.entity.EntityCockroach;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderCockroach extends RenderLiving<EntityCockroach> {
    private static final ResourceLocation TEXTURE = new ResourceLocation("alexsmobs:textures/entity/cockroach.png");

    public RenderCockroach(RenderManager renderManagerIn) {
        super(renderManagerIn, new ModelCockroach(), 0.3F);
        this.addLayer(new LayerCockroachRainbow(this));
        this.addLayer(new LayerCockroachMaracas(this));
    }

    @Override
    protected void preRenderCallback(EntityCockroach entitylivingbaseIn, float partialTickTime) {
        GlStateManager.scale(0.85F, 0.85F, 0.85F);
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityCockroach entity) {
        return TEXTURE;
    }
}
