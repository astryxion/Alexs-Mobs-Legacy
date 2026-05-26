package com.github.alexthe666.alexsmobs.client.render;

import com.github.alexthe666.alexsmobs.client.model.ModelLobster;
import com.github.alexthe666.alexsmobs.entity.EntityLobster;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderLobster extends RenderLiving<EntityLobster> {
    private static final ResourceLocation TEXTURE_RED = new ResourceLocation("alexsmobs:textures/entity/lobster_red.png");
    private static final ResourceLocation TEXTURE_BLUE = new ResourceLocation("alexsmobs:textures/entity/lobster_blue.png");
    private static final ResourceLocation TEXTURE_YELLOW = new ResourceLocation("alexsmobs:textures/entity/lobster_yellow.png");
    private static final ResourceLocation TEXTURE_REDBLUE = new ResourceLocation("alexsmobs:textures/entity/lobster_redblue.png");
    private static final ResourceLocation TEXTURE_BLACK = new ResourceLocation("alexsmobs:textures/entity/lobster_black.png");
    private static final ResourceLocation TEXTURE_WHITE = new ResourceLocation("alexsmobs:textures/entity/lobster_white.png");

    public RenderLobster(RenderManager renderManagerIn) {
        super(renderManagerIn, new ModelLobster(), 0.25F);
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityLobster entity) {
        switch (entity.getVariant()) {
            case 1:
                return TEXTURE_BLUE;
            case 2:
                return TEXTURE_YELLOW;
            case 3:
                return TEXTURE_REDBLUE;
            case 4:
                return TEXTURE_BLACK;
            case 5:
                return TEXTURE_WHITE;
            default:
                return TEXTURE_RED;
        }
    }
}
