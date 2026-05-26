package com.github.alexthe666.alexsmobs.client.render;

import com.github.alexthe666.alexsmobs.client.model.ModelDevilsHolePupfish;
import com.github.alexthe666.alexsmobs.entity.EntityDevilsHolePupfish;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderDevilsHolePupfish extends RenderLiving<EntityDevilsHolePupfish> {
    private static final ResourceLocation TEXTURE = new ResourceLocation("alexsmobs:textures/entity/devils_hole_pupfish.png");
    private static final ModelDevilsHolePupfish MODEL = new ModelDevilsHolePupfish();

    public RenderDevilsHolePupfish(RenderManager renderManagerIn) {
        super(renderManagerIn, MODEL, 0.2F);
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityDevilsHolePupfish entity) {
        return TEXTURE;
    }

    @Override
    protected void preRenderCallback(EntityDevilsHolePupfish entity, float partialTickTime) {
        float scale = entity.getPupfishScale();
        if (entity.isBaby()) {
            scale *= 0.65F;
        }
        GlStateManager.scale(scale, scale, scale);
    }
}
