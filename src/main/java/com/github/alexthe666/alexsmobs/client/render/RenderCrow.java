package com.github.alexthe666.alexsmobs.client.render;

import com.github.alexthe666.alexsmobs.client.model.ModelCrow;
import com.github.alexthe666.alexsmobs.client.render.layer.LayerCrowItem;
import com.github.alexthe666.alexsmobs.entity.EntityCrow;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderCrow extends RenderLiving<EntityCrow> {
    private static final ResourceLocation TEXTURE = new ResourceLocation("alexsmobs:textures/entity/crow.png");

    public RenderCrow(RenderManager renderManagerIn) {
        super(renderManagerIn, new ModelCrow(), 0.3F);
        this.addLayer(new LayerCrowItem(this));
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityCrow entity) {
        return TEXTURE;
    }
}
