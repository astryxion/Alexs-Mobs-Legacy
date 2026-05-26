package com.github.alexthe666.alexsmobs.client.render;

import com.github.alexthe666.alexsmobs.client.model.ModelEmu;
import com.github.alexthe666.alexsmobs.entity.EntityEmu;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderEmu extends RenderLiving<EntityEmu> {
    private static final ResourceLocation TEXTURE = new ResourceLocation("alexsmobs:textures/entity/emu.png");
    private static final ResourceLocation TEXTURE_BABY = new ResourceLocation("alexsmobs:textures/entity/emu_baby.png");
    private static final ResourceLocation TEXTURE_BLONDE = new ResourceLocation("alexsmobs:textures/entity/emu_blonde.png");
    private static final ResourceLocation TEXTURE_BLONDE_BABY = new ResourceLocation("alexsmobs:textures/entity/emu_baby_blonde.png");
    private static final ResourceLocation TEXTURE_BLUE = new ResourceLocation("alexsmobs:textures/entity/emu_blue.png");

    public RenderEmu(RenderManager renderManagerIn) {
        super(renderManagerIn, new ModelEmu(), 0.45F);
    }

    @Override
    protected void preRenderCallback(EntityEmu entitylivingbaseIn, float partialTickTime) {
        GlStateManager.scale(0.85F, 0.85F, 0.85F);
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityEmu entity) {
        if (entity.getVariant() == 2) {
            return entity.isChild() ? TEXTURE_BLONDE_BABY : TEXTURE_BLONDE;
        }
        if (entity.getVariant() == 1 && !entity.isChild()) {
            return TEXTURE_BLUE;
        }
        return entity.isChild() ? TEXTURE_BABY : TEXTURE;
    }
}
