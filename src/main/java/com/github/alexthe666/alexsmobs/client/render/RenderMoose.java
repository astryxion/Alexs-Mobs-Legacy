package com.github.alexthe666.alexsmobs.client.render;

import com.github.alexthe666.alexsmobs.client.model.ModelMoose;
import com.github.alexthe666.alexsmobs.client.render.layer.LayerMooseSnow;
import com.github.alexthe666.alexsmobs.entity.EntityMoose;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderMoose extends RenderLiving<EntityMoose> {
    private static final ResourceLocation TEXTURE_ANTLERED = new ResourceLocation("alexsmobs:textures/entity/moose_antlered.png");
    private static final ResourceLocation TEXTURE = new ResourceLocation("alexsmobs:textures/entity/moose.png");

    public RenderMoose(RenderManager renderManagerIn) {
        super(renderManagerIn, new ModelMoose(), 0.8F);
        this.addLayer(new LayerMooseSnow(this));
    }

    @Override
    protected void preRenderCallback(EntityMoose entitylivingbaseIn, float partialTickTime) {
        GlStateManager.scale(1.3F, 1.3F, 1.3F);
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityMoose entity) {
        return entity.isAntlered() && !entity.isChild() ? TEXTURE_ANTLERED : TEXTURE;
    }
}
