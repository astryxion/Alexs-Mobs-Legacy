package com.github.alexthe666.alexsmobs.client.render;

import com.github.alexthe666.alexsmobs.client.model.ModelKomodoDragon;
import com.github.alexthe666.alexsmobs.entity.EntityKomodoDragon;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderKomodoDragon extends RenderLiving<EntityKomodoDragon> {
    private static final ResourceLocation TEXTURE = new ResourceLocation("alexsmobs:textures/entity/komodo_dragon.png");

    public RenderKomodoDragon(RenderManager renderManagerIn) {
        super(renderManagerIn, new ModelKomodoDragon(), 0.6F);
    }

    @Override
    protected void preRenderCallback(EntityKomodoDragon entitylivingbaseIn, float partialTickTime) {
        GlStateManager.scale(1.2F, 1.2F, 1.2F);
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityKomodoDragon entity) {
        return TEXTURE;
    }
}
