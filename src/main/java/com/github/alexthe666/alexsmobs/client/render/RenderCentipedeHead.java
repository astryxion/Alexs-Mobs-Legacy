package com.github.alexthe666.alexsmobs.client.render;

import com.github.alexthe666.alexsmobs.client.model.ModelCentipedeHead;
import com.github.alexthe666.alexsmobs.client.render.layer.LayerCentipedeHeadEyes;
import com.github.alexthe666.alexsmobs.entity.EntityCentipedeHead;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderCentipedeHead extends RenderLiving<EntityCentipedeHead> {
    private static final ResourceLocation TEXTURE = new ResourceLocation("alexsmobs:textures/entity/centipede_head.png");

    public RenderCentipedeHead(RenderManager renderManagerIn) {
        super(renderManagerIn, new ModelCentipedeHead(), 0.5F);
        this.addLayer(new LayerCentipedeHeadEyes(this));
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityCentipedeHead entity) {
        return TEXTURE;
    }
}
