package com.github.alexthe666.alexsmobs.client.render;

import com.github.alexthe666.alexsmobs.client.model.ModelCatfishLarge;
import com.github.alexthe666.alexsmobs.client.model.ModelCatfishMedium;
import com.github.alexthe666.alexsmobs.client.model.ModelCatfishSmall;
import com.github.alexthe666.alexsmobs.entity.EntityCatfish;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderCatfish extends RenderLiving<EntityCatfish> {
    private static final ResourceLocation TEXTURE = new ResourceLocation("alexsmobs:textures/entity/catfish_small.png");
    private static final ResourceLocation TEXTURE_MEDIUM = new ResourceLocation("alexsmobs:textures/entity/catfish_medium.png");
    private static final ResourceLocation TEXTURE_LARGE = new ResourceLocation("alexsmobs:textures/entity/catfish_large.png");
    private static final ResourceLocation TEXTURE_SPIT = new ResourceLocation("alexsmobs:textures/entity/catfish_small_spit.png");
    private static final ResourceLocation TEXTURE_SPIT_MEDIUM = new ResourceLocation("alexsmobs:textures/entity/catfish_medium_spit.png");
    private static final ResourceLocation TEXTURE_SPIT_LARGE = new ResourceLocation("alexsmobs:textures/entity/catfish_large_spit.png");
    private final ModelCatfishSmall modelSmall;
    private final ModelCatfishMedium modelMedium;
    private final ModelCatfishLarge modelLarge;

    public RenderCatfish(RenderManager renderManagerIn) {
        super(renderManagerIn, new ModelCatfishSmall(), 0.5F);
        this.modelSmall = (ModelCatfishSmall) this.mainModel;
        this.modelMedium = new ModelCatfishMedium();
        this.modelLarge = new ModelCatfishLarge();
    }

    @Override
    protected void preRenderCallback(EntityCatfish entity, float partialTickTime) {
        if (entity.getCatfishSize() == 2) {
            this.mainModel = modelLarge;
        } else if (entity.getCatfishSize() == 1) {
            this.mainModel = modelMedium;
        } else {
            this.mainModel = modelSmall;
        }
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityCatfish entity) {
        if (entity.getCatfishSize() == 2) {
            return entity.isSpitting() ? TEXTURE_SPIT_LARGE : TEXTURE_LARGE;
        }
        if (entity.getCatfishSize() == 1) {
            return entity.isSpitting() ? TEXTURE_SPIT_MEDIUM : TEXTURE_MEDIUM;
        }
        return entity.isSpitting() ? TEXTURE_SPIT : TEXTURE;
    }

    @Override
    protected float getDeathMaxRotation(EntityCatfish entityLivingBaseIn) {
        return super.getDeathMaxRotation(entityLivingBaseIn);
    }
}
