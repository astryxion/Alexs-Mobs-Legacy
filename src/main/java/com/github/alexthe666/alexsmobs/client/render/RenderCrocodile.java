package com.github.alexthe666.alexsmobs.client.render;

import com.github.alexthe666.alexsmobs.client.model.ModelCrocodile;
import com.github.alexthe666.alexsmobs.entity.EntityCrocodile;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderCrocodile extends RenderLiving<EntityCrocodile> {
    private static final ResourceLocation TEXTURE_0 = new ResourceLocation("alexsmobs:textures/entity/crocodile_0.png");
    private static final ResourceLocation TEXTURE_1 = new ResourceLocation("alexsmobs:textures/entity/crocodile_1.png");

    public RenderCrocodile(RenderManager renderManagerIn) {
        super(renderManagerIn, new ModelCrocodile(), 0.8F);
    }

    @Override
    protected void preRenderCallback(EntityCrocodile entity, float partialTickTime) {
        if (entity.isChild()) {
            GlStateManager.scale(0.3F, 0.3F, 0.3F);
            GlStateManager.translate(0.0F, 7.0F, 0.125F);
        } else {
            GlStateManager.scale(0.9F, 0.9F, 0.9F);
        }
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityCrocodile entity) {
        return entity.isDesert() ? TEXTURE_1 : TEXTURE_0;
    }
}
