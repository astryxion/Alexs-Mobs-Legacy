package com.github.alexthe666.alexsmobs.client.render;

import com.github.alexthe666.alexsmobs.client.model.ModelStradpole;
import com.github.alexthe666.alexsmobs.entity.EntityStradpole;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderStradpole extends RenderLiving<EntityStradpole> {
    public static final ResourceLocation TEXTURE = new ResourceLocation("alexsmobs:textures/entity/stradpole.png");

    public RenderStradpole(RenderManager renderManagerIn) {
        super(renderManagerIn, new ModelStradpole(), 0.25F);
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityStradpole entity) {
        return TEXTURE;
    }
}
