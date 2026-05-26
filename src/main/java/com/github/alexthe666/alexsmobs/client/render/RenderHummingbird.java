package com.github.alexthe666.alexsmobs.client.render;

import com.github.alexthe666.alexsmobs.client.model.ModelHummingbird;
import com.github.alexthe666.alexsmobs.entity.EntityHummingbird;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderHummingbird extends RenderLiving<EntityHummingbird> {
    private static final ResourceLocation TEXTURE_0 = new ResourceLocation("alexsmobs:textures/entity/hummingbird_0.png");
    private static final ResourceLocation TEXTURE_1 = new ResourceLocation("alexsmobs:textures/entity/hummingbird_1.png");
    private static final ResourceLocation TEXTURE_2 = new ResourceLocation("alexsmobs:textures/entity/hummingbird_2.png");

    public RenderHummingbird(RenderManager renderManagerIn) {
        super(renderManagerIn, new ModelHummingbird(), 0.15F);
    }

    @Override
    protected void preRenderCallback(EntityHummingbird entitylivingbaseIn, float partialTickTime) {
        GlStateManager.scale(0.75F, 0.75F, 0.75F);
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityHummingbird entity) {
        if (entity.getVariant() == 0) {
            return TEXTURE_0;
        }
        return entity.getVariant() == 1 ? TEXTURE_1 : TEXTURE_2;
    }
}
