package com.github.alexthe666.alexsmobs.client.render;

import com.github.alexthe666.alexsmobs.entity.EntitySharkToothArrow;
import net.minecraft.client.renderer.entity.RenderArrow;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;

public class RenderSharkToothArrow extends RenderArrow<EntitySharkToothArrow> {

    private static final ResourceLocation TEXTURE = new ResourceLocation("alexsmobs:textures/entity/shark_tooth_arrow.png");

    public RenderSharkToothArrow(RenderManager renderManagerIn) {
        super(renderManagerIn);
    }

    @Override
    protected ResourceLocation getEntityTexture(EntitySharkToothArrow entity) {
        return TEXTURE;
    }
}
