package com.github.alexthe666.alexsmobs.client.render.layer;

import com.github.alexthe666.alexsmobs.client.model.ModelTiger;
import com.github.alexthe666.alexsmobs.client.render.AMRenderTypes;
import com.github.alexthe666.alexsmobs.client.render.RenderTiger;
import com.github.alexthe666.alexsmobs.entity.EntityTiger;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.EnumSkyBlock;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class LayerTigerEyes implements LayerRenderer<EntityTiger> {
    private static final ResourceLocation TEXTURE = new ResourceLocation("alexsmobs:textures/entity/tiger/tiger_eyes.png");
    private static final ResourceLocation TEXTURE_WHITE = new ResourceLocation("alexsmobs:textures/entity/tiger/tiger_white_eyes.png");
    private static final ResourceLocation TEXTURE_ANGRY = new ResourceLocation("alexsmobs:textures/entity/tiger/tiger_angry_eyes.png");
    private final RenderTiger renderer;

    public LayerTigerEyes(RenderTiger render) {
        this.renderer = render;
    }

    @Override
    public void doRenderLayer(EntityTiger tiger, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
        if (!tiger.isSleeping()) {
            long roundedTime = tiger.world.getWorldTime() % 24000L;
            boolean night = roundedTime >= 13000L && roundedTime <= 22000L;
            BlockPos ratPos = tiger.getPosition();
            int i = tiger.world.getLightFromNeighborsFor(EnumSkyBlock.SKY, ratPos);
            int j = tiger.world.getLightFromNeighborsFor(EnumSkyBlock.BLOCK, ratPos);
            int brightness;
            if (night) {
                brightness = j;
            } else {
                brightness = Math.max(i, j);
            }
            if (brightness < 7 || tiger.getAngerTime() > 0) {
                ResourceLocation eyes = tiger.getAngerTime() > 0 ? TEXTURE_ANGRY : (tiger.isWhite() ? TEXTURE_WHITE : TEXTURE);
                this.renderer.bindTexture(eyes);
                AMRenderTypes.beginEyesNoCull();
                ((ModelTiger) this.renderer.getMainModel()).render(tiger, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
                AMRenderTypes.endEyesNoCull();
            }
        }
    }

    @Override
    public boolean shouldCombineTextures() {
        return true;
    }
}
