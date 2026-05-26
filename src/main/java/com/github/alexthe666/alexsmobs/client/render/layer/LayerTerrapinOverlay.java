package com.github.alexthe666.alexsmobs.client.render.layer;

import com.github.alexthe666.alexsmobs.client.model.ModelTerrapin;
import com.github.alexthe666.alexsmobs.client.render.AMRenderTypes;
import com.github.alexthe666.alexsmobs.client.render.RenderTerrapin;
import com.github.alexthe666.alexsmobs.entity.EntityTerrapin;
import com.github.alexthe666.alexsmobs.entity.util.TerrapinTypes;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class LayerTerrapinOverlay implements LayerRenderer<EntityTerrapin> {

    private static final ResourceLocation[] SHELL_TEXTURES = {
            new ResourceLocation("alexsmobs:textures/entity/terrapin/overlay/terrapin_shell_pattern_0.png"),
            new ResourceLocation("alexsmobs:textures/entity/terrapin/overlay/terrapin_shell_pattern_1.png"),
            new ResourceLocation("alexsmobs:textures/entity/terrapin/overlay/terrapin_shell_pattern_2.png"),
            new ResourceLocation("alexsmobs:textures/entity/terrapin/overlay/terrapin_shell_pattern_3.png"),
            new ResourceLocation("alexsmobs:textures/entity/terrapin/overlay/terrapin_shell_pattern_4.png"),
            new ResourceLocation("alexsmobs:textures/entity/terrapin/overlay/terrapin_shell_pattern_5.png")
    };
    private static final ResourceLocation[] SKIN_PATTERN_TEXTURES = {
            new ResourceLocation("alexsmobs:textures/entity/terrapin/overlay/terrapin_skin_pattern_0.png"),
            new ResourceLocation("alexsmobs:textures/entity/terrapin/overlay/terrapin_skin_pattern_1.png"),
            new ResourceLocation("alexsmobs:textures/entity/terrapin/overlay/terrapin_skin_pattern_2.png"),
            new ResourceLocation("alexsmobs:textures/entity/terrapin/overlay/terrapin_skin_pattern_3.png")
    };

    private final RenderTerrapin renderer;
    private final int layer;

    public LayerTerrapinOverlay(RenderTerrapin render, int layer) {
        this.renderer = render;
        this.layer = layer;
    }

    @Override
    public void doRenderLayer(EntityTerrapin turtle, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
        if (turtle.getTurtleType() == TerrapinTypes.OVERLAY && !turtle.isKoopa()) {
            ResourceLocation tex = layer == 0 ? this.renderer.getEntityTexture(turtle)
                    : layer == 1 ? SHELL_TEXTURES[turtle.getShellType() % SHELL_TEXTURES.length]
                    : SKIN_PATTERN_TEXTURES[turtle.getSkinType() % SKIN_PATTERN_TEXTURES.length];
            int color = layer == 0 ? turtle.getTurtleColor() : layer == 1 ? turtle.getShellColor() : turtle.getSkinColor();
            float r = (float) (color >> 16 & 255) / 255.0F;
            float g = (float) (color >> 8 & 255) / 255.0F;
            float b = (float) (color & 255) / 255.0F;
            this.renderer.bindTexture(tex);
            AMRenderTypes.beginEntityTranslucent();
            GlStateManager.color(r, g, b, 1.0F);
            ((ModelTerrapin) this.renderer.getMainModel()).render(turtle, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
            AMRenderTypes.endEntityTranslucent();
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        }
    }

    @Override
    public boolean shouldCombineTextures() {
        return true;
    }
}
