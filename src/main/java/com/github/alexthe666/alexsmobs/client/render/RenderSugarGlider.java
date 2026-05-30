package com.github.alexthe666.alexsmobs.client.render;

import com.github.alexthe666.alexsmobs.client.model.ModelSugarGlider;
import com.github.alexthe666.alexsmobs.entity.EntitySugarGlider;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderSugarGlider extends RenderLiving<EntitySugarGlider> {
    private static final ResourceLocation TEXTURE = new ResourceLocation("alexsmobs:textures/entity/sugar_glider.png");

    public RenderSugarGlider(RenderManager renderManagerIn) {
        super(renderManagerIn, new ModelSugarGlider(), 0.35F);
    }

    @Override
    protected ResourceLocation getEntityTexture(EntitySugarGlider entity) {
        return TEXTURE;
    }

    @Override
    public boolean shouldRender(EntitySugarGlider entity, net.minecraft.client.renderer.culling.ICamera camera, double camX, double camY, double camZ) {
        if (entity.isRiding() && entity.getRidingEntity() instanceof EntityPlayer
                && Minecraft.getMinecraft().player == entity.getRidingEntity()
                && Minecraft.getMinecraft().gameSettings.thirdPersonView == 0) {
            return false;
        }
        return super.shouldRender(entity, camera, camX, camY, camZ);
    }

    @Override
    protected void preRenderCallback(EntitySugarGlider entity, float partialTickTime) {
        if (!entity.isRiding() || !(entity.getRidingEntity() instanceof EntityPlayer)) {
            return;
        }
        EntityPlayer mount = (EntityPlayer) entity.getRidingEntity();
        if (Minecraft.getMinecraft().player == mount && Minecraft.getMinecraft().gameSettings.thirdPersonView == 0) {
            return;
        }
        net.minecraft.client.renderer.entity.Render<?> playerRender = Minecraft.getMinecraft().getRenderManager().getEntityRenderObject(mount);
        if (playerRender instanceof net.minecraft.client.renderer.entity.RenderLivingBase) {
            net.minecraft.client.renderer.entity.RenderLivingBase<?> livingR = (net.minecraft.client.renderer.entity.RenderLivingBase<?>) playerRender;
            if (livingR.getMainModel() instanceof ModelBiped) {
                ModelBiped biped = (ModelBiped) livingR.getMainModel();
                GlStateManager.pushMatrix();
                GlStateManager.translate(0.0F, 0.5F, 0.0F);
                biped.bipedHead.postRender(0.0625F);
                GlStateManager.translate(0.0F, -0.5F, 0.0F);
                GlStateManager.popMatrix();
            }
        }
    }

    private EnumFacing rotateFacing(EnumFacing attachmentFacing) {
        return attachmentFacing.getAxis() == EnumFacing.Axis.Y ? EnumFacing.UP : attachmentFacing;
    }

    private void rotateForAngle(EnumFacing rotate, float amount) {
        if (rotate == null || amount <= 0.0F) {
            return;
        }
        if (rotate.getAxis() != EnumFacing.Axis.Y) {
            GlStateManager.rotate(90.0F * amount, 1.0F, 0.0F, 0.0F);
        }
        if (rotate == EnumFacing.DOWN) {
            GlStateManager.rotate(180.0F * amount, 0.0F, 0.0F, 1.0F);
        } else if (rotate == EnumFacing.NORTH) {
            GlStateManager.rotate(180.0F * amount, 0.0F, 0.0F, 1.0F);
        } else if (rotate == EnumFacing.WEST) {
            GlStateManager.rotate(90.0F * amount, 0.0F, 0.0F, 1.0F);
        } else if (rotate == EnumFacing.EAST) {
            GlStateManager.rotate(-90.0F * amount, 0.0F, 0.0F, 1.0F);
        }
    }

    @Override
    protected void applyRotations(EntitySugarGlider entityLiving, float ageInTicks, float rotationYaw, float partialTicks) {
        if (entityLiving.isRiding()) {
            super.applyRotations(entityLiving, ageInTicks, rotationYaw, partialTicks);
            return;
        }
        if (Minecraft.getMinecraft().getRenderViewEntity() == entityLiving) {
            rotationYaw += (float) (Math.cos(entityLiving.ticksExisted * 3.25D) * Math.PI * 0.4D);
        }
        float trans = entityLiving.isChild() ? 0.2F : 0.4F;
        if (!entityLiving.isPlayerSleeping()) {
            float prevProg = entityLiving.prevAttachChangeProgress + (entityLiving.attachChangeProgress - entityLiving.prevAttachChangeProgress) * partialTicks;
            float blend = 1.0F - prevProg;
            float yawMul = 0.0F;
            if (entityLiving.getAttachmentFacing() == EnumFacing.DOWN
                    && entityLiving.prevAttachDir == EnumFacing.DOWN) {
                yawMul = 1.0F;
            }
            GlStateManager.rotate(180.0F - yawMul * rotationYaw, 0.0F, 1.0F, 0.0F);

            if (entityLiving.getAttachmentFacing() == EnumFacing.DOWN) {
                GlStateManager.translate(0.0F, trans, 0.0F);
                if (entityLiving.lastTickPosY <= entityLiving.posY) {
                    GlStateManager.rotate(90.0F * prevProg, 1.0F, 0.0F, 0.0F);
                } else {
                    GlStateManager.rotate(-90.0F * prevProg, 1.0F, 0.0F, 0.0F);
                }
                GlStateManager.translate(0.0F, -trans, 0.0F);
            }

            GlStateManager.translate(0.0F, trans, 0.0F);
            rotateForAngle(rotateFacing(entityLiving.getAttachmentFacing()), blend);
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
}
