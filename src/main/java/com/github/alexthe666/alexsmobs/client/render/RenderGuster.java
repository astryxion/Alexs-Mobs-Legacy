package com.github.alexthe666.alexsmobs.client.render;

import com.github.alexthe666.alexsmobs.client.model.ModelGuster;
import com.github.alexthe666.alexsmobs.client.render.layer.LayerGusterEyes;
import com.github.alexthe666.alexsmobs.entity.EntityGuster;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderGuster extends RenderLiving<EntityGuster> {
    private static final ResourceLocation TEXTURE = new ResourceLocation("alexsmobs:textures/entity/guster.png");
    private static final ResourceLocation TEXTURE_GOOGLY = new ResourceLocation("alexsmobs:textures/entity/guster_silly.png");
    private static final ResourceLocation TEXTURE_RED = new ResourceLocation("alexsmobs:textures/entity/guster_red.png");
    private static final ResourceLocation TEXTURE_SOUL = new ResourceLocation("alexsmobs:textures/entity/guster_soul.png");

    public RenderGuster(RenderManager renderManagerIn) {
        super(renderManagerIn, new ModelGuster(), 0.25F);
        this.addLayer(new LayerGusterEyes(this));
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityGuster entity) {
        if (entity.isGooglyEyes()) {
            return TEXTURE_GOOGLY;
        }
        if (entity.getVariant() == 2) {
            return TEXTURE_SOUL;
        }
        if (entity.getVariant() == 1) {
            return TEXTURE_RED;
        }
        return TEXTURE;
    }

    @Override
    protected void preRenderCallback(EntityGuster entity, float partialTickTime) {
        if (entity.isInvisible()) {
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        }
    }
}
