package com.github.alexthe666.alexsmobs.client.render;

import com.github.alexthe666.alexsmobs.client.model.ModelTasmanianDevil;
import com.github.alexthe666.alexsmobs.client.render.layer.LayerTasmanianDevilEyes;
import com.github.alexthe666.alexsmobs.entity.EntityTasmanianDevil;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderTasmanianDevil extends RenderLiving<EntityTasmanianDevil> {
    private static final ResourceLocation TEXTURE = new ResourceLocation("alexsmobs:textures/entity/tasmanian_devil.png");

    public RenderTasmanianDevil(RenderManager renderManagerIn) {
        super(renderManagerIn, new ModelTasmanianDevil(), 0.3F);
        this.addLayer(new LayerTasmanianDevilEyes(this));
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityTasmanianDevil entity) {
        return TEXTURE;
    }
}
