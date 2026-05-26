package com.github.alexthe666.alexsmobs.client.render;

import com.github.alexthe666.alexsmobs.client.model.ModelLaviathan;
import com.github.alexthe666.alexsmobs.client.render.layer.LayerLaviathanOverlays;
import com.github.alexthe666.alexsmobs.entity.EntityLaviathan;
import com.github.alexthe666.alexsmobs.entity.EntityLaviathanPart;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderLaviathan extends RenderLiving<EntityLaviathan> {
    private static final ResourceLocation TEXTURE = new ResourceLocation("alexsmobs:textures/entity/laviathan.png");
    private static final ResourceLocation TEXTURE_OBSIDIAN = new ResourceLocation("alexsmobs:textures/entity/laviathan_obsidian.png");
    private static final float REINS_COLOR_R = 98F / 255F;
    private static final float REINS_COLOR_G = 77F / 255F;
    private static final float REINS_COLOR_B = 52F / 255F;
    private static final float REINS_COLOR_R2 = 58F / 255F;
    private static final float REINS_COLOR_G2 = 40F / 255F;
    private static final float REINS_COLOR_B2 = 34F / 255F;
    public static boolean renderWithoutShaking = false;

    public RenderLaviathan(RenderManager renderManagerIn) {
        super(renderManagerIn, new ModelLaviathan(), 4.0F);
        this.addLayer(new LayerLaviathanOverlays(this));
    }

    @Override
    public void doRender(EntityLaviathan mob, double x, double y, double z, float entityYaw, float partialTick) {
        super.doRender(mob, x, y, z, entityYaw, partialTick);
        Entity entity = mob.getControllingPassenger();
        if (entity != null) {
            GlStateManager.pushMatrix();
            GlStateManager.translate(x, y, z);
            this.renderRein(mob, partialTick, entity, true);
            this.renderRein(mob, partialTick, entity, false);
            GlStateManager.popMatrix();
        }
    }

    protected boolean isShaking(EntityLaviathan entity) {
        return entity.isWet() && !entity.isObsidian() && !renderWithoutShaking;
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityLaviathan entity) {
        return entity.isObsidian() ? TEXTURE_OBSIDIAN : TEXTURE;
    }

    @Override
    public boolean shouldRender(EntityLaviathan livingEntity, ICamera camera, double camX, double camY, double camZ) {
        if (super.shouldRender(livingEntity, camera, camX, camY, camZ)) {
            return true;
        }
        if (camera == null) {
            return false;
        }
        for (EntityLaviathanPart part : livingEntity.allParts) {
            if (part != null && part.getEntityBoundingBox() != null && camera.isBoundingBoxInFrustum(part.getEntityBoundingBox())) {
                return true;
            }
        }
        return false;
    }

    private float getHeadShakeForReins(EntityLaviathan mob, float partialTick) {
        float hh1 = mob.prevHeadHeight;
        float hh2 = mob.getHeadHeight();
        float rawHeadHeight = (hh1 + (hh2 - hh1) * partialTick) / 3F;
        float clampedNeckRot = MathHelper.clamp(-rawHeadHeight, -1, 1);
        float headStillProgress = 1F - Math.abs(clampedNeckRot);
        float swim = mob.prevSwimProgress + (mob.swimProgress - mob.prevSwimProgress) * partialTick;
        float limbSwingAmount = mob.limbSwingAmount;
        float swing = mob.limbSwing + partialTick;
        float swingAmount = limbSwingAmount * swim * 0.2F * headStillProgress;
        float swimSpeed = mob.swimProgress >= 5F ? 0.3F : 0.9F;
        float swimDegree = 0.5F + swim * 0.05F;
        float boxOffset = (float) (-21 * 3.141592653589793D / (double) (2 * 3));
        float moveScale = 1;
        return 1.3F * MathHelper.cos(swing * swimSpeed * moveScale + boxOffset * (float) 2) * swingAmount * swimDegree * moveScale;
    }

    private float getHeadBobForReins(EntityLaviathan mob, float partialTick) {
        float swing = mob.ticksExisted + partialTick;
        float swingAmount = 1.0F;
        float idleSpeed = 0.04f;
        float idleDegree = 0.3f;
        float boxOffset = (float) (9 * 3.141592653589793D / (double) (2 * 3));
        float moveScale = 1;
        return 0.8F * MathHelper.cos(swing * idleSpeed * moveScale + boxOffset * (float) 2) * swingAmount * idleDegree * moveScale;
    }

    private void renderRein(EntityLaviathan mob, float partialTick, Entity rider, boolean left) {
        Entity head = mob.headPart;
        if (head == null) {
            return;
        }
        float limbSwingAmount = mob.limbSwingAmount;
        float shake = getHeadShakeForReins(mob, partialTick);
        float headYaw = Math.abs(mob.getHeadYaw(partialTick)) / 50F;
        float headPitch = 1F - Math.abs((mob.prevHeadHeight + (mob.getHeadHeight() - mob.prevHeadHeight) * partialTick) / 3F);
        float yawAdd = (1F - headYaw) * 0.4F * (1F - limbSwingAmount * 0.7F) - headPitch * 0.2F;
        Vec3d vec3 = rider instanceof EntityLivingBase ? getReinPosition((EntityLivingBase) rider, partialTick, left) : new Vec3d(rider.posX, rider.posY + rider.height * 0.5D, rider.posZ);
        double d0 = (double) (lerp(partialTick, mob.renderYawOffset, mob.prevRenderYawOffset) * 0.017453292F) + (Math.PI / 2D);
        Vec3d vec31 = new Vec3d((left ? -0.05F - yawAdd : 0.05F + yawAdd) + shake, 0.45F - headYaw * 0.2F + getHeadBobForReins(mob, partialTick), 0.1F);
        double d1 = Math.cos(d0) * vec31.z + Math.sin(d0) * vec31.x;
        double d2 = Math.sin(d0) * vec31.z - Math.cos(d0) * vec31.x;
        double d3 = head.prevPosX + (head.posX - head.prevPosX) * partialTick + d1;
        double d4 = head.prevPosY + (head.posY - head.prevPosY) * partialTick + vec31.y;
        double d5 = head.prevPosZ + (head.posZ - head.prevPosZ) * partialTick + d2;
        float f = (float) (vec3.x - d3);
        float f1 = (float) (vec3.y - d4);
        float f2 = (float) (vec3.z - d5);
        float f4 = (float) (MathHelper.fastInvSqrt(f * f + f2 * f2) * 0.025F / 2.0F);
        float f5 = f2 * f4;
        float f6 = f * f4;
        float width = 0.05F;
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        GlStateManager.disableLighting();
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        bufferbuilder.begin(5, DefaultVertexFormats.POSITION_COLOR);
        for (int i1 = 0; i1 <= 24; ++i1) {
            addReinVertex(bufferbuilder, f, f1, f2, width, width, f5, f6, i1, false);
        }
        for (int j1 = 24; j1 >= 0; --j1) {
            addReinVertex(bufferbuilder, f, f1, f2, width, width, f5, f6, j1, true);
        }
        tessellator.draw();
        GlStateManager.enableTexture2D();
        GlStateManager.enableLighting();
        GlStateManager.disableBlend();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    }

    private static void addReinVertex(BufferBuilder buffer, float dx, float dy, float dz, float width, float height, float offsetX, float offsetZ, int segment, boolean reverse) {
        float f = (float) segment / 24.0F;
        float r = segment % 2 == (reverse ? 1 : 0) ? REINS_COLOR_R2 : REINS_COLOR_R;
        float g = segment % 2 == (reverse ? 1 : 0) ? REINS_COLOR_G2 : REINS_COLOR_G;
        float b = segment % 2 == (reverse ? 1 : 0) ? REINS_COLOR_B2 : REINS_COLOR_B;
        float f5 = dx * f;
        float f6 = dy > 0.0F ? dy * f * f : dy - dy * (1.0F - f) * (1.0F - f);
        float f7 = dz * f;
        buffer.pos(f5 - offsetX, f6 + height, f7 + offsetZ).color(r, g, b, 1.0F).endVertex();
        buffer.pos(f5 + offsetX, f6 + width - height, f7 - offsetZ).color(r, g, b, 1.0F).endVertex();
    }

    private Vec3d getReinPosition(EntityLivingBase entity, float partialTick, boolean left) {
        double d0 = 0.4D * (left ? -1.0D : 1.0D);
        float f1 = lerp(partialTick, entity.prevRenderYawOffset, entity.renderYawOffset) * 0.017453292F;
        double d5 = entity.height - 1.0D;
        double d6 = entity.isSneaking() ? -0.2D : 0.07D;
        return new Vec3d(entity.prevPosX + (entity.posX - entity.prevPosX) * partialTick, entity.prevPosY + (entity.posY - entity.prevPosY) * partialTick, entity.prevPosZ + (entity.posZ - entity.prevPosZ) * partialTick)
                .add(new Vec3d(d0 * -MathHelper.sin(f1), d5 + d6, d0 * MathHelper.cos(f1)));
    }

    private static float lerp(float partialTick, float from, float to) {
        return from + (to - from) * partialTick;
    }
}
