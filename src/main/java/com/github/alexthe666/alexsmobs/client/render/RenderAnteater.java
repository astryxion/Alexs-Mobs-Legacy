package com.github.alexthe666.alexsmobs.client.render;

import com.github.alexthe666.alexsmobs.client.model.ModelAnteater;
import com.github.alexthe666.alexsmobs.client.render.layer.LayerAnteaterBaby;
import com.github.alexthe666.alexsmobs.client.render.layer.LayerAnteaterTongueItem;
import com.github.alexthe666.alexsmobs.entity.EntityAnteater;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderAnteater extends RenderLiving<EntityAnteater> {
    private static final ResourceLocation TEXTURE = new ResourceLocation("alexsmobs:textures/entity/anteater.png");
    private static final ResourceLocation TEXTURE_PETER = new ResourceLocation("alexsmobs:textures/entity/anteater_peter.png");

    public RenderAnteater(RenderManager renderManagerIn) {
        super(renderManagerIn, new ModelAnteater(), 0.45F);
        this.addLayer(new LayerAnteaterTongueItem(this));
        this.addLayer(new LayerAnteaterBaby(this));
    }

    @Override
    protected boolean canRenderName(EntityAnteater entity) {
        if (entity.isChild() && entity.isRiding() && entity.getRidingEntity() instanceof EntityAnteater) {
            return false;
        }
        return super.canRenderName(entity);
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityAnteater entity) {
        return entity.isPeter() ? TEXTURE_PETER : TEXTURE;
    }
}
