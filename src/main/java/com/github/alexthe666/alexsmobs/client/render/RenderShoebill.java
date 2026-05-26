package com.github.alexthe666.alexsmobs.client.render;

import com.github.alexthe666.alexsmobs.client.model.ModelShoebill;
import com.github.alexthe666.alexsmobs.entity.EntityShoebill;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderShoebill extends RenderLiving<EntityShoebill> {
    private static final ResourceLocation TEXTURE = new ResourceLocation("alexsmobs:textures/entity/shoebill.png");

    public RenderShoebill(RenderManager renderManagerIn) {
        super(renderManagerIn, new ModelShoebill(), 0.3F);
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityShoebill entity) {
        return TEXTURE;
    }
}
