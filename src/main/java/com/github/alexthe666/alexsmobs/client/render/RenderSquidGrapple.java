package com.github.alexthe666.alexsmobs.client.render;

import com.github.alexthe666.alexsmobs.client.model.ModelSquidGrapple;
import com.github.alexthe666.alexsmobs.entity.EntitySquidGrapple;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
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
public class RenderSquidGrapple extends Render<EntitySquidGrapple> {
    private static final ResourceLocation SQUID_TEXTURE = new ResourceLocation("alexsmobs:textures/entity/giant_squid.png");
    private static final ModelSquidGrapple SQUID_MODEL = new ModelSquidGrapple();
    private static final float TENTACLES_COLOR_R = 181F / 255F;
    private static final float TENTACLES_COLOR_G = 87F / 255F;
    private static final float TENTACLES_COLOR_B = 85F / 255F;
    private static final float TENTACLES_COLOR_R2 = 191F / 255F;
    private static final float TENTACLES_COLOR_G2 = 98F / 255F;
    private static final float TENTACLES_COLOR_B2 = 89F / 255F;

    public RenderSquidGrapple(RenderManager renderManagerIn) {
        super(renderManagerIn);
    }

    private static void addVertexPairAlex(BufferBuilder builder, float dx, float dy, float dz, float width, float width2, float offsetX, float offsetZ, int segment, boolean reverse) {
        float f = (float) segment / 24.0F;
        float r = TENTACLES_COLOR_R;
        float g = TENTACLES_COLOR_G;
        float b = TENTACLES_COLOR_B;
        if (segment % 2 == (reverse ? 1 : 0)) {
            r = TENTACLES_COLOR_R2;
            g = TENTACLES_COLOR_G2;
            b = TENTACLES_COLOR_B2;
        }
        float alongX = dx * f;
        float alongY = dy > 0.0F ? dy * f * f : dy - dy * (1.0F - f) * (1.0F - f);
        float alongZ = dz * f;
        builder.pos(alongX - offsetX, alongY + width2, alongZ + offsetZ).color(r, g, b, 1.0F).endVertex();
        builder.pos(alongX + offsetX, alongY + width - width2, alongZ - offsetZ).color(r, g, b, 1.0F).endVertex();
    }

    /**
     * Camera-relative tentacle strip (same approach as {@link RenderVineLasso#renderVine}).
     */
    public static void renderTentacle(Entity mob, float partialTick, EntityLivingBase player, double renderX, double renderY, double renderZ, boolean left) {
        float bodyRot = mob instanceof EntityLivingBase ? ((EntityLivingBase) mob).renderYawOffset : mob.rotationYaw;
        float bodyRot0 = mob instanceof EntityLivingBase ? ((EntityLivingBase) mob).prevRenderYawOffset : mob.prevRotationYaw;
        Vec3d holdPos = getRopeHoldPosition(player, partialTick);
        double bodyAngle = (bodyRot0 + (bodyRot - bodyRot0) * partialTick) * ((float) Math.PI / 180F) + (Math.PI / 2D);
        Vec3d attachOffset = new Vec3d(left ? -0.05F : 0.05F, mob.height * 0.5F, -0.1F);
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
        float perpScale = 0.025F / 2.0F / Math.max(horizLen, 1.0E-4F);
        float perpX = dz * perpScale;
        float perpZ = dx * perpScale;
        float width = 0.2F;

        buffer.begin(GL11.GL_TRIANGLE_STRIP, DefaultVertexFormats.POSITION_COLOR);
        for (int segment = 0; segment <= 24; ++segment) {
            addVertexPairAlex(buffer, dx, dy, dz, width, width, perpX, perpZ, segment, false);
        }
        for (int segment = 24; segment >= 0; --segment) {
            addVertexPairAlex(buffer, dx, dy, dz, width, width, perpX, perpZ, segment, true);
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

    @Override
    public boolean shouldRender(EntitySquidGrapple grapple, net.minecraft.client.renderer.culling.ICamera camera, double d1, double d2, double d3) {
        if (super.shouldRender(grapple, camera, d1, d2, d3)) {
            return true;
        }
        Entity owner = grapple.getOwner();
        return owner != null && (camera != null && camera.isBoundingBoxInFrustum(owner.getEntityBoundingBox()) || owner == Minecraft.getMinecraft().player);
    }

    @Override
    public void doRender(EntitySquidGrapple entityIn, double x, double y, double z, float entityYaw, float partialTicks) {
        GlStateManager.pushMatrix();
        GlStateManager.translate((float) x, (float) y, (float) z);
        GlStateManager.rotate(180.0F - (entityIn.prevRotationYaw + (entityIn.rotationYaw - entityIn.prevRotationYaw) * partialTicks), 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(180.0F + (entityIn.prevRotationPitch + (entityIn.rotationPitch - entityIn.prevRotationPitch) * partialTicks), 1.0F, 0.0F, 0.0F);
        GlStateManager.translate(0, -1.5F, -0.25F);
        this.bindEntityTexture(entityIn);
        SQUID_MODEL.render(entityIn, 0, 0, partialTicks, 0, 0, 0.0625F);
        GlStateManager.popMatrix();
        super.doRender(entityIn, x, y, z, entityYaw, partialTicks);
        Entity owner = entityIn.getOwner();
        if (owner instanceof EntityLivingBase) {
            EntityLivingBase holder = (EntityLivingBase) owner;
            renderTentacle(entityIn, partialTicks, holder, x, y, z, holder.getPrimaryHand() != EnumHandSide.LEFT);
        }
    }

    @Override
    protected ResourceLocation getEntityTexture(EntitySquidGrapple entity) {
        return SQUID_TEXTURE;
    }
}
