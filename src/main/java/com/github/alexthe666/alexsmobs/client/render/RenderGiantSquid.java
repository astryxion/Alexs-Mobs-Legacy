package com.github.alexthe666.alexsmobs.client.render;

import com.github.alexthe666.alexsmobs.client.model.ModelGiantSquid;
import com.github.alexthe666.alexsmobs.client.render.layer.LayerGiantSquidDepressurization;
import com.github.alexthe666.alexsmobs.entity.EntityGiantSquid;
import com.github.alexthe666.alexsmobs.entity.EntityGiantSquidPart;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderGiantSquid extends RenderLiving<EntityGiantSquid> {
    private static final ResourceLocation TEXTURE = new ResourceLocation("alexsmobs:textures/entity/giant_squid.png");
    private static final ResourceLocation TEXTURE_BLUE = new ResourceLocation("alexsmobs:textures/entity/giant_squid_blue.png");

    public RenderGiantSquid(RenderManager renderManagerIn) {
        super(renderManagerIn, new ModelGiantSquid(), 1F);
        this.addLayer(new LayerGiantSquidDepressurization(this));
    }

    @Override
    protected float getDeathMaxRotation(EntityGiantSquid squid) {
        return 0.0F;
    }

    @Override
    public boolean shouldRender(EntityGiantSquid livingEntityIn, net.minecraft.client.renderer.culling.ICamera camera, double camX, double camY, double camZ) {
        if (livingEntityIn.isCaptured() && livingEntityIn.isEntityAlive()) {
            return false;
        }
        if (super.shouldRender(livingEntityIn, camera, camX, camY, camZ)) {
            return true;
        }
        if (camera == null) {
            return false;
        }
        for (EntityGiantSquidPart part : livingEntityIn.allParts) {
            if (part != null && part.getEntityBoundingBox() != null && camera.isBoundingBoxInFrustum(part.getEntityBoundingBox())) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityGiantSquid entity) {
        return entity.isBlue() ? TEXTURE_BLUE : TEXTURE;
    }
}
