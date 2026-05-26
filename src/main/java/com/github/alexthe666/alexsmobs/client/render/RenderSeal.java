package com.github.alexthe666.alexsmobs.client.render;

import com.github.alexthe666.alexsmobs.client.model.ModelSeal;
import com.github.alexthe666.alexsmobs.client.render.AMRenderTypes;
import com.github.alexthe666.alexsmobs.client.render.layer.LayerSealItem;
import com.github.alexthe666.alexsmobs.entity.EntitySeal;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderSeal extends RenderLiving<EntitySeal> {
    private static final ResourceLocation TEXTURE_BROWN_0 = new ResourceLocation("alexsmobs:textures/entity/seal/seal_brown_0.png");
    private static final ResourceLocation TEXTURE_BROWN_1 = new ResourceLocation("alexsmobs:textures/entity/seal/seal_brown_1.png");
    private static final ResourceLocation TEXTURE_ARCTIC_0 = new ResourceLocation("alexsmobs:textures/entity/seal/seal_arctic_0.png");
    private static final ResourceLocation TEXTURE_ARCTIC_1 = new ResourceLocation("alexsmobs:textures/entity/seal/seal_arctic_1.png");
    private static final ResourceLocation TEXTURE_ARCTIC_BABY = new ResourceLocation("alexsmobs:textures/entity/seal/seal_arctic_baby.png");

    public RenderSeal(RenderManager renderManagerIn) {
        super(renderManagerIn, new ModelSeal(), 0.45F);
        this.addLayer(new LayerSealItem(this));
        this.addLayer(new LayerSealTears(this));
    }

    @Override
    protected void preRenderCallback(EntitySeal entitylivingbaseIn, float partialTickTime) {
        GlStateManager.scale(1.3F, 1.3F, 1.3F);
    }

    @Override
    protected boolean canRenderName(EntitySeal entity) {
        return super.canRenderName(entity) || entity.isTearsEasterEgg();
    }

    @Override
    protected ResourceLocation getEntityTexture(EntitySeal entity) {
        if (entity.isArctic()) {
            if (entity.isChild()) {
                return TEXTURE_ARCTIC_BABY;
            }
            return entity.getVariant() == 1 ? TEXTURE_ARCTIC_1 : TEXTURE_ARCTIC_0;
        }
        return entity.getVariant() == 1 ? TEXTURE_BROWN_1 : TEXTURE_BROWN_0;
    }

    @SideOnly(Side.CLIENT)
    private static class LayerSealTears implements LayerRenderer<EntitySeal> {
        private static final ResourceLocation TEXTURE_TEARS = new ResourceLocation("alexsmobs:textures/entity/seal/seal_crying.png");
        private final RenderSeal renderer;

        LayerSealTears(RenderSeal renderer) {
            this.renderer = renderer;
        }

        @Override
        public void doRenderLayer(EntitySeal entity, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
            if (entity.isTearsEasterEgg()) {
                this.renderer.bindTexture(TEXTURE_TEARS);
                AMRenderTypes.beginEntityCutoutNoCull();
                this.renderer.getMainModel().render(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
                AMRenderTypes.endEntityCutoutNoCull();
            }
        }

        @Override
        public boolean shouldCombineTextures() {
            return true;
        }
    }
}
