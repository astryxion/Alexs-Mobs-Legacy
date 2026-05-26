package com.github.alexthe666.alexsmobs.client.render;

import com.github.alexthe666.alexsmobs.client.model.ModelWarpedToad;
import com.github.alexthe666.alexsmobs.client.render.layer.LayerWarpedToadGlow;
import com.github.alexthe666.alexsmobs.entity.EntityWarpedToad;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderWarpedToad extends RenderLiving<EntityWarpedToad> {
    private static final ResourceLocation TEXTURE = new ResourceLocation("alexsmobs:textures/entity/warped_toad.png");
    private static final ResourceLocation TEXTURE_BLINKING = new ResourceLocation("alexsmobs:textures/entity/warped_toad_blink.png");
    private static final ResourceLocation TEXTURE_PEPE = new ResourceLocation("alexsmobs:textures/entity/warped_toad_pepe.png");
    private static final ResourceLocation TEXTURE_PEPE_BLINKING = new ResourceLocation("alexsmobs:textures/entity/warped_toad_pepe_blink.png");

    public RenderWarpedToad(RenderManager renderManagerIn) {
        super(renderManagerIn, new ModelWarpedToad(), 0.85F);
        this.addLayer(new LayerWarpedToadGlow(this));
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityWarpedToad entity) {
        if (entity.isBased()) {
            return entity.isBlinking() ? TEXTURE_PEPE_BLINKING : TEXTURE_PEPE;
        } else {
            return entity.isBlinking() ? TEXTURE_BLINKING : TEXTURE;
        }
    }

    @Override
    protected void preRenderCallback(EntityWarpedToad entitylivingbaseIn, float partialTickTime) {
        GlStateManager.scale(1.25F, 1.25F, 1.25F);
    }
}
