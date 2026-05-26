package com.github.alexthe666.alexsmobs.client.render;

import com.github.alexthe666.alexsmobs.client.model.ModelJerboa;
import com.github.alexthe666.alexsmobs.entity.EntityJerboa;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderJerboa extends RenderLiving<EntityJerboa> {
    private static final ResourceLocation TEXTURE = new ResourceLocation("alexsmobs:textures/entity/jerboa.png");
    private static final ModelJerboa MODEL = new ModelJerboa();

    public RenderJerboa(RenderManager renderManagerIn) {
        super(renderManagerIn, MODEL, 0.1F);
    }

    private static final ResourceLocation TEXTURE_SLEEPING = new ResourceLocation("alexsmobs:textures/entity/jerboa_sleeping.png");

    @Override
    protected ResourceLocation getEntityTexture(EntityJerboa entity) {
        return entity.isSleeping() ? TEXTURE_SLEEPING : TEXTURE;
    }
}
