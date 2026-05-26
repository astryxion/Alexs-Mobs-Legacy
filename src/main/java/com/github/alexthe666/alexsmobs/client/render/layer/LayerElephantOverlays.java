package com.github.alexthe666.alexsmobs.client.render.layer;

import com.github.alexthe666.alexsmobs.client.model.ModelElephant;
import com.github.alexthe666.alexsmobs.client.render.AMRenderTypes;
import com.github.alexthe666.alexsmobs.client.render.RenderElephant;
import com.github.alexthe666.alexsmobs.entity.EntityElephant;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class LayerElephantOverlays implements LayerRenderer<EntityElephant> {
    private static final ResourceLocation[] ELEPHANT_DECOR_TEXTURES = new ResourceLocation[]{
            new ResourceLocation("alexsmobs:textures/entity/elephant/decor/white.png"),
            new ResourceLocation("alexsmobs:textures/entity/elephant/decor/orange.png"),
            new ResourceLocation("alexsmobs:textures/entity/elephant/decor/magenta.png"),
            new ResourceLocation("alexsmobs:textures/entity/elephant/decor/light_blue.png"),
            new ResourceLocation("alexsmobs:textures/entity/elephant/decor/yellow.png"),
            new ResourceLocation("alexsmobs:textures/entity/elephant/decor/lime.png"),
            new ResourceLocation("alexsmobs:textures/entity/elephant/decor/pink.png"),
            new ResourceLocation("alexsmobs:textures/entity/elephant/decor/gray.png"),
            new ResourceLocation("alexsmobs:textures/entity/elephant/decor/light_gray.png"),
            new ResourceLocation("alexsmobs:textures/entity/elephant/decor/cyan.png"),
            new ResourceLocation("alexsmobs:textures/entity/elephant/decor/purple.png"),
            new ResourceLocation("alexsmobs:textures/entity/elephant/decor/blue.png"),
            new ResourceLocation("alexsmobs:textures/entity/elephant/decor/brown.png"),
            new ResourceLocation("alexsmobs:textures/entity/elephant/decor/green.png"),
            new ResourceLocation("alexsmobs:textures/entity/elephant/decor/red.png"),
            new ResourceLocation("alexsmobs:textures/entity/elephant/decor/black.png")
    };
    private static final ResourceLocation TRADER_TEXTURE = new ResourceLocation("alexsmobs:textures/entity/elephant/decor/trader.png");
    private static final ResourceLocation TEXTURE_CHEST = new ResourceLocation("alexsmobs:textures/entity/elephant/elephant_chest.png");
    private final ModelElephant model = new ModelElephant(0.5F);
    private final RenderElephant renderer;

    public LayerElephantOverlays(RenderElephant renderElephant) {
        this.renderer = renderElephant;
    }

    @Override
    public void doRenderLayer(EntityElephant elephant, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
        ModelElephant mainModel = (ModelElephant) this.renderer.getMainModel();
        if (elephant.isChested()) {
            this.renderer.bindTexture(TEXTURE_CHEST);
            mainModel.render(elephant, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
        }
        EnumDyeColor dye = elephant.getColor();
        if (dye != null || elephant.isTrader()) {
            ResourceLocation decor;
            if (!elephant.isTrader()) {
                decor = ELEPHANT_DECOR_TEXTURES[dye.getMetadata()];
            } else {
                decor = TRADER_TEXTURE;
            }
            this.model.setRotationAngles(elephant, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
            this.renderer.bindTexture(decor);
            AMRenderTypes.beginEntityCutoutNoCull();
            this.model.render(elephant, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
            AMRenderTypes.endEntityCutoutNoCull();
        }
    }

    @Override
    public boolean shouldCombineTextures() {
        return true;
    }
}
