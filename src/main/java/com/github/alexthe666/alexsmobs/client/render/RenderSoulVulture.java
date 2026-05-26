package com.github.alexthe666.alexsmobs.client.render;

import com.github.alexthe666.alexsmobs.client.model.ModelSoulVulture;
import com.github.alexthe666.alexsmobs.client.render.layer.LayerSoulVultureGlow;
import com.github.alexthe666.alexsmobs.entity.EntitySoulVulture;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderSoulVulture extends RenderLiving<EntitySoulVulture> {
    private static final ResourceLocation TEXTURE = new ResourceLocation("alexsmobs:textures/entity/soul_vulture.png");

    public RenderSoulVulture(RenderManager renderManagerIn) {
        super(renderManagerIn, new ModelSoulVulture(), 0.3F);
        this.addLayer(new LayerSoulVultureGlow(this));
    }

    @Override
    protected ResourceLocation getEntityTexture(EntitySoulVulture entity) {
        return TEXTURE;
    }
}
