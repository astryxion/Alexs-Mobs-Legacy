package com.github.alexthe666.alexsmobs.client.render.layer;

import com.github.alexthe666.alexsmobs.client.model.ModelRaccoon;
import com.github.alexthe666.alexsmobs.client.render.AMRenderTypes;
import com.github.alexthe666.alexsmobs.client.render.RenderRaccoon;
import com.github.alexthe666.alexsmobs.entity.EntityRaccoon;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.entity.passive.EntitySheep;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class LayerRaccoonBandana implements LayerRenderer<EntityRaccoon> {
    private static final ResourceLocation TEXTURE_BANDANA = new ResourceLocation("alexsmobs:textures/entity/raccoon_bandana.png");
    private final RenderRaccoon renderer;

    public LayerRaccoonBandana(RenderRaccoon renderRaccoon) {
        this.renderer = renderRaccoon;
    }

    @Override
    public void doRenderLayer(EntityRaccoon raccoon, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
        if (raccoon.getColor() != null && !raccoon.isInvisible()) {
            float r;
            float g;
            float b;
            if (raccoon.hasCustomName() && "jeb_".equals(raccoon.getName())) {
                int i = raccoon.ticksExisted / 25 + raccoon.getEntityId();
                int colors = EnumDyeColor.values().length;
                int cur = i % colors;
                int next = (i + 1) % colors;
                float blend = ((float) (raccoon.ticksExisted % 25) + partialTicks) / 25.0F;
                float[] c0 = EntitySheep.getDyeRgb(EnumDyeColor.byMetadata(cur));
                float[] c1 = EntitySheep.getDyeRgb(EnumDyeColor.byMetadata(next));
                r = c0[0] * (1.0F - blend) + c1[0] * blend;
                g = c0[1] * (1.0F - blend) + c1[1] * blend;
                b = c0[2] * (1.0F - blend) + c1[2] * blend;
            } else {
                float[] col = EntitySheep.getDyeRgb(raccoon.getColor());
                r = col[0];
                g = col[1];
                b = col[2];
            }
            this.renderer.bindTexture(TEXTURE_BANDANA);
            AMRenderTypes.beginEntityCutoutNoCull();
            GlStateManager.color(r, g, b, 1.0F);
            ((ModelRaccoon) this.renderer.getMainModel()).render(raccoon, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            AMRenderTypes.endEntityCutoutNoCull();
        }
    }

    @Override
    public boolean shouldCombineTextures() {
        return true;
    }
}
