package com.github.alexthe666.alexsmobs.client.render;

import com.github.alexthe666.alexsmobs.client.model.ModelRoadrunner;
import com.github.alexthe666.alexsmobs.entity.EntityRoadrunner;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderRoadrunner extends RenderLiving<EntityRoadrunner> {
    private static final ResourceLocation TEXTURE = new ResourceLocation("alexsmobs:textures/entity/roadrunner.png");

    public RenderRoadrunner(RenderManager renderManagerIn) {
        super(renderManagerIn, new ModelRoadrunner(), 0.3F);
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityRoadrunner entity) {
        return TEXTURE;
    }
}
