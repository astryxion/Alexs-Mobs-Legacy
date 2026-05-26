package com.github.alexthe666.alexsmobs.client.render;

import com.github.alexthe666.alexsmobs.client.model.ModelBoneSerpentHead;
import com.github.alexthe666.alexsmobs.entity.EntityBoneSerpent;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderBoneSerpent extends RenderLiving<EntityBoneSerpent> {
    private static final ResourceLocation TEXTURE = new ResourceLocation("alexsmobs:textures/entity/bone_serpent_head.png");

    public RenderBoneSerpent(RenderManager renderManagerIn) {
        super(renderManagerIn, new ModelBoneSerpentHead(), 0.3F);
    }

    @Override
    protected void preRenderCallback(EntityBoneSerpent entitylivingbaseIn, float partialTickTime) {
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityBoneSerpent entity) {
        return TEXTURE;
    }
}
