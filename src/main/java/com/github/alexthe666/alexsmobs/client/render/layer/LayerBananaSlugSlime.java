package com.github.alexthe666.alexsmobs.client.render.layer;

import com.github.alexthe666.alexsmobs.client.model.ModelBananaSlug;
import com.github.alexthe666.alexsmobs.client.render.AMRenderTypes;
import com.github.alexthe666.alexsmobs.client.render.RenderBananaSlug;
import com.github.alexthe666.alexsmobs.entity.EntityBananaSlug;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class LayerBananaSlugSlime implements LayerRenderer<EntityBananaSlug> {

    private static final ResourceLocation TEXTURE_SLIME = new ResourceLocation("alexsmobs:textures/entity/banana_slug/banana_slug_slime.png");
    private final RenderBananaSlug renderer;

    public LayerBananaSlugSlime(RenderBananaSlug renderer) {
        this.renderer = renderer;
    }

    @Override
    public void doRenderLayer(EntityBananaSlug entitylivingbaseIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
        float alpha = entitylivingbaseIn.prevTrailVisability + (entitylivingbaseIn.trailVisability - entitylivingbaseIn.prevTrailVisability) * partialTicks;
        if (alpha > 0.0F && this.renderer.getMainModel() instanceof ModelBananaSlug) {
            this.renderer.bindTexture(TEXTURE_SLIME);
            AMRenderTypes.beginEntityTranslucent();
            GlStateManager.color(1.0F, 1.0F, 1.0F, alpha);
            this.renderer.getMainModel().render(entitylivingbaseIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            AMRenderTypes.endEntityTranslucent();
        }
    }

    @Override
    public boolean shouldCombineTextures() {
        return true;
    }
}
