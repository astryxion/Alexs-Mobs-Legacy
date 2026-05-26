package com.github.alexthe666.alexsmobs.client.render.layer;

import com.github.alexthe666.alexsmobs.client.model.ModelLeafcutterAnt;
import com.github.alexthe666.alexsmobs.client.render.AMRenderTypes;
import com.github.alexthe666.alexsmobs.client.render.RenderLeafcutterAnt;
import com.github.alexthe666.alexsmobs.entity.EntityLeafcutterAnt;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class LayerLeafcutterAntLeaf implements LayerRenderer<EntityLeafcutterAnt> {

    private static final ResourceLocation TEXTURE_0 = new ResourceLocation("alexsmobs:textures/entity/leafcutter_ant_leaf_0.png");
    private static final ResourceLocation TEXTURE_1 = new ResourceLocation("alexsmobs:textures/entity/leafcutter_ant_leaf_1.png");
    private static final ResourceLocation TEXTURE_2 = new ResourceLocation("alexsmobs:textures/entity/leafcutter_ant_leaf_2.png");
    private final RenderLeafcutterAnt renderer;

    public LayerLeafcutterAntLeaf(RenderLeafcutterAnt render) {
        this.renderer = render;
    }

    @Override
    public void doRenderLayer(EntityLeafcutterAnt entitylivingbaseIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
        if (entitylivingbaseIn.hasLeaf() && !entitylivingbaseIn.isQueen() && this.renderer.getMainModel() instanceof ModelLeafcutterAnt) {
            int leafType = entitylivingbaseIn.getEntityId() % 3;
            ResourceLocation res;
            if (leafType == 2) {
                res = TEXTURE_2;
            } else if (leafType == 1) {
                res = TEXTURE_1;
            } else {
                res = TEXTURE_0;
            }
            int leafColor = Minecraft.getMinecraft().getItemColors().colorMultiplier(new ItemStack(net.minecraft.init.Blocks.LEAVES, 1, 3), 0);
            if (entitylivingbaseIn.getHarvestedPos() != null && entitylivingbaseIn.getHarvestedState() != null) {
                leafColor = Minecraft.getMinecraft().getBlockColors().getColor(entitylivingbaseIn.getHarvestedState(), entitylivingbaseIn.world, entitylivingbaseIn.getHarvestedPos());
            }
            float f = (float) (leafColor >> 16 & 255) / 255.0F;
            float f1 = (float) (leafColor >> 8 & 255) / 255.0F;
            float f2 = (float) (leafColor & 255) / 255.0F;
            this.renderer.bindTexture(res);
            AMRenderTypes.beginEntityCutoutNoCull();
            GlStateManager.color(f, f1, f2, 1.0F);
            this.renderer.getMainModel().render(entitylivingbaseIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            AMRenderTypes.endEntityCutoutNoCull();
        }
    }

    @Override
    public boolean shouldCombineTextures() {
        return true;
    }
}
