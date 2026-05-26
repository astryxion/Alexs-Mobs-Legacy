package com.github.alexthe666.alexsmobs.client.render;

import com.github.alexthe666.alexsmobs.client.model.ModelTarantulaHawk;
import com.github.alexthe666.alexsmobs.client.model.ModelTarantulaHawkBaby;
import com.github.alexthe666.alexsmobs.entity.EntityTarantulaHawk;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderTarantulaHawk extends RenderLiving<EntityTarantulaHawk> {
    private static final ResourceLocation TEXTURE = new ResourceLocation("alexsmobs:textures/entity/tarantula_hawk.png");
    private static final ResourceLocation TEXTURE_ANGRY = new ResourceLocation("alexsmobs:textures/entity/tarantula_hawk_angry.png");
    private static final ResourceLocation TEXTURE_NETHER = new ResourceLocation("alexsmobs:textures/entity/tarantula_hawk_nether.png");
    private static final ResourceLocation TEXTURE_NETHER_ANGRY = new ResourceLocation("alexsmobs:textures/entity/tarantula_hawk_nether_angry.png");
    private static final ResourceLocation TEXTURE_BABY = new ResourceLocation("alexsmobs:textures/entity/tarantula_hawk_baby.png");
    private static final ModelTarantulaHawk MODEL = new ModelTarantulaHawk();
    private static final ModelTarantulaHawkBaby MODEL_BABY = new ModelTarantulaHawkBaby();

    public RenderTarantulaHawk(RenderManager renderManagerIn) {
        super(renderManagerIn, MODEL, 0.5F);
    }

    @Override
    protected void preRenderCallback(EntityTarantulaHawk entitylivingbaseIn, float partialTickTime) {
        if (entitylivingbaseIn.isChild()) {
            this.mainModel = MODEL_BABY;
        } else {
            this.mainModel = MODEL;
            GlStateManager.scale(0.9F, 0.9F, 0.9F);
            float f = entitylivingbaseIn.prevDragProgress + (entitylivingbaseIn.dragProgress - entitylivingbaseIn.prevDragProgress) * partialTickTime;
            GlStateManager.rotate(f * 180.0F * 0.2F, 0.0F, 1.0F, 0.0F);
        }
        if (entitylivingbaseIn.isScared()) {
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        }
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityTarantulaHawk entity) {
        if (entity.isChild()) {
            return TEXTURE_BABY;
        }
        if (entity.isNether()) {
            return entity.isAngry() ? TEXTURE_NETHER_ANGRY : TEXTURE_NETHER;
        }
        return entity.isAngry() ? TEXTURE_ANGRY : TEXTURE;
    }
}
