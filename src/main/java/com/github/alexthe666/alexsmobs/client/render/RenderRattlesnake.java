package com.github.alexthe666.alexsmobs.client.render;

import com.github.alexthe666.alexsmobs.client.model.ModelRattlesnake;
import com.github.alexthe666.alexsmobs.entity.EntityRattlesnake;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderRattlesnake extends RenderLiving<EntityRattlesnake> {
    private static final ResourceLocation TEXTURE = new ResourceLocation("alexsmobs:textures/entity/rattlesnake.png");

    public RenderRattlesnake(RenderManager renderManagerIn) {
        super(renderManagerIn, new ModelRattlesnake(), 0.2F);
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityRattlesnake entity) {
        return TEXTURE;
    }
}
