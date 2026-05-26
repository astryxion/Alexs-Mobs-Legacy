package com.github.alexthe666.alexsmobs.client.render;

import com.github.alexthe666.alexsmobs.client.model.ModelCachalotWhale;
import com.github.alexthe666.alexsmobs.client.render.layer.LayerCachalotWhaleCapturedSquid;
import com.github.alexthe666.alexsmobs.entity.EntityCachalotPart;
import com.github.alexthe666.alexsmobs.entity.EntityCachalotWhale;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderCachalotWhale extends RenderLiving<EntityCachalotWhale> {
    private static final ResourceLocation TEXTURE = new ResourceLocation("alexsmobs:textures/entity/cachalot/cachalot_whale.png");
    private static final ResourceLocation TEXTURE_SLEEPING = new ResourceLocation("alexsmobs:textures/entity/cachalot/cachalot_whale_sleeping.png");
    private static final ResourceLocation TEXTURE_ALBINO = new ResourceLocation("alexsmobs:textures/entity/cachalot/cachalot_whale_albino.png");
    private static final ResourceLocation TEXTURE_ALBINO_SLEEPING = new ResourceLocation("alexsmobs:textures/entity/cachalot/cachalot_whale_albino_sleeping.png");

    public RenderCachalotWhale(RenderManager renderManagerIn) {
        super(renderManagerIn, new ModelCachalotWhale(), 4.2F);
        this.addLayer(new LayerCachalotWhaleCapturedSquid(this));
    }

    @Override
    protected void preRenderCallback(EntityCachalotWhale entitylivingbaseIn, float partialTickTime) {
        ModelCachalotWhale model = (ModelCachalotWhale) this.mainModel;
        model.setChildHeadScale(entitylivingbaseIn.isChild());
        if (entitylivingbaseIn.isChild()) {
            GlStateManager.scale(0.5F, 0.5F, 0.5F);
            GlStateManager.translate(0.0F, 1.5F, 0.125F);
        }
    }

    @Override
    public boolean shouldRender(EntityCachalotWhale livingEntity, ICamera camera, double camX, double camY, double camZ) {
        if (super.shouldRender(livingEntity, camera, camX, camY, camZ)) {
            return true;
        }
        if (camera == null) {
            return false;
        }
        for (EntityCachalotPart part : livingEntity.whaleParts) {
            if (part != null && part.getEntityBoundingBox() != null && camera.isBoundingBoxInFrustum(part.getEntityBoundingBox())) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityCachalotWhale entity) {
        if (entity.isAlbino()) {
            return entity.isSleeping() || entity.isBeached() ? TEXTURE_ALBINO_SLEEPING : TEXTURE_ALBINO;
        }
        return entity.isSleeping() || entity.isBeached() ? TEXTURE_SLEEPING : TEXTURE;
    }
}
