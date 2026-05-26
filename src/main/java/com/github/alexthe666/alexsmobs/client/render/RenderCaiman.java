package com.github.alexthe666.alexsmobs.client.render;

import com.github.alexthe666.alexsmobs.client.model.ModelCaiman;
import com.github.alexthe666.alexsmobs.entity.EntityCaiman;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderCaiman extends RenderLiving<EntityCaiman> {
    private static final ResourceLocation TEXTURE = new ResourceLocation("alexsmobs:textures/entity/caiman.png");

    public RenderCaiman(RenderManager renderManagerIn) {
        super(renderManagerIn, new ModelCaiman(), 0.4F);
    }

    @Override
    protected void preRenderCallback(EntityCaiman entity, float partialTickTime) {
        if (entity.isChild()) {
            GlStateManager.scale(0.5F, 0.5F, 0.5F);
        }
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityCaiman entity) {
        return TEXTURE;
    }
}
