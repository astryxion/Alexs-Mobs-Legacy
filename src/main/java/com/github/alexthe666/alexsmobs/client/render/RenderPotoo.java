package com.github.alexthe666.alexsmobs.client.render;

import com.github.alexthe666.alexsmobs.client.model.ModelPotoo;
import com.github.alexthe666.alexsmobs.entity.EntityPotoo;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderPotoo extends RenderLiving<EntityPotoo> {
    private static final ResourceLocation TEXTURE = new ResourceLocation("alexsmobs:textures/entity/potoo.png");

    public RenderPotoo(RenderManager renderManagerIn) {
        super(renderManagerIn, new ModelPotoo(), 0.35F);
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityPotoo entity) {
        return TEXTURE;
    }
}
