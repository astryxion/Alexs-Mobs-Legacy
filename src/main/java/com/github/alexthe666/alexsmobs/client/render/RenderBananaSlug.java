package com.github.alexthe666.alexsmobs.client.render;

import com.github.alexthe666.alexsmobs.client.model.ModelBananaSlug;
import com.github.alexthe666.alexsmobs.client.render.layer.LayerBananaSlugSlime;
import com.github.alexthe666.alexsmobs.entity.EntityBananaSlug;
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
public class RenderBananaSlug extends RenderLiving<EntityBananaSlug> {
    private static final ResourceLocation TEXTURE_0 = new ResourceLocation("alexsmobs:textures/entity/banana_slug/banana_slug_0.png");
    private static final ResourceLocation TEXTURE_1 = new ResourceLocation("alexsmobs:textures/entity/banana_slug/banana_slug_1.png");
    private static final ResourceLocation TEXTURE_2 = new ResourceLocation("alexsmobs:textures/entity/banana_slug/banana_slug_2.png");
    private static final ResourceLocation TEXTURE_3 = new ResourceLocation("alexsmobs:textures/entity/banana_slug/banana_slug_3.png");

    public RenderBananaSlug(RenderManager renderManagerIn) {
        super(renderManagerIn, new ModelBananaSlug(), 0.2F);
        this.addLayer(new LayerBananaSlugSlime(this));
    }

    @Override
    protected void preRenderCallback(EntityBananaSlug entitylivingbaseIn, float partialTickTime) {
        GlStateManager.scale(0.9F, 0.9F, 0.9F);
    }

    private EnumFacing rotate(EnumFacing attachmentFacing) {
        return attachmentFacing.getAxis() == EnumFacing.Axis.Y ? EnumFacing.UP : attachmentFacing;
    }

    private void rotateForAngle(EnumFacing rotate, float f) {
        if (rotate == null) {
            return;
        }
        if (rotate.getAxis() != EnumFacing.Axis.Y) {
            GlStateManager.rotate(90.0F * f, 1.0F, 0.0F, 0.0F);
        }
        if (rotate == EnumFacing.DOWN) {
            GlStateManager.rotate(180.0F * f, 0.0F, 0.0F, 1.0F);
        } else if (rotate == EnumFacing.NORTH) {
            GlStateManager.rotate(180.0F * f, 0.0F, 0.0F, 1.0F);
        } else if (rotate == EnumFacing.WEST) {
            GlStateManager.rotate(90F * f, 0.0F, 0.0F, 1.0F);
        } else if (rotate == EnumFacing.EAST) {
            GlStateManager.rotate(-90F * f, 0.0F, 0.0F, 1.0F);
        }
    }

    @Override
    protected void applyRotations(EntityBananaSlug entityLiving, float ageInTicks, float rotationYaw, float partialTicks) {
        if (entityLiving.isRiding()) {
            super.applyRotations(entityLiving, ageInTicks, rotationYaw, partialTicks);
            return;
        }
        if (Minecraft.getMinecraft().getRenderViewEntity() == entityLiving) {
            rotationYaw += (float) (Math.cos(entityLiving.ticksExisted * 3.25D) * Math.PI * 0.4D);
        }
        float trans = entityLiving.isChild() ? 0.2F : 0.4F;
        if (!entityLiving.isPlayerSleeping()) {
            float progress = (entityLiving.prevAttachChangeProgress + (entityLiving.attachChangeProgress - entityLiving.prevAttachChangeProgress) * partialTicks) * 0.2F;
            float yawMul = 0F;
            if (entityLiving.prevAttachDir == entityLiving.getAttachmentFacing() && entityLiving.getAttachmentFacing().getAxis() == EnumFacing.Axis.Y) {
                yawMul = 1.0F;
            }
            GlStateManager.rotate(180.0F - yawMul * rotationYaw, 0.0F, 1.0F, 0.0F);
            GlStateManager.translate(0.0F, trans, 0.0F);
            float prevProg = 1F - progress;
            rotateForAngle(rotate(entityLiving.prevAttachDir), prevProg);
            rotateForAngle(rotate(entityLiving.getAttachmentFacing()), progress);
            if (entityLiving.getAttachmentFacing() != EnumFacing.DOWN) {
                GlStateManager.translate(0.0F, trans, 0.0F);
                if (entityLiving.motionY <= -0.001F) {
                    GlStateManager.rotate(180 * progress, 0.0F, 1.0F, 0.0F);
                }
                GlStateManager.translate(0.0F, -trans, 0.0F);
            }
            GlStateManager.translate(0.0F, -trans, 0.0F);
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
                GlStateManager.translate(0.0F, entityLiving.height + 0.1F, 0.0F);
                GlStateManager.rotate(180.0F, 0.0F, 0.0F, 1.0F);
            }
        }
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityBananaSlug entity) {
        int variant = entity.getVariant();
        if (variant == 1) {
            return TEXTURE_1;
        } else if (variant == 2) {
            return TEXTURE_2;
        } else if (variant == 3) {
            return TEXTURE_3;
        }
        return TEXTURE_0;
    }
}
