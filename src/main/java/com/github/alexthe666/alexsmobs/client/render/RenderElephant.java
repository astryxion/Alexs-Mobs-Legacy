package com.github.alexthe666.alexsmobs.client.render;

import com.github.alexthe666.alexsmobs.client.model.ModelElephant;
import com.github.alexthe666.alexsmobs.client.render.layer.LayerElephantItem;
import com.github.alexthe666.alexsmobs.client.render.layer.LayerElephantOverlays;
import com.github.alexthe666.alexsmobs.entity.EntityElephant;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderElephant extends RenderLiving<EntityElephant> {
    private static final ResourceLocation TEXTURE_TUSK = new ResourceLocation("alexsmobs:textures/entity/elephant/elephant_tusks.png");
    private static final ResourceLocation TEXTURE = new ResourceLocation("alexsmobs:textures/entity/elephant/elephant.png");

    public RenderElephant(RenderManager renderManagerIn) {
        super(renderManagerIn, new ModelElephant(0.0F), 0.8F);
        this.addLayer(new LayerElephantOverlays(this));
        this.addLayer(new LayerElephantItem(this));
    }

    @Override
    protected void preRenderCallback(EntityElephant entitylivingbaseIn, float partialTickTime) {
        if (entitylivingbaseIn.isTusked()) {
            GlStateManager.scale(1.1F, 1.1F, 1.1F);
        }
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityElephant entity) {
        return entity.isTusked() && !entity.isChild() ? TEXTURE_TUSK : TEXTURE;
    }
}
