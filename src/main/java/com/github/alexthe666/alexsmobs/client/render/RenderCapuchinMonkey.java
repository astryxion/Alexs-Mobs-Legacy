package com.github.alexthe666.alexsmobs.client.render;

import com.github.alexthe666.alexsmobs.client.model.ModelCapuchinMonkey;
import com.github.alexthe666.alexsmobs.client.render.layer.LayerCapuchinItem;
import com.github.alexthe666.alexsmobs.entity.EntityCapuchinMonkey;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderCapuchinMonkey extends RenderLiving<EntityCapuchinMonkey> {

    private static final ResourceLocation TEXTURE_0 = new ResourceLocation("alexsmobs:textures/entity/capuchin_monkey_0.png");
    private static final ResourceLocation TEXTURE_1 = new ResourceLocation("alexsmobs:textures/entity/capuchin_monkey_1.png");
    private static final ResourceLocation TEXTURE_2 = new ResourceLocation("alexsmobs:textures/entity/capuchin_monkey_2.png");
    private static final ResourceLocation TEXTURE_3 = new ResourceLocation("alexsmobs:textures/entity/capuchin_monkey_3.png");

    public RenderCapuchinMonkey(RenderManager renderManagerIn) {
        super(renderManagerIn, new ModelCapuchinMonkey(), 0.25F);
        this.addLayer(new LayerCapuchinItem(this));
    }

    @Override
    protected void preRenderCallback(EntityCapuchinMonkey entitylivingbaseIn, float partialTickTime) {
        GlStateManager.scale(0.8F, 0.8F, 0.8F);
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityCapuchinMonkey entity) {
        switch (entity.getVariant()) {
            case 1:
                return TEXTURE_1;
            case 2:
                return TEXTURE_2;
            case 3:
                return TEXTURE_3;
            default:
                return TEXTURE_0;
        }
    }
}
