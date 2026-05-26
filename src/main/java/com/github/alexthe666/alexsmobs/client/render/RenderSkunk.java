package com.github.alexthe666.alexsmobs.client.render;

import com.github.alexthe666.alexsmobs.client.model.ModelSkunk;
import com.github.alexthe666.alexsmobs.entity.EntitySkunk;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderSkunk extends RenderLiving<EntitySkunk> {
    private static final ResourceLocation TEXTURE = new ResourceLocation("alexsmobs:textures/entity/skunk.png");
    private static final ModelSkunk MODEL = new ModelSkunk();

    public RenderSkunk(RenderManager renderManagerIn) {
        super(renderManagerIn, MODEL, 0.45F);
    }

    @Override
    protected ResourceLocation getEntityTexture(EntitySkunk entity) {
        return TEXTURE;
    }
}
