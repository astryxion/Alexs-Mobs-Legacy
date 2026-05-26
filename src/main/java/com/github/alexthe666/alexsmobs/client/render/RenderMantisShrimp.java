package com.github.alexthe666.alexsmobs.client.render;

import com.github.alexthe666.alexsmobs.client.model.ModelMantisShrimp;
import com.github.alexthe666.alexsmobs.client.render.layer.LayerMantisShrimpItem;
import com.github.alexthe666.alexsmobs.entity.EntityMantisShrimp;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderMantisShrimp extends RenderLiving<EntityMantisShrimp> {
    private static final ResourceLocation TEXTURE_0 = new ResourceLocation("alexsmobs:textures/entity/mantis_shrimp_0.png");
    private static final ResourceLocation TEXTURE_1 = new ResourceLocation("alexsmobs:textures/entity/mantis_shrimp_1.png");
    private static final ResourceLocation TEXTURE_2 = new ResourceLocation("alexsmobs:textures/entity/mantis_shrimp_2.png");

    public RenderMantisShrimp(RenderManager renderManagerIn) {
        super(renderManagerIn, new ModelMantisShrimp(), 0.6F);
        this.addLayer(new LayerMantisShrimpItem(this));
    }

    @Override
    protected void preRenderCallback(EntityMantisShrimp entitylivingbaseIn, float partialTickTime) {
        GlStateManager.scale(0.8F, 0.8F, 0.8F);
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityMantisShrimp entity) {
        if (entity.getVariant() == 2) {
            return TEXTURE_2;
        }
        return entity.getVariant() == 1 ? TEXTURE_1 : TEXTURE_0;
    }
}
