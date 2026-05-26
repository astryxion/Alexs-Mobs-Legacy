package com.github.alexthe666.alexsmobs.client.render;

import com.github.alexthe666.alexsmobs.client.model.ModelRhinoceros;
import com.github.alexthe666.alexsmobs.client.render.layer.LayerRhinocerosPotion;
import com.github.alexthe666.alexsmobs.entity.EntityRhinoceros;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderRhinoceros extends RenderLiving<EntityRhinoceros> {
    private static final ResourceLocation TEXTURE = new ResourceLocation("alexsmobs:textures/entity/rhinoceros.png");
    private static final ResourceLocation TEXTURE_ANGRY = new ResourceLocation("alexsmobs:textures/entity/rhinoceros_angry.png");

    public RenderRhinoceros(RenderManager renderManagerIn) {
        super(renderManagerIn, new ModelRhinoceros(), 0.9F);
        this.addLayer(new LayerRhinocerosPotion(this));
    }

    @Override
    protected void preRenderCallback(EntityRhinoceros entity, float partialTickTime) {
        GlStateManager.scale(1.1F, 1.1F, 1.1F);
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityRhinoceros entity) {
        return entity.isAngry() ? TEXTURE_ANGRY : TEXTURE;
    }
}
