package com.github.alexthe666.alexsmobs.client.render;

import com.github.alexthe666.alexsmobs.client.model.ModelEndergrade;
import com.github.alexthe666.alexsmobs.client.render.layer.LayerEndergradeSaddle;
import com.github.alexthe666.alexsmobs.entity.EntityEndergrade;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderEndergrade extends RenderLiving<EntityEndergrade> {
    private static final ResourceLocation TEXTURE = new ResourceLocation("alexsmobs:textures/entity/endergrade.png");

    public RenderEndergrade(RenderManager renderManagerIn) {
        super(renderManagerIn, new ModelEndergrade(), 0.6F);
        this.addLayer(new LayerEndergradeSaddle(this));
    }

    @Override
    protected void preRenderCallback(EntityEndergrade entitylivingbaseIn, float partialTickTime) {
        GlStateManager.scale(1.2F, 1.2F, 1.2F);
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityEndergrade entity) {
        return TEXTURE;
    }
}
