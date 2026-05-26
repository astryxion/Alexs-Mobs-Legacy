package com.github.alexthe666.alexsmobs.client.render;

import com.github.alexthe666.alexsmobs.client.model.ModelStraddleboard;
import com.github.alexthe666.alexsmobs.entity.EntityStraddleboard;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderStraddleboard extends Render<EntityStraddleboard> {

    private static final ResourceLocation TEXTURE_OVERLAY = new ResourceLocation("alexsmobs:textures/entity/straddleboard_overlay.png");
    private static final ResourceLocation TEXTURE = new ResourceLocation("alexsmobs:textures/entity/straddleboard.png");
    private static final ModelStraddleboard BOARD_MODEL = new ModelStraddleboard();

    public RenderStraddleboard(RenderManager renderManager) {
        super(renderManager);
        this.shadowSize = 0.6F;
    }

    @Override
    public void doRender(EntityStraddleboard entityIn, double x, double y, double z, float entityYaw, float partialTicks) {
        GlStateManager.pushMatrix();
        GlStateManager.translate((float) x, (float) y, (float) z);

        GlStateManager.rotate(180.0F, 1.0F, 0.0F, 0.0F);

        float yaw = (entityIn.prevRotationYaw + (entityIn.rotationYaw - entityIn.prevRotationYaw) * partialTicks);
        float pitch = (entityIn.prevRotationPitch + (entityIn.rotationPitch - entityIn.prevRotationPitch) * partialTicks);

        GlStateManager.rotate(yaw, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(pitch, 1.0F, 0.0F, 0.0F);

        GlStateManager.pushMatrix();

        boolean lava = entityIn.isInLava() || entityIn.isBeingRidden();
        float f2 = entityIn.getRockingAngle(partialTicks);
        if (Math.abs(f2) > 1.0E-5F) {
            GlStateManager.rotate(f2, 0.0F, 0.0F, 1.0F);
        }

        int k = entityIn.getColor();
        float r = (float) (k >> 16 & 255) / 255.0F;
        float g = (float) (k >> 8 & 255) / 255.0F;
        float b = (float) (k & 255) / 255.0F;

        float boardInterp = entityIn.prevBoardRot + partialTicks * (entityIn.boardRot - entityIn.prevBoardRot);
        GlStateManager.rotate(boardInterp, 0.0F, 0.0F, 1.0F);
        GlStateManager.translate(0.0F, -1.5F - Math.abs(entityIn.boardRot * 0.007F) - (lava ? 0.25F : 0.0F), 0.0F);

        float ageInTicks = entityIn.ticksExisted + partialTicks;

        int light = entityIn.getBrightnessForRender();
        int lx = light % 65536;
        int ly = light / 65536;
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, (float) lx / 1.0F, (float) ly / 1.0F);

        Minecraft.getMinecraft().renderEngine.bindTexture(TEXTURE_OVERLAY);
        GlStateManager.color(r, g, b, 1.0F);
        BOARD_MODEL.render(entityIn, 0.0F, 0.0F, ageInTicks, 0.0F, 0.0F, 0.0625F);

        Minecraft.getMinecraft().renderEngine.bindTexture(TEXTURE);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        BOARD_MODEL.render(entityIn, 0.0F, 0.0F, ageInTicks, 0.0F, 0.0F, 0.0625F);

        GlStateManager.popMatrix();
        GlStateManager.popMatrix();

        super.doRender(entityIn, x, y, z, entityYaw, partialTicks);
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityStraddleboard entity) {
        return TEXTURE;
    }
}
