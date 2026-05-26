package com.github.alexthe666.alexsmobs.client.render;

import com.github.alexthe666.alexsmobs.client.model.ModelOrca;
import com.github.alexthe666.alexsmobs.entity.EntityOrca;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderOrca extends RenderLiving<EntityOrca> {
    private static final ResourceLocation TEXTURE = new ResourceLocation("alexsmobs:textures/entity/orca.png");

    public RenderOrca(RenderManager renderManagerIn) {
        super(renderManagerIn, new ModelOrca(), 1.0F);
    }

    @Override
    protected void preRenderCallback(EntityOrca entitylivingbaseIn, float partialTickTime) {
        GlStateManager.scale(1.3F, 1.3F, 1.3F);
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityOrca entity) {
        return TEXTURE;
    }
}
