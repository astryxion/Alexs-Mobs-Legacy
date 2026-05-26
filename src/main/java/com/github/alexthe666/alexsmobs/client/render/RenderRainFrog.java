package com.github.alexthe666.alexsmobs.client.render;

import com.github.alexthe666.alexsmobs.client.model.ModelRainFrog;
import com.github.alexthe666.alexsmobs.entity.EntityRainFrog;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderRainFrog extends RenderLiving<EntityRainFrog> {
    private static final ResourceLocation TEXTURE = new ResourceLocation("alexsmobs:textures/entity/rain_frog_0.png");
    private static final ModelRainFrog MODEL = new ModelRainFrog();

    public RenderRainFrog(RenderManager renderManagerIn) {
        super(renderManagerIn, MODEL, 0.2F);
    }

    private static final ResourceLocation TEXTURE_1 = new ResourceLocation("alexsmobs:textures/entity/rain_frog_1.png");
    private static final ResourceLocation TEXTURE_2 = new ResourceLocation("alexsmobs:textures/entity/rain_frog_2.png");

    @Override
    protected void preRenderCallback(EntityRainFrog entity, float partialTicks) {
        GlStateManager.scale(0.9F, 0.9F, 0.9F);
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityRainFrog entity) {
        return entity.getVariant() == 2 ? TEXTURE_2 : entity.getVariant() == 1 ? TEXTURE_1 : TEXTURE;
    }
}
