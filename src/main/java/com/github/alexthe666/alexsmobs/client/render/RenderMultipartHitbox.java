package com.github.alexthe666.alexsmobs.client.render;

import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Collision-only multipart segments are invisible server-side; this empty renderer prevents
 * Forge from drawing default entity hitbox cubes on the client.
 */
@SideOnly(Side.CLIENT)
public class RenderMultipartHitbox extends Render<Entity> {

    public RenderMultipartHitbox(RenderManager renderManagerIn) {
        super(renderManagerIn);
    }

    @Override
    public void doRender(Entity entity, double x, double y, double z, float entityYaw, float partialTicks) {
    }

    @Override
    protected ResourceLocation getEntityTexture(Entity entity) {
        return null;
    }
}
