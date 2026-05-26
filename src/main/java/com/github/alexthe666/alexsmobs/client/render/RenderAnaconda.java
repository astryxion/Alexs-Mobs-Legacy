package com.github.alexthe666.alexsmobs.client.render;

import com.github.alexthe666.alexsmobs.client.model.ModelAnaconda;
import com.github.alexthe666.alexsmobs.entity.EntityAnaconda;
import com.github.alexthe666.alexsmobs.entity.util.AnacondaPartIndex;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderAnaconda extends RenderLiving<EntityAnaconda> {
    private static final ResourceLocation TEXTURE = new ResourceLocation("alexsmobs:textures/entity/anaconda.png");
    private static final ResourceLocation TEXTURE_SHEDDING = new ResourceLocation("alexsmobs:textures/entity/anaconda_shedding.png");
    private static final ResourceLocation TEXTURE_YELLOW = new ResourceLocation("alexsmobs:textures/entity/anaconda_yellow.png");
    private static final ResourceLocation TEXTURE_YELLOW_SHEDDING = new ResourceLocation("alexsmobs:textures/entity/anaconda_yellow_shedding.png");

    public RenderAnaconda(RenderManager renderManagerIn) {
        super(renderManagerIn, new ModelAnaconda(AnacondaPartIndex.HEAD), 0.3F);
    }

    @Override
    protected void preRenderCallback(EntityAnaconda entitylivingbaseIn, float partialTickTime) {
        GlStateManager.scale(entitylivingbaseIn.getScale(), entitylivingbaseIn.getScale(), entitylivingbaseIn.getScale());
    }

    public static ResourceLocation getAnacondaTexture(boolean yellow, boolean shedding) {
        if (yellow) {
            return shedding ? TEXTURE_YELLOW_SHEDDING : TEXTURE_YELLOW;
        }
        return shedding ? TEXTURE_SHEDDING : TEXTURE;
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityAnaconda entity) {
        return getAnacondaTexture(entity.isYellow(), entity.isShedding());
    }
}
