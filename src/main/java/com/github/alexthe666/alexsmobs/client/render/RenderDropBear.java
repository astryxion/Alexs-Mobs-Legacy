package com.github.alexthe666.alexsmobs.client.render;

import com.github.alexthe666.alexsmobs.client.model.ModelDropBear;
import com.github.alexthe666.alexsmobs.client.render.layer.LayerDropBearEyes;
import com.github.alexthe666.alexsmobs.entity.EntityDropBear;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderDropBear extends RenderLiving<EntityDropBear> {
    private static final ResourceLocation TEXTURE = new ResourceLocation("alexsmobs:textures/entity/dropbear.png");

    public RenderDropBear(RenderManager renderManagerIn) {
        super(renderManagerIn, new ModelDropBear(), 0.7F);
        this.addLayer(new LayerDropBearEyes(this));
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityDropBear entity) {
        return TEXTURE;
    }
}
