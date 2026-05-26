package com.github.alexthe666.alexsmobs.client.render.layer;

import com.github.alexthe666.alexsmobs.client.model.ModelGiantSquid;
import com.github.alexthe666.alexsmobs.client.render.RenderGiantSquid;
import com.github.alexthe666.alexsmobs.entity.EntityGiantSquid;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class LayerGiantSquidDepressurization implements LayerRenderer<EntityGiantSquid> {

    private static final ResourceLocation TEXTURE_DEPRESSURIZED = new ResourceLocation("alexsmobs:textures/entity/giant_squid_depressurized.png");
    private final RenderGiantSquid renderer;

    public LayerGiantSquidDepressurization(RenderGiantSquid render) {
        this.renderer = render;
    }

    @Override
    public void doRenderLayer(EntityGiantSquid squid, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
        float alpha = squid.prevDepressurization + (squid.getDepressurization() - squid.prevDepressurization) * partialTicks;
        if (alpha <= 0.001F) {
            return;
        }
        this.renderer.bindTexture(TEXTURE_DEPRESSURIZED);
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.color(1.0F, 1.0F, 1.0F, alpha);
        GlStateManager.disableCull();
        ((ModelGiantSquid) this.renderer.getMainModel()).render(squid, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
        GlStateManager.enableCull();
        GlStateManager.disableBlend();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    }

    @Override
    public boolean shouldCombineTextures() {
        return true;
    }
}
