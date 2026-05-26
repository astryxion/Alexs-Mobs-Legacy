package com.github.alexthe666.alexsmobs.client.render;

import com.github.alexthe666.alexsmobs.client.model.ModelEnderiophage;
import com.github.alexthe666.alexsmobs.client.render.layer.LayerEnderiophageEyes;
import com.github.alexthe666.alexsmobs.entity.EntityEnderiophage;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderEnderiophage extends RenderLiving<EntityEnderiophage> {
    private static final ResourceLocation TEXTURE = new ResourceLocation("alexsmobs:textures/entity/enderiophage.png");
    private static final ResourceLocation TEXTURE_OVERWORLD = new ResourceLocation("alexsmobs:textures/entity/enderiophage_overworld.png");
    private static final ResourceLocation TEXTURE_NETHER = new ResourceLocation("alexsmobs:textures/entity/enderiophage_nether.png");

    public RenderEnderiophage(RenderManager renderManagerIn) {
        super(renderManagerIn, new ModelEnderiophage(), 0.5F);
        this.addLayer(new LayerEnderiophageEyes(this));
    }

    @Override
    protected void preRenderCallback(EntityEnderiophage entitylivingbaseIn, float partialTickTime) {
        float scale = entitylivingbaseIn.prevEnderiophageScale + (entitylivingbaseIn.getPhageScale() - entitylivingbaseIn.prevEnderiophageScale) * partialTickTime;
        GlStateManager.scale(0.8F * scale, 0.8F * scale, 0.8F * scale);
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityEnderiophage entity) {
        return entity.getVariant() == 2 ? TEXTURE_NETHER : entity.getVariant() == 1 ? TEXTURE_OVERWORLD : TEXTURE;
    }
}
