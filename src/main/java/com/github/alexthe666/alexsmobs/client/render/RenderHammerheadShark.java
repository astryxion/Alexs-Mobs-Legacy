package com.github.alexthe666.alexsmobs.client.render;

import com.github.alexthe666.alexsmobs.client.model.ModelHammerheadShark;
import com.github.alexthe666.alexsmobs.entity.EntityHammerheadShark;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderHammerheadShark extends RenderLiving<EntityHammerheadShark> {
    private static final ResourceLocation TEXTURE = new ResourceLocation("alexsmobs:textures/entity/hammerhead_shark.png");

    public RenderHammerheadShark(RenderManager renderManagerIn) {
        super(renderManagerIn, new ModelHammerheadShark(), 0.8F);
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityHammerheadShark entity) {
        return TEXTURE;
    }
}
