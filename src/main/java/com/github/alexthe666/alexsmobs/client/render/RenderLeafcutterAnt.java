package com.github.alexthe666.alexsmobs.client.render;

import com.github.alexthe666.alexsmobs.client.model.ModelLeafcutterAnt;
import com.github.alexthe666.alexsmobs.client.model.ModelLeafcutterAntQueen;
import com.github.alexthe666.alexsmobs.client.render.layer.LayerLeafcutterAntLeaf;
import com.github.alexthe666.alexsmobs.entity.EntityLeafcutterAnt;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderLeafcutterAnt extends RenderLiving<EntityLeafcutterAnt> {
    private static final ResourceLocation TEXTURE = new ResourceLocation("alexsmobs:textures/entity/leafcutter_ant.png");
    private static final ResourceLocation TEXTURE_QUEEN = new ResourceLocation("alexsmobs:textures/entity/leafcutter_ant_queen.png");
    private static final ResourceLocation TEXTURE_ANGRY = new ResourceLocation("alexsmobs:textures/entity/leafcutter_ant_angry.png");
    private static final ResourceLocation TEXTURE_QUEEN_ANGRY = new ResourceLocation("alexsmobs:textures/entity/leafcutter_ant_queen_angry.png");
    private final ModelLeafcutterAnt model = new ModelLeafcutterAnt();
    private final ModelLeafcutterAntQueen modelQueen = new ModelLeafcutterAntQueen();

    public RenderLeafcutterAnt(RenderManager renderManagerIn) {
        super(renderManagerIn, new ModelLeafcutterAnt(), 0.25F);
        this.addLayer(new LayerLeafcutterAntLeaf(this));
    }

    @Override
    protected void applyRotations(EntityLeafcutterAnt entityLiving, float ageInTicks, float rotationYaw, float partialTicks) {
        if (Minecraft.getMinecraft().getRenderViewEntity() == entityLiving) {
            rotationYaw += (float) (Math.cos(entityLiving.ticksExisted * 3.25D) * Math.PI * 0.4D);
        }
        float trans = entityLiving.isChild() ? 0.25F : 0.5F;
        float progresso = 1.0F - (entityLiving.prevAttachChangeProgress + (entityLiving.attachChangeProgress - entityLiving.prevAttachChangeProgress) * partialTicks);

        if (entityLiving.getAttachmentFacing() == EnumFacing.DOWN) {
            GlStateManager.rotate(180.0F - rotationYaw, 0.0F, 1.0F, 0.0F);
            GlStateManager.translate(0.0D, trans, 0.0D);
            if (entityLiving.prevPosY < entityLiving.posY) {
                GlStateManager.rotate(90.0F * (1.0F - progresso), 1.0F, 0.0F, 0.0F);
            } else {
                GlStateManager.rotate(-90.0F * (1.0F - progresso), 1.0F, 0.0F, 0.0F);
            }
            GlStateManager.translate(0.0D, -trans, 0.0D);
        } else if (entityLiving.getAttachmentFacing() == EnumFacing.UP) {
            GlStateManager.rotate(180.0F - rotationYaw, 0.0F, 1.0F, 0.0F);
            GlStateManager.rotate(180.0F, 1.0F, 0.0F, 0.0F);
            GlStateManager.rotate(180.0F, 0.0F, 1.0F, 0.0F);
            GlStateManager.translate(0.0D, -trans, 0.0D);
        } else {
            GlStateManager.translate(0.0D, trans, 0.0D);
            switch (entityLiving.getAttachmentFacing()) {
                case NORTH:
                    GlStateManager.rotate(90.0F * progresso, 1.0F, 0.0F, 0.0F);
                    break;
                case SOUTH:
                    GlStateManager.rotate(180.0F, 0.0F, 1.0F, 0.0F);
                    GlStateManager.rotate(90.0F * progresso, 1.0F, 0.0F, 0.0F);
                    break;
                case WEST:
                    GlStateManager.rotate(90.0F, 1.0F, 0.0F, 0.0F);
                    GlStateManager.rotate(90.0F - 90.0F * progresso, 0.0F, 1.0F, 0.0F);
                    GlStateManager.rotate(-90.0F, 0.0F, 0.0F, 1.0F);
                    break;
                case EAST:
                    GlStateManager.rotate(90.0F, 1.0F, 0.0F, 0.0F);
                    GlStateManager.rotate(90.0F * progresso - 90.0F, 0.0F, 1.0F, 0.0F);
                    GlStateManager.rotate(90.0F, 0.0F, 0.0F, 1.0F);
                    break;
                default:
                    break;
            }
            if (entityLiving.motionY <= -0.001F) {
                GlStateManager.rotate(-180.0F, 0.0F, 1.0F, 0.0F);
            }
            GlStateManager.translate(0.0D, -trans, 0.0D);
        }

        if (entityLiving.deathTime > 0) {
            float f = ((float) entityLiving.deathTime + partialTicks - 1.0F) / 20.0F * 1.6F;
            f = MathHelper.sqrt(f);
            if (f > 1.0F) {
                f = 1.0F;
            }
            GlStateManager.rotate(f * this.getDeathMaxRotation(entityLiving), 0.0F, 0.0F, 1.0F);
        } else if (entityLiving.hasCustomName()) {
            String s = TextFormatting.getTextWithoutFormattingCodes(entityLiving.getName());
            if ("Dinnerbone".equals(s) || "Grumm".equals(s)) {
                GlStateManager.translate(0.0D, entityLiving.height + 0.1F, 0.0D);
                GlStateManager.rotate(180.0F, 0.0F, 0.0F, 1.0F);
            }
        }
    }

    @Override
    protected void preRenderCallback(EntityLeafcutterAnt entitylivingbaseIn, float partialTickTime) {
        this.mainModel = entitylivingbaseIn.isQueen() ? this.modelQueen : this.model;
        float scale = entitylivingbaseIn.getAntScale();
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityLeafcutterAnt entity) {
        if (entity.getAngerTime() > 0) {
            return entity.isQueen() ? TEXTURE_QUEEN_ANGRY : TEXTURE_ANGRY;
        } else {
            return entity.isQueen() ? TEXTURE_QUEEN : TEXTURE;
        }
    }
}
