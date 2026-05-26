package com.github.alexthe666.alexsmobs.client.render;

import com.github.alexthe666.alexsmobs.client.model.ModelFly;
import com.github.alexthe666.alexsmobs.entity.EntityFly;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderFly extends RenderLiving<EntityFly> {
    private static final ResourceLocation TEXTURE = new ResourceLocation("alexsmobs:textures/entity/fly.png");

    public RenderFly(RenderManager renderManagerIn) {
        super(renderManagerIn, new ModelFly(), 0.2F);
    }

    @Override
    protected void applyRotations(EntityFly entityLiving, float ageInTicks, float rotationYaw, float partialTicks) {
        if (entityLiving.isInNether()) {
            rotationYaw += (float) (Math.cos(entityLiving.ticksExisted * 7F) * Math.PI * 0.9F);
            float vibrate = 0.05F;
            GlStateManager.translate((entityLiving.getRNG().nextFloat() - 0.5F) * vibrate, (entityLiving.getRNG().nextFloat() - 0.5F) * vibrate, (entityLiving.getRNG().nextFloat() - 0.5F) * vibrate);
        }
        super.applyRotations(entityLiving, ageInTicks, rotationYaw, partialTicks);
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityFly entity) {
        return TEXTURE;
    }
}
