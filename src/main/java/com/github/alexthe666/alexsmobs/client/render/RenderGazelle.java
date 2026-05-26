package com.github.alexthe666.alexsmobs.client.render;

import com.github.alexthe666.alexsmobs.client.model.ModelGazelle;
import com.github.alexthe666.alexsmobs.entity.EntityGazelle;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderGazelle extends RenderLiving<EntityGazelle> {
    private static final ResourceLocation TEXTURE = new ResourceLocation("alexsmobs:textures/entity/gazelle.png");

    public RenderGazelle(RenderManager renderManagerIn) {
        super(renderManagerIn, new ModelGazelle(), 0.4F);
    }

    @Override
    protected void preRenderCallback(EntityGazelle entitylivingbaseIn, float partialTickTime) {
        GlStateManager.scale(0.8F, 0.8F, 0.8F);
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityGazelle entity) {
        return TEXTURE;
    }
}
