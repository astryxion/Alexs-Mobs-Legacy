package com.github.alexthe666.alexsmobs.client.render.layer;

import com.github.alexthe666.alexsmobs.client.model.ModelRaccoon;
import com.github.alexthe666.alexsmobs.client.render.AMRenderTypes;
import com.github.alexthe666.alexsmobs.client.render.RenderRaccoon;
import com.github.alexthe666.alexsmobs.entity.EntityRaccoon;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.EnumSkyBlock;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class LayerRaccoonEyes implements LayerRenderer<EntityRaccoon> {
    private static final ResourceLocation TEXTURE = new ResourceLocation("alexsmobs:textures/entity/raccoon_eyes.png");
    private final RenderRaccoon renderer;

    public LayerRaccoonEyes(RenderRaccoon render) {
        this.renderer = render;
    }

    @Override
    public void doRenderLayer(EntityRaccoon raccoon, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
        long roundedTime = raccoon.world.getWorldTime() % 24000L;
        boolean night = roundedTime >= 13000L && roundedTime <= 22000L;
        BlockPos ratPos = raccoon.getPosition();
        int i = raccoon.world.getLightFromNeighborsFor(EnumSkyBlock.SKY, ratPos);
        int j = raccoon.world.getLightFromNeighborsFor(EnumSkyBlock.BLOCK, ratPos);
        int brightness;
        if (night) {
            brightness = j;
        } else {
            brightness = Math.max(i, j);
        }
        if (brightness < 7) {
            this.renderer.bindTexture(TEXTURE);
            AMRenderTypes.beginEyesNoCull();
            ((ModelRaccoon) this.renderer.getMainModel()).render(raccoon, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
            AMRenderTypes.endEyesNoCull();
        }
    }

    @Override
    public boolean shouldCombineTextures() {
        return true;
    }
}
