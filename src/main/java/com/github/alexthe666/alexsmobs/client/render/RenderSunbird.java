package com.github.alexthe666.alexsmobs.client.render;

import com.github.alexthe666.alexsmobs.client.model.ModelSunbird;
import com.github.alexthe666.alexsmobs.entity.EntitySunbird;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderSunbird extends RenderLiving<EntitySunbird> {
    private static final ResourceLocation TEXTURE = new ResourceLocation("alexsmobs:textures/entity/sunbird.png");

    public RenderSunbird(RenderManager renderManagerIn) {
        super(renderManagerIn, new ModelSunbird(), 0.5F);
    }

    @Override
    protected void preRenderCallback(EntitySunbird entitylivingbaseIn, float partialTickTime) {
        GlStateManager.scale(1.25F, 1.25F, 1.25F);
    }

    @Override
    public void doRender(EntitySunbird entity, double x, double y, double z, float entityYaw, float partialTicks) {
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0F, 240.0F);
        super.doRender(entity, x, y, z, entityYaw, partialTicks);
    }

    @Override
    protected ResourceLocation getEntityTexture(EntitySunbird entity) {
        return TEXTURE;
    }
}
