package com.github.alexthe666.alexsmobs.client.render;

import com.github.alexthe666.alexsmobs.client.model.ModelManedWolf;
import com.github.alexthe666.alexsmobs.entity.EntityManedWolf;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderManedWolf extends RenderLiving<EntityManedWolf> {
    private static final ResourceLocation TEXTURE = new ResourceLocation("alexsmobs:textures/entity/maned_wolf.png");
    private static final ResourceLocation TEXTURE_ENDER = new ResourceLocation("alexsmobs:textures/entity/maned_wolf_ender.png");

    public RenderManedWolf(RenderManager renderManagerIn) {
        super(renderManagerIn, new ModelManedWolf(), 0.45F);
    }

    @Override
    protected void preRenderCallback(EntityManedWolf entitylivingbaseIn, float partialTickTime) {
        GlStateManager.scale(0.85F, 0.85F, 0.85F);
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityManedWolf entity) {
        return entity.isEnder() ? TEXTURE_ENDER : TEXTURE;
    }
}
