package com.github.alexthe666.alexsmobs.client.render;

import com.github.alexthe666.alexsmobs.client.model.ModelCrimsonMosquito;
import com.github.alexthe666.alexsmobs.client.render.layer.LayerCrimsonMosquitoBlood;
import com.github.alexthe666.alexsmobs.entity.EntityCrimsonMosquito;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderCrimsonMosquito extends RenderLiving<EntityCrimsonMosquito> {
    private static final ResourceLocation TEXTURE = new ResourceLocation("alexsmobs:textures/entity/crimson_mosquito.png");
    private static final ResourceLocation TEXTURE_SICK = new ResourceLocation("alexsmobs:textures/entity/crimson_mosquito_blue.png");
    private static final ResourceLocation TEXTURE_FLY = new ResourceLocation("alexsmobs:textures/entity/crimson_mosquito_fly.png");
    private static final ResourceLocation TEXTURE_SICK_FLY = new ResourceLocation("alexsmobs:textures/entity/crimson_mosquito_fly_blue.png");

    public RenderCrimsonMosquito(RenderManager renderManagerIn) {
        super(renderManagerIn, new ModelCrimsonMosquito(), 0.6F);
        this.addLayer(new LayerCrimsonMosquitoBlood(this));
    }

    @Override
    protected void preRenderCallback(EntityCrimsonMosquito entitylivingbaseIn, float partialTickTime) {
        float mosScale = entitylivingbaseIn.prevMosquitoScale + (entitylivingbaseIn.getMosquitoScale() - entitylivingbaseIn.prevMosquitoScale) * partialTickTime;
        GlStateManager.scale(mosScale * 1.2F, mosScale * 1.2F, mosScale * 1.2F);
    }

    @Override
    protected void applyRotations(EntityCrimsonMosquito entityLiving, float ageInTicks, float rotationYaw, float partialTicks) {
        if (entityLiving.isSick()) {
            rotationYaw += (float) (Math.cos(entityLiving.ticksExisted * 7F) * Math.PI * 0.9F);
            float vibrate = 0.05F * entityLiving.getMosquitoScale();
            GlStateManager.translate((entityLiving.getRNG().nextFloat() - 0.5F) * vibrate, (entityLiving.getRNG().nextFloat() - 0.5F) * vibrate, (entityLiving.getRNG().nextFloat() - 0.5F) * vibrate);
        }
        super.applyRotations(entityLiving, ageInTicks, rotationYaw, partialTicks);
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityCrimsonMosquito entity) {
        if (entity.isSick()) {
            return entity.isFromFly() ? TEXTURE_SICK_FLY : TEXTURE_SICK;
        }
        return entity.isFromFly() ? TEXTURE_FLY : TEXTURE;
    }
}
