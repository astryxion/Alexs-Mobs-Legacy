package com.github.alexthe666.alexsmobs.client.render;

import com.github.alexthe666.alexsmobs.client.model.ModelMimicube;
import com.github.alexthe666.alexsmobs.client.render.layer.LayerMimicubeHeldItem;
import com.github.alexthe666.alexsmobs.client.render.layer.LayerMimicubeHelmet;
import com.github.alexthe666.alexsmobs.client.render.layer.LayerMimicubeTexture;
import com.github.alexthe666.alexsmobs.entity.EntityMimicube;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderMimicube extends RenderLiving<EntityMimicube> {
    private static final ResourceLocation TEXTURE = new ResourceLocation("alexsmobs:textures/entity/mimicube.png");

    public RenderMimicube(RenderManager renderManagerIn) {
        super(renderManagerIn, new ModelMimicube(), 0.5F);
        this.addLayer(new LayerMimicubeHelmet(this));
        this.addLayer(new LayerMimicubeHeldItem(this));
        this.addLayer(new LayerMimicubeTexture(this));
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityMimicube entity) {
        return TEXTURE;
    }
}
