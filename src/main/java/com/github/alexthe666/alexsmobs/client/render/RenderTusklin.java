package com.github.alexthe666.alexsmobs.client.render;

import com.github.alexthe666.alexsmobs.client.model.ModelTusklin;
import com.github.alexthe666.alexsmobs.client.render.layer.LayerTusklinGear;
import com.github.alexthe666.alexsmobs.entity.EntityTusklin;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderTusklin extends RenderLiving<EntityTusklin> {
    private static final ResourceLocation TEXTURE = new ResourceLocation("alexsmobs:textures/entity/tusklin.png");
    private static final ModelTusklin MODEL = new ModelTusklin();

    public RenderTusklin(RenderManager renderManagerIn) {
        super(renderManagerIn, MODEL, 1.0F);
        this.addLayer(new LayerTusklinGear(this));
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityTusklin entity) {
        return TEXTURE;
    }

    @Override
    protected void preRenderCallback(EntityTusklin entitylivingbaseIn, float partialTickTime) {
        if (entitylivingbaseIn.isInNether()) {
            float f = entitylivingbaseIn.ticksExisted + partialTickTime;
            GlStateManager.translate(0.015F * (float) Math.sin(f * 0.25F), 0.0F, 0.0F);
        }
    }
}
