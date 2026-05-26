package com.github.alexthe666.alexsmobs.client.render;

import com.github.alexthe666.alexsmobs.client.model.ModelGrizzlyBear;
import com.github.alexthe666.alexsmobs.client.render.layer.LayerGrizzlyHoney;
import com.github.alexthe666.alexsmobs.client.render.layer.LayerGrizzlyItem;
import com.github.alexthe666.alexsmobs.client.render.layer.LayerGrizzlySnow;
import com.github.alexthe666.alexsmobs.entity.EntityGrizzlyBear;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderGrizzlyBear extends RenderLiving<EntityGrizzlyBear> {
    private static final ResourceLocation TEXTURE = new ResourceLocation("alexsmobs:textures/entity/grizzly_bear.png");

    public RenderGrizzlyBear(RenderManager renderManagerIn) {
        super(renderManagerIn, new ModelGrizzlyBear(), 0.8F);
        this.addLayer(new LayerGrizzlyHoney(this));
        this.addLayer(new LayerGrizzlySnow(this));
        this.addLayer(new LayerGrizzlyItem(this));
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityGrizzlyBear entity) {
        return TEXTURE;
    }
}
