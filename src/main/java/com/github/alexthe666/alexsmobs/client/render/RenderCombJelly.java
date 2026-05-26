package com.github.alexthe666.alexsmobs.client.render;

import com.github.alexthe666.alexsmobs.client.model.ModelCombJelly;
import com.github.alexthe666.alexsmobs.client.render.AMRenderTypes;
import com.github.alexthe666.alexsmobs.client.render.layer.LayerCombJellyRainbow;
import com.github.alexthe666.alexsmobs.entity.EntityCombJelly;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderCombJelly extends RenderLiving<EntityCombJelly> {
    private static final ResourceLocation TEXTURE_0 = new ResourceLocation("alexsmobs:textures/entity/comb_jelly_blue.png");
    private static final ResourceLocation TEXTURE_1 = new ResourceLocation("alexsmobs:textures/entity/comb_jelly_green.png");
    private static final ResourceLocation TEXTURE_2 = new ResourceLocation("alexsmobs:textures/entity/comb_jelly_red.png");
    private static final ModelCombJelly MODEL = new ModelCombJelly(0.0F);

    public RenderCombJelly(RenderManager renderManagerIn) {
        super(renderManagerIn, MODEL, 0.3F);
        this.addLayer(new LayerCombJellyRainbow(this));
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityCombJelly entity) {
        int variant = entity.getVariant();
        if (variant == 1) {
            return TEXTURE_1;
        }
        if (variant == 2) {
            return TEXTURE_2;
        }
        return TEXTURE_0;
    }

    @Override
    protected void renderModel(EntityCombJelly entitylivingbaseIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor) {
        if (this.bindEntityTexture(entitylivingbaseIn)) {
            AMRenderTypes.beginEntityTranslucent();
            this.mainModel.render(entitylivingbaseIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor);
            AMRenderTypes.endEntityTranslucent();
        }
    }

    @Override
    protected void preRenderCallback(EntityCombJelly entity, float partialTickTime) {
        float scale = entity.getJellyScale();
        GlStateManager.scale(scale, scale, scale);
    }

    @Override
    protected float getDeathMaxRotation(EntityCombJelly entityLivingBaseIn) {
        return 0.0F;
    }
}
