package com.github.alexthe666.alexsmobs.client.render;

import com.github.alexthe666.alexsmobs.entity.EntityVineLasso;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.EnumHandSide;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class RenderVineLasso extends Render<EntityVineLasso> {
    private static final ResourceLocation TEXTURE = new ResourceLocation("alexsmobs:textures/entity/vine_lasso.png");

    private static final float VINES_COLOR_R = 96F / 255F;
    private static final float VINES_COLOR_G = 143F / 255F;
    private static final float VINES_COLOR_B = 62F / 255F;
    private static final float VINES_COLOR_R2 = 166F / 255F;
    private static final float VINES_COLOR_G2 = 191F / 255F;
    private static final float VINES_COLOR_B2 = 97F / 255F;

    public RenderVineLasso(RenderManager renderManagerIn) {
        super(renderManagerIn);
    }

    @Override
    public void doRender(EntityVineLasso entityIn, double x, double y, double z, float entityYaw, float partialTicks) {
        GlStateManager.pushMatrix();
        GlStateManager.translate((float) x, (float) y, (float) z);
        GlStateManager.translate(0.0D, 0.25F, 0.0D);
        GlStateManager.rotate((entityIn.prevRotationYaw + (entityIn.rotationYaw - entityIn.prevRotationYaw) * partialTicks) - 180F, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate((entityIn.prevRotationPitch + (entityIn.rotationPitch - entityIn.prevRotationPitch) * partialTicks), 1.0F, 0.0F, 0.0F);
        GlStateManager.translate(0.0D, -0.1F, 0.0D);
        GlStateManager.pushMatrix();
        GlStateManager.scale(0.45F, 0.45F, 0.45F);
        renderCircle(entityIn);
        GlStateManager.popMatrix();
        GlStateManager.popMatrix();
        super.doRender(entityIn, x, y, z, entityYaw, partialTicks);
        Entity holderEntity = entityIn.getShooter();
        EntityLivingBase holder = holderEntity instanceof EntityLivingBase ? (EntityLivingBase) holderEntity : Minecraft.getMinecraft().player;
        if (holder != null) {
            renderVine(entityIn, partialTicks, holder, x, y, z, holder.getPrimaryHand() != EnumHandSide.LEFT, -0.4F, entityIn.height * 0.5F);
        }
    }

    private void renderCircle(EntityVineLasso entityIn) {
        GlStateManager.pushMatrix();
        this.bindTexture(TEXTURE);
        int light = entityIn.getBrightnessForRender();
        int lx = light % 65536;
        int ly = light / 65536;
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, (float) lx, (float) ly);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR_NORMAL);
        drawVertex(buffer, -1, 0, -1, 0, 0, 1, 0, 1);
        drawVertex(buffer, -1, 0, 1, 0, 1, 1, 0, 1);
        drawVertex(buffer, 1, 0, 1, 1, 1, 1, 0, 1);
        drawVertex(buffer, 1, 0, -1, 1, 0, 1, 0, 1);
        tessellator.draw();
        GlStateManager.popMatrix();
    }

    public static void renderVine(EntityLivingBase mob, float partialTick, EntityLivingBase player, double renderX, double renderY, double renderZ, boolean left, float zOffset) {
        renderVine(mob, partialTick, player, renderX, renderY, renderZ, left, zOffset, mob.getEyeHeight());
    }

    public static void renderVine(Entity mob, float partialTick, EntityLivingBase player, double renderX, double renderY, double renderZ, boolean left, float zOffset, float attachHeight) {
        float bodyRot = mob instanceof EntityLivingBase ? ((EntityLivingBase) mob).renderYawOffset : mob.rotationYaw;
        float bodyRot0 = mob instanceof EntityLivingBase ? ((EntityLivingBase) mob).prevRenderYawOffset : mob.prevRotationYaw;
        Vec3d holdPos = getRopeHoldPosition(player, partialTick);
        double bodyAngle = (bodyRot0 + (bodyRot - bodyRot0) * partialTick) * ((float) Math.PI / 180F) + (Math.PI / 2D);
        Vec3d attachOffset = new Vec3d(left ? -0.05F : 0.05F, attachHeight, zOffset);
        double offsetX = Math.cos(bodyAngle) * attachOffset.z + Math.sin(bodyAngle) * attachOffset.x;
        double offsetZ = Math.sin(bodyAngle) * attachOffset.z - Math.cos(bodyAngle) * attachOffset.x;

        double mobWorldX = mob.lastTickPosX + (mob.posX - mob.lastTickPosX) * partialTick;
        double mobWorldY = mob.lastTickPosY + (mob.posY - mob.lastTickPosY) * partialTick;
        double mobWorldZ = mob.lastTickPosZ + (mob.posZ - mob.lastTickPosZ) * partialTick;
        double anchorWorldX = mobWorldX + offsetX;
        double anchorWorldY = mobWorldY + attachOffset.y;
        double anchorWorldZ = mobWorldZ + offsetZ;

        renderX += offsetX;
        renderZ += offsetZ;
        double baseY = renderY + attachOffset.y;

        float dx = (float) (holdPos.x - anchorWorldX);
        float dy = (float) (holdPos.y - anchorWorldY);
        float dz = (float) (holdPos.z - anchorWorldZ);

        GlStateManager.pushMatrix();
        GlStateManager.translate(renderX, baseY, renderZ);
        GlStateManager.disableTexture2D();
        GlStateManager.disableLighting();
        GlStateManager.disableCull();

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        float horizLen = MathHelper.sqrt(dx * dx + dz * dz);
        float perpScale = 0.0125F / Math.max(horizLen, 1.0E-4F);
        float perpX = dz * perpScale;
        float perpZ = dx * perpScale;
        int mobLight = mob.getBrightnessForRender();
        int playerLight = player.getBrightnessForRender();
        float width = 0.1F;

        buffer.begin(GL11.GL_TRIANGLE_STRIP, DefaultVertexFormats.POSITION_COLOR);
        for (int segment = 0; segment <= 24; ++segment) {
            addVertexPairAlex(buffer, dx, dy, dz, mobLight, playerLight, mobLight, playerLight, width, width, perpX, perpZ, segment, false);
        }
        for (int segment = 24; segment >= 0; --segment) {
            addVertexPairAlex(buffer, dx, dy, dz, mobLight, playerLight, mobLight, playerLight, width, width, perpX, perpZ, segment, true);
        }
        tessellator.draw();

        GlStateManager.enableCull();
        GlStateManager.enableLighting();
        GlStateManager.enableTexture2D();
        GlStateManager.popMatrix();
    }

    private static Vec3d getRopeHoldPosition(EntityLivingBase entity, float partialTicks) {
        double d0 = 0.4D;
        float yaw = (entity.prevRenderYawOffset + (entity.renderYawOffset - entity.prevRenderYawOffset) * partialTicks) * ((float) Math.PI / 180F);
        double y = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * partialTicks + entity.getEyeHeight() - 0.2D;
        return new Vec3d(
                entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * partialTicks + Math.sin(yaw) * d0,
                y,
                entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * partialTicks - Math.cos(yaw) * d0
        );
    }

    private static void addVertexPairAlex(BufferBuilder buffer, float dx, float dy, float dz, int lightMob, int lightPlayer, int skyMob, int skyPlayer, float width, float width2, float offsetX, float offsetZ, int segment, boolean reverse) {
        float f = (float) segment / 24.0F;
        int blockLight = (int) ((lightMob & 0xFF) + ((lightPlayer & 0xFF) - (lightMob & 0xFF)) * f);
        int skyLight = (int) ((skyMob & 0xFF) + ((skyPlayer & 0xFF) - (skyMob & 0xFF)) * f);
        float r = VINES_COLOR_R;
        float g = VINES_COLOR_G;
        float b = VINES_COLOR_B;
        if (segment % 2 == (reverse ? 1 : 0)) {
            r = VINES_COLOR_R2;
            g = VINES_COLOR_G2;
            b = VINES_COLOR_B2;
        }
        float alongX = dx * f;
        float alongY = dy > 0.0F ? dy * f * f : dy - dy * (1.0F - f) * (1.0F - f);
        float alongZ = dz * f;
        buffer.pos(alongX - offsetX, alongY + width2, alongZ + offsetZ).color(r, g, b, 1.0F).endVertex();
        buffer.pos(alongX + offsetX, alongY + width - width2, alongZ - offsetZ).color(r, g, b, 1.0F).endVertex();
    }

    private void drawVertex(BufferBuilder buffer, int x, int y, int z, float u, float v, int nx, int ny, int nz) {
        buffer.pos(x, y, z).tex(u, v).color(255, 255, 255, 255).normal(nx, ny, nz).endVertex();
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityVineLasso entity) {
        return TEXTURE;
    }
}
