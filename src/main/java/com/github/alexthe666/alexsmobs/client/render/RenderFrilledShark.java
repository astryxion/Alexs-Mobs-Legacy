package com.github.alexthe666.alexsmobs.client.render;

import com.github.alexthe666.alexsmobs.client.model.ModelFrilledShark;
import com.github.alexthe666.alexsmobs.client.render.layer.LayerFrilledSharkTeeth;
import com.github.alexthe666.alexsmobs.entity.EntityFrilledShark;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderFrilledShark extends RenderLiving<EntityFrilledShark> {
    private static final ResourceLocation TEXTURE = new ResourceLocation("alexsmobs:textures/entity/frilled_shark.png");
    private static final ResourceLocation TEXTURE_DEPRESSURIZED = new ResourceLocation("alexsmobs:textures/entity/frilled_shark_depressurized.png");
    private static final ResourceLocation TEXTURE_KAIJU = new ResourceLocation("alexsmobs:textures/entity/frilled_shark_kaiju.png");
    private static final ResourceLocation TEXTURE_KAIJU_DEPRESSURIZED = new ResourceLocation("alexsmobs:textures/entity/frilled_shark_kaiju_depressurized.png");

    public RenderFrilledShark(RenderManager renderManagerIn) {
        super(renderManagerIn, new ModelFrilledShark(), 0.4F);
        this.addLayer(new LayerFrilledSharkTeeth(this));
    }

    @Override
    protected void preRenderCallback(EntityFrilledShark entitylivingbaseIn, float partialTickTime) {
        GlStateManager.scale(0.85F, 0.85F, 0.85F);
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityFrilledShark entity) {
        if (entity.isKaiju()) {
            return entity.isDepressurized() ? TEXTURE_KAIJU_DEPRESSURIZED : TEXTURE_KAIJU;
        }
        return entity.isDepressurized() ? TEXTURE_DEPRESSURIZED : TEXTURE;
    }
}
