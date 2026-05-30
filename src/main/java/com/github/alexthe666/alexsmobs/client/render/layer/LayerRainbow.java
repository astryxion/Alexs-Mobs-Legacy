package com.github.alexthe666.alexsmobs.client.render.layer;

import com.github.alexthe666.alexsmobs.client.render.AMRenderTypes;
import com.github.alexthe666.alexsmobs.entity.util.RainbowUtil;
import com.github.alexthe666.alexsmobs.item.ItemRainbowJelly;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class LayerRainbow implements LayerRenderer<EntityLivingBase> {

    private static final ResourceLocation GLINT_RAINBOW = new ResourceLocation("alexsmobs:textures/entity/rainbow_jelly_overlays/glint_rainbow.png");
    private static final ResourceLocation GLINT_TRANS = new ResourceLocation("alexsmobs:textures/entity/rainbow_jelly_overlays/glint_trans.png");
    private static final ResourceLocation GLINT_NONBI = new ResourceLocation("alexsmobs:textures/entity/rainbow_jelly_overlays/glint_nonbi.png");
    private static final ResourceLocation GLINT_BI = new ResourceLocation("alexsmobs:textures/entity/rainbow_jelly_overlays/glint_bi.png");
    private static final ResourceLocation GLINT_ACE = new ResourceLocation("alexsmobs:textures/entity/rainbow_jelly_overlays/glint_ace.png");
    private static final ResourceLocation GLINT_WEEZER = new ResourceLocation("alexsmobs:textures/entity/rainbow_jelly_overlays/glint_weezer.png");
    private static final ResourceLocation GLINT_BRAZIL = new ResourceLocation("alexsmobs:textures/entity/rainbow_jelly_overlays/glint_brazil.png");

    private final RenderLivingBase<?> renderer;

    public LayerRainbow(RenderLivingBase<?> renderer) {
        this.renderer = renderer;
    }

    @Override
    public void doRenderLayer(EntityLivingBase entity, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
        int i = RainbowUtil.getRainbowType(entity);
        if (i > 0) {
            ItemRainbowJelly.RainbowType rainbowType = ItemRainbowJelly.RainbowType.values()[MathHelper.clamp(i - 1, 0, ItemRainbowJelly.RainbowType.values().length - 1)];
            this.renderer.bindTexture(getGlintTexture(rainbowType));
            if (rainbowType == ItemRainbowJelly.RainbowType.WEEZER) {
                AMRenderTypes.beginWeezerRainbowGlint();
            } else if (rainbowType == ItemRainbowJelly.RainbowType.BRAZIL) {
                AMRenderTypes.beginBrazilRainbowGlint();
            } else {
                AMRenderTypes.beginRainbowGlint();
            }
            ModelBase model = this.renderer.getMainModel();
            model.setLivingAnimations(entity, limbSwing, limbSwingAmount, partialTicks);
            model.render(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
            AMRenderTypes.endRainbowGlint();
        }
    }

    private ResourceLocation getGlintTexture(ItemRainbowJelly.RainbowType rainbowType) {
        switch (rainbowType) {
            case TRANS:
                return GLINT_TRANS;
            case NONBI:
                return GLINT_NONBI;
            case BI:
                return GLINT_BI;
            case ACE:
                return GLINT_ACE;
            case WEEZER:
                return GLINT_WEEZER;
            case BRAZIL:
                return GLINT_BRAZIL;
            default:
                return GLINT_RAINBOW;
        }
    }

    @Override
    public boolean shouldCombineTextures() {
        return true;
    }
}
