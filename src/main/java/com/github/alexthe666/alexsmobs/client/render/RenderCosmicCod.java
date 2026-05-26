package com.github.alexthe666.alexsmobs.client.render;

import com.github.alexthe666.alexsmobs.client.model.ModelCosmicCod;
import com.github.alexthe666.alexsmobs.client.render.layer.LayerBasicGlow;
import com.github.alexthe666.alexsmobs.entity.EntityCosmicCod;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderCosmicCod extends RenderLiving<EntityCosmicCod> {
    private static final ResourceLocation TEXTURE = new ResourceLocation("alexsmobs:textures/entity/cosmic_cod.png");
    private static final ResourceLocation TEXTURE_EYES = new ResourceLocation("alexsmobs:textures/entity/cosmic_cod_eyes.png");
    private static final ModelCosmicCod MODEL = new ModelCosmicCod();

    public RenderCosmicCod(RenderManager renderManagerIn) {
        super(renderManagerIn, MODEL, 0.25F);
        this.addLayer(new LayerBasicGlow<>(this, TEXTURE_EYES));
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityCosmicCod entity) {
        return TEXTURE;
    }
}
