package com.github.alexthe666.alexsmobs.client.render;

import com.github.alexthe666.alexsmobs.client.model.ModelBlobfish;
import com.github.alexthe666.alexsmobs.client.model.ModelBlobfishDepressurized;
import com.github.alexthe666.alexsmobs.entity.EntityBlobfish;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderBlobfish extends RenderLiving<EntityBlobfish> {
    private static final ResourceLocation TEXTURE = new ResourceLocation("alexsmobs:textures/entity/blobfish.png");
    private static final ResourceLocation TEXTURE_DEPRESSURIZED = new ResourceLocation("alexsmobs:textures/entity/blobfish_depressurized.png");
    private final ModelBlobfish modelNormal = new ModelBlobfish();
    private final ModelBlobfishDepressurized modelDepressurized = new ModelBlobfishDepressurized();

    public RenderBlobfish(RenderManager renderManagerIn) {
        super(renderManagerIn, new ModelBlobfish(), 0.35F);
    }

    @Override
    protected void preRenderCallback(EntityBlobfish entity, float partialTickTime) {
        if (entity.isDepressurized()) {
            this.mainModel = this.modelDepressurized;
        } else {
            this.mainModel = this.modelNormal;
        }
        float sc = entity.getBlobfishScale();
        GlStateManager.scale(sc, sc, sc);
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityBlobfish entity) {
        return entity.isDepressurized() ? TEXTURE_DEPRESSURIZED : TEXTURE;
    }

    @Override
    public void doRender(EntityBlobfish entity, double x, double y, double z, float entityYaw, float partialTicks) {
        float squish = entity.prevSquishFactor + (entity.squishFactor - entity.prevSquishFactor) * partialTicks;
        if (!entity.isDepressurized()) {
            y += (double) (0.15F * squish);
        }
        super.doRender(entity, x, y, z, entityYaw, partialTicks);
    }
}
