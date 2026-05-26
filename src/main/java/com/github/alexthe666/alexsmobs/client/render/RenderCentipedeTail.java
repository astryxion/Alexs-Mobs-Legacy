package com.github.alexthe666.alexsmobs.client.render;

import com.github.alexthe666.alexsmobs.client.model.ModelCentipedeTail;
import com.github.alexthe666.alexsmobs.entity.EntityCentipedeTail;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderCentipedeTail extends RenderLiving<EntityCentipedeTail> {
    private static final ResourceLocation TEXTURE = new ResourceLocation("alexsmobs:textures/entity/centipede_tail.png");

    public RenderCentipedeTail(RenderManager renderManagerIn) {
        super(renderManagerIn, new ModelCentipedeTail(), 0.5F);
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityCentipedeTail entity) {
        return TEXTURE;
    }
}
