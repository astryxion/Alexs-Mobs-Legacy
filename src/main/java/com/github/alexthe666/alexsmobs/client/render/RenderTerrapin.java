package com.github.alexthe666.alexsmobs.client.render;

import com.github.alexthe666.alexsmobs.client.model.ModelTerrapin;
import com.github.alexthe666.alexsmobs.client.render.layer.LayerTerrapinOverlay;
import com.github.alexthe666.alexsmobs.entity.EntityTerrapin;
import com.github.alexthe666.alexsmobs.entity.util.TerrapinTypes;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderTerrapin extends RenderLiving<EntityTerrapin> {

    public RenderTerrapin(RenderManager renderManagerIn) {
        super(renderManagerIn, new ModelTerrapin(), 0.3F);
        this.addLayer(new LayerTerrapinOverlay(this, 0));
        this.addLayer(new LayerTerrapinOverlay(this, 1));
        this.addLayer(new LayerTerrapinOverlay(this, 2));
    }

    @Override
    protected void applyRotations(EntityTerrapin entityLiving, float ageInTicks, float rotationYaw, float partialTicks) {
        if (entityLiving.isSpinning()) {
            rotationYaw += MathHelper.cos((entityLiving.ticksExisted + partialTicks) * 3.25F) * 57.295776F * 0.4F;
        }
        super.applyRotations(entityLiving, ageInTicks, rotationYaw, partialTicks);
    }

    @Override
    protected void preRenderCallback(EntityTerrapin entity, float partialTickTime) {
        if (entity.isChild()) {
            GlStateManager.scale(0.5F, 0.5F, 0.5F);
        }
    }

    @Override
    public ResourceLocation getEntityTexture(EntityTerrapin entity) {
        if (entity.isKoopa()) {
            return TerrapinTypes.KOOPA.getTexture();
        }
        return entity.getTurtleType().getTexture();
    }
}
