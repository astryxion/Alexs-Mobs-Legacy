package com.github.alexthe666.alexsmobs.client.render;

import com.github.alexthe666.alexsmobs.client.model.ModelSugarGlider;
import com.github.alexthe666.alexsmobs.entity.EntitySugarGlider;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderSugarGlider extends RenderLiving<EntitySugarGlider> {
    private static final ResourceLocation TEXTURE = new ResourceLocation("alexsmobs:textures/entity/sugar_glider.png");

    public RenderSugarGlider(RenderManager renderManagerIn) {
        super(renderManagerIn, new ModelSugarGlider(), 0.35F);
    }

    @Override
    protected ResourceLocation getEntityTexture(EntitySugarGlider entity) {
        return TEXTURE;
    }
}
