package com.github.alexthe666.alexsmobs.client.render;

import com.github.alexthe666.alexsmobs.client.model.ModelFlyingFish;
import com.github.alexthe666.alexsmobs.entity.EntityFlyingFish;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderFlyingFish extends RenderLiving<EntityFlyingFish> {
    private static final ResourceLocation TEXTURE_0 = new ResourceLocation("alexsmobs:textures/entity/flying_fish_0.png");
    private static final ResourceLocation TEXTURE_1 = new ResourceLocation("alexsmobs:textures/entity/flying_fish_1.png");
    private static final ResourceLocation TEXTURE_2 = new ResourceLocation("alexsmobs:textures/entity/flying_fish_2.png");

    public RenderFlyingFish(RenderManager renderManagerIn) {
        super(renderManagerIn, new ModelFlyingFish(), 0.2F);
    }

    @Override
    protected void preRenderCallback(EntityFlyingFish entitylivingbaseIn, float partialTickTime) {
        GlStateManager.scale(0.8F, 0.8F, 0.8F);
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityFlyingFish entity) {
        switch (entity.getVariant()) {
            case 1:
                return TEXTURE_1;
            case 2:
                return TEXTURE_2;
            default:
                return TEXTURE_0;
        }
    }
}
