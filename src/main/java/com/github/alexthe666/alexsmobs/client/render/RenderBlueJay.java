package com.github.alexthe666.alexsmobs.client.render;

import com.github.alexthe666.alexsmobs.client.model.ModelBlueJay;
import com.github.alexthe666.alexsmobs.client.render.layer.LayerBlueJayShiny;
import com.github.alexthe666.alexsmobs.entity.EntityBlueJay;
import com.github.alexthe666.alexsmobs.entity.EntityRaccoon;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderBlueJay extends RenderLiving<EntityBlueJay> {
    private static final ResourceLocation TEXTURE = new ResourceLocation("alexsmobs:textures/entity/blue_jay.png");

    public RenderBlueJay(RenderManager renderManagerIn) {
        super(renderManagerIn, new ModelBlueJay(), 0.2F);
        this.addLayer(new LayerBlueJayShiny(this));
    }

    @Override
    protected void preRenderCallback(EntityBlueJay entitylivingbaseIn, float partialTickTime) {
        GlStateManager.scale(0.9F, 0.9F, 0.9F);
        if (entitylivingbaseIn.isRiding() && entitylivingbaseIn.getRidingEntity() instanceof EntityRaccoon) {
            EntityRaccoon raccoon = (EntityRaccoon) entitylivingbaseIn.getRidingEntity();
            float begProgress = raccoon.prevBegProgress + (raccoon.begProgress - raccoon.prevBegProgress) * partialTickTime;
            float standProgress0 = raccoon.prevStandProgress + (raccoon.standProgress - raccoon.prevStandProgress) * partialTickTime;
            float sitProgress = raccoon.prevSitProgress + (raccoon.sitProgress - raccoon.prevSitProgress) * partialTickTime;
            float standProgress = Math.max(Math.max(begProgress, standProgress0) - sitProgress, 0);
            GlStateManager.translate(0F, -1.03F - sitProgress * 0.01F, 0F);
            GlStateManager.translate(0F, 0.05F + standProgress * 0.1F, -0.1F + standProgress * 0.1F);
        }
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityBlueJay entity) {
        return TEXTURE;
    }
}
