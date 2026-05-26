package com.github.alexthe666.alexsmobs.client.render;

import com.github.alexthe666.alexsmobs.client.model.ModelSnowLeopard;
import com.github.alexthe666.alexsmobs.entity.EntitySnowLeopard;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderSnowLeopard extends RenderLiving<EntitySnowLeopard> {
    private static final ResourceLocation TEXTURE = new ResourceLocation("alexsmobs:textures/entity/snow_leopard.png");
    private static final ResourceLocation TEXTURE_SLEEPING = new ResourceLocation("alexsmobs:textures/entity/snow_leopard_sleeping.png");

    public RenderSnowLeopard(RenderManager renderManagerIn) {
        super(renderManagerIn, new ModelSnowLeopard(), 0.4F);
    }

    @Override
    protected void preRenderCallback(EntitySnowLeopard entitylivingbaseIn, float partialTickTime) {
        GlStateManager.scale(0.9F, 0.9F, 0.9F);
    }

    @Override
    protected ResourceLocation getEntityTexture(EntitySnowLeopard entity) {
        return entity.isSleeping() ? TEXTURE_SLEEPING : TEXTURE;
    }
}
