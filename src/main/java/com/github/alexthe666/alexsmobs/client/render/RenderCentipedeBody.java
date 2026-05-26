package com.github.alexthe666.alexsmobs.client.render;

import com.github.alexthe666.alexsmobs.client.model.ModelCentipedeBody;
import com.github.alexthe666.alexsmobs.entity.EntityCentipedeBody;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderCentipedeBody extends RenderLiving<EntityCentipedeBody> {
    private static final ResourceLocation TEXTURE = new ResourceLocation("alexsmobs:textures/entity/centipede_body.png");

    public RenderCentipedeBody(RenderManager renderManagerIn) {
        super(renderManagerIn, new ModelCentipedeBody(), 0.5F);
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityCentipedeBody entity) {
        return TEXTURE;
    }
}
