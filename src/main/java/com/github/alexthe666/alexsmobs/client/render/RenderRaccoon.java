package com.github.alexthe666.alexsmobs.client.render;

import com.github.alexthe666.alexsmobs.client.model.ModelRaccoon;
import com.github.alexthe666.alexsmobs.client.render.layer.LayerRaccoonBandana;
import com.github.alexthe666.alexsmobs.client.render.layer.LayerRaccoonEyes;
import com.github.alexthe666.alexsmobs.client.render.layer.LayerRaccoonItem;
import com.github.alexthe666.alexsmobs.entity.EntityRaccoon;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderRaccoon extends RenderLiving<EntityRaccoon> {
    private static final ResourceLocation TEXTURE = new ResourceLocation("alexsmobs:textures/entity/raccoon.png");

    public RenderRaccoon(RenderManager renderManagerIn) {
        super(renderManagerIn, new ModelRaccoon(), 0.4F);
        this.addLayer(new LayerRaccoonEyes(this));
        this.addLayer(new LayerRaccoonItem(this));
        this.addLayer(new LayerRaccoonBandana(this));
    }

    @Override
    protected void preRenderCallback(EntityRaccoon entitylivingbaseIn, float partialTickTime) {
        GlStateManager.scale(0.75F, 0.75F, 0.75F);
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityRaccoon entity) {
        return TEXTURE;
    }
}
