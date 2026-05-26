package com.github.alexthe666.alexsmobs.client.render;

import com.github.alexthe666.alexsmobs.client.model.ModelTriops;
import com.github.alexthe666.alexsmobs.entity.EntityTriops;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderTriops extends RenderLiving<EntityTriops> {
    private static final ResourceLocation TEXTURE = new ResourceLocation("alexsmobs:textures/entity/triops.png");
    private static final ModelTriops MODEL = new ModelTriops();

    public RenderTriops(RenderManager renderManagerIn) {
        super(renderManagerIn, MODEL, 0.2F);
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityTriops entity) {
        return TEXTURE;
    }

    @Override
    protected void preRenderCallback(EntityTriops entity, float partialTicks) {
        float scale = entity.getTriopsScale();
        if (entity.isBaby()) {
            scale *= 0.65F;
        }
        GlStateManager.scale(scale, scale, scale);
    }
}
