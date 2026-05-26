package com.github.alexthe666.alexsmobs.client.render;

import com.github.alexthe666.alexsmobs.client.model.ModelMimicOctopus;
import com.github.alexthe666.alexsmobs.entity.EntityMimicOctopus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderMimicOctopus extends RenderLiving<EntityMimicOctopus> {
    private static final ResourceLocation TEXTURE = new ResourceLocation("alexsmobs:textures/entity/mimic_octopus.png");
    private static final ResourceLocation TEXTURE_OVERLAY = new ResourceLocation("alexsmobs:textures/entity/mimic_octopus_overlay.png");
    private static final ResourceLocation TEXTURE_CREEPER = new ResourceLocation("alexsmobs:textures/entity/mimic_octopus_creeper.png");
    private static final ResourceLocation TEXTURE_GUARDIAN = new ResourceLocation("alexsmobs:textures/entity/mimic_octopus_guardian.png");
    private static final ResourceLocation TEXTURE_PUFFERFISH = new ResourceLocation("alexsmobs:textures/entity/mimic_octopus_pufferfish.png");
    private static final ResourceLocation TEXTURE_MIMICUBE = new ResourceLocation("alexsmobs:textures/entity/mimic_octopus_mimicube.png");
    private static final ResourceLocation GUARDIAN_BEAM_TEXTURE = new ResourceLocation("textures/entity/guardian_beam.png");

    public RenderMimicOctopus(RenderManager renderManagerIn) {
        super(renderManagerIn, new ModelMimicOctopus(), 0.4F);
        this.addLayer(new OverlayLayer(this));
    }

    private static void drawBeamVertex(BufferBuilder buffer, float x, float y, float z, int red, int green, int blue, float u, float v) {
        buffer.pos(x, y, z).tex(u, v).color(red, green, blue, 255).lightmap(0, 240).endVertex();
    }

    @Override
    public void doRender(EntityMimicOctopus entityIn, double x, double y, double z, float entityYaw, float partialTicks) {
        EntityLivingBase laserTarget = entityIn.getGuardianLaser();
        if (laserTarget != null) {
            renderGuardianBeam(entityIn, laserTarget, partialTicks);
        }
        super.doRender(entityIn, x, y, z, entityYaw, partialTicks);
    }

    private void renderGuardianBeam(EntityMimicOctopus entityIn, EntityLivingBase laserTarget, float partialTicks) {
        float f = entityIn.getLaserAttackAnimationScale(partialTicks);
        float f1 = (float) entityIn.world.getTotalWorldTime() + partialTicks;
        float f2 = f1 * 0.5F % 1.0F;
        float f3 = entityIn.getEyeHeight();
        double targetX = laserTarget.lastTickPosX + (laserTarget.posX - laserTarget.lastTickPosX) * partialTicks;
        double targetY = laserTarget.lastTickPosY + (laserTarget.posY - laserTarget.lastTickPosY) * partialTicks + laserTarget.height * 0.5D;
        double targetZ = laserTarget.lastTickPosZ + (laserTarget.posZ - laserTarget.lastTickPosZ) * partialTicks;
        double selfX = entityIn.lastTickPosX + (entityIn.posX - entityIn.lastTickPosX) * partialTicks;
        double selfY = entityIn.lastTickPosY + (entityIn.posY - entityIn.lastTickPosY) * partialTicks + f3;
        double selfZ = entityIn.lastTickPosZ + (entityIn.posZ - entityIn.lastTickPosZ) * partialTicks;
        double dx = targetX - selfX;
        double dy = targetY - selfY;
        double dz = targetZ - selfZ;
        float f4 = (float) (Math.sqrt(dx * dx + dy * dy + dz * dz) + 1.0D);
        double len = Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (len > 0.0D) {
            dx /= len;
            dy /= len;
            dz /= len;
        }
        float f5 = (float) Math.acos(dy);
        float f6 = (float) Math.atan2(dz, dx);
        float f7 = f1 * 0.05F * -1.5F;
        float f8 = f * f;
        int j = 64 + (int) (f8 * 191.0F);
        int k = 32 + (int) (f8 * 191.0F);
        int l = 128 - (int) (f8 * 64.0F);
        float f11 = MathHelper.cos(f7 + 2.3561945F) * 0.282F;
        float f12 = MathHelper.sin(f7 + 2.3561945F) * 0.282F;
        float f13 = MathHelper.cos(f7 + ((float) Math.PI / 4F)) * 0.282F;
        float f14 = MathHelper.sin(f7 + ((float) Math.PI / 4F)) * 0.282F;
        float f15 = MathHelper.cos(f7 + 3.926991F) * 0.282F;
        float f16 = MathHelper.sin(f7 + 3.926991F) * 0.282F;
        float f17 = MathHelper.cos(f7 + 5.4977875F) * 0.282F;
        float f18 = MathHelper.sin(f7 + 5.4977875F) * 0.282F;
        float f19 = MathHelper.cos(f7 + (float) Math.PI) * 0.2F;
        float f20 = MathHelper.sin(f7 + (float) Math.PI) * 0.2F;
        float f21 = MathHelper.cos(f7 + 0.0F) * 0.2F;
        float f22 = MathHelper.sin(f7 + 0.0F) * 0.2F;
        float f23 = MathHelper.cos(f7 + ((float) Math.PI / 2F)) * 0.2F;
        float f24 = MathHelper.sin(f7 + ((float) Math.PI / 2F)) * 0.2F;
        float f25 = MathHelper.cos(f7 + ((float) Math.PI * 1.5F)) * 0.2F;
        float f26 = MathHelper.sin(f7 + ((float) Math.PI * 1.5F)) * 0.2F;
        float f29 = -1.0F + f2;
        float f30 = f4 * 2.5F + f29;
        float f31 = 0.0F;
        if (entityIn.ticksExisted % 2 == 0) {
            f31 = 0.5F;
        }

        GlStateManager.pushMatrix();
        GlStateManager.translate(selfX - this.renderManager.viewerPosX, selfY - this.renderManager.viewerPosY, selfZ - this.renderManager.viewerPosZ);
        GlStateManager.rotate((float) ((((float) Math.PI / 2F) - f6) * (180F / (float) Math.PI)), 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(f5 * (180F / (float) Math.PI), 1.0F, 0.0F, 0.0F);
        Minecraft.getMinecraft().getTextureManager().bindTexture(GUARDIAN_BEAM_TEXTURE);
        GlStateManager.disableLighting();
        GlStateManager.disableCull();
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0F, 240.0F);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
        drawBeamVertex(bufferbuilder, f19, f4, f20, j, k, l, 0.4999F, f30);
        drawBeamVertex(bufferbuilder, f19, 0.0F, f20, j, k, l, 0.4999F, f29);
        drawBeamVertex(bufferbuilder, f21, 0.0F, f22, j, k, l, 0.0F, f29);
        drawBeamVertex(bufferbuilder, f21, f4, f22, j, k, l, 0.0F, f30);
        drawBeamVertex(bufferbuilder, f23, f4, f24, j, k, l, 0.4999F, f30);
        drawBeamVertex(bufferbuilder, f23, 0.0F, f24, j, k, l, 0.4999F, f29);
        drawBeamVertex(bufferbuilder, f25, 0.0F, f26, j, k, l, 0.0F, f29);
        drawBeamVertex(bufferbuilder, f25, f4, f26, j, k, l, 0.0F, f30);
        drawBeamVertex(bufferbuilder, f11, f4, f12, j, k, l, 0.5F, f31 + 0.5F);
        drawBeamVertex(bufferbuilder, f13, f4, f14, j, k, l, 1.0F, f31 + 0.5F);
        drawBeamVertex(bufferbuilder, f17, f4, f18, j, k, l, 1.0F, f31);
        drawBeamVertex(bufferbuilder, f15, f4, f16, j, k, l, 0.5F, f31);
        tessellator.draw();
        GlStateManager.enableLighting();
        GlStateManager.enableCull();
        GlStateManager.popMatrix();
    }

    @Override
    protected void preRenderCallback(EntityMimicOctopus octo, float partialTickTime) {
        GlStateManager.translate(0.0F, -0.02F, 0.0F);
        GlStateManager.scale(0.9F * octo.getRenderScale(), 0.9F * octo.getRenderScale(), 0.9F * octo.getRenderScale());
    }

    @Override
    public boolean shouldRender(EntityMimicOctopus livingEntityIn, ICamera camera, double camX, double camY, double camZ) {
        if (super.shouldRender(livingEntityIn, camera, camX, camY, camZ)) {
            return true;
        }
        if (livingEntityIn.hasGuardianLaser()) {
            EntityLivingBase laserTarget = livingEntityIn.getGuardianLaser();
            if (laserTarget != null) {
                double x1 = livingEntityIn.posX;
                double y1 = livingEntityIn.posY + livingEntityIn.getEyeHeight();
                double z1 = livingEntityIn.posZ;
                double x2 = laserTarget.posX;
                double y2 = laserTarget.posY + laserTarget.height * 0.5D;
                double z2 = laserTarget.posZ;
                return camera.isBoundingBoxInFrustum(new AxisAlignedBB(x1, y1, z1, x2, y2, z2));
            }
        }
        return false;
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityMimicOctopus entity) {
        return TEXTURE;
    }

    @SideOnly(Side.CLIENT)
    class OverlayLayer implements LayerRenderer<EntityMimicOctopus> {
        private final RenderMimicOctopus renderer;

        OverlayLayer(RenderMimicOctopus render) {
            this.renderer = render;
        }

        @Override
        public void doRenderLayer(EntityMimicOctopus entitylivingbaseIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
            float transProgress = entitylivingbaseIn.prevTransProgress + (entitylivingbaseIn.transProgress - entitylivingbaseIn.prevTransProgress) * partialTicks;
            float colorProgress = (entitylivingbaseIn.prevColorShiftProgress + (entitylivingbaseIn.colorShiftProgress - entitylivingbaseIn.prevColorShiftProgress) * partialTicks) * 0.2F;
            float r = 1F;
            float g = 1F;
            float b = 1F;
            float a = 1F;
            float startR = 1.0F;
            float startG = 1.0F;
            float startB = 1.0F;
            float startA = 1.0F;
            float finR = 1.0F;
            float finG = 1.0F;
            float finB = 1.0F;
            float finA = 1.0F;
            if (entitylivingbaseIn.getPrevMimicState() == EntityMimicOctopus.MimicState.OVERLAY) {
                if (entitylivingbaseIn.getPrevMimickedBlock() != null) {
                    int j = OctopusColorRegistry.getBlockColor(entitylivingbaseIn.getPrevMimickedBlock());
                    startR = (float) (j >> 16 & 255) / 255.0F;
                    startG = (float) (j >> 8 & 255) / 255.0F;
                    startB = (float) (j & 255) / 255.0F;
                } else {
                    startA = 0.0F;
                }
            }
            if (entitylivingbaseIn.getMimicState() == EntityMimicOctopus.MimicState.OVERLAY) {
                if (entitylivingbaseIn.getMimickedBlock() != null) {
                    int i = OctopusColorRegistry.getBlockColor(entitylivingbaseIn.getMimickedBlock());
                    finR = (float) (i >> 16 & 255) / 255.0F;
                    finG = (float) (i >> 8 & 255) / 255.0F;
                    finB = (float) (i & 255) / 255.0F;
                } else {
                    finA = 0.0F;
                }
                r = startR + (finR - startR) * colorProgress;
                g = startG + (finG - startG) * colorProgress;
                b = startB + (finB - startB) * colorProgress;
                a = startA + (finA - startA) * colorProgress;
            }
            if (a == 1.0F) {
                a *= 0.9F + 0.1F * (float) Math.sin(entitylivingbaseIn.ticksExisted * 0.1F);
            }
            ModelMimicOctopus model = (ModelMimicOctopus) this.renderer.getMainModel();
            if (entitylivingbaseIn.getPrevMimicState() != null) {
                float alphaPrev = 1 - transProgress * 0.2F;
                if (entitylivingbaseIn.getPrevMimicState() == entitylivingbaseIn.getMimicState()) {
                    alphaPrev *= a;
                }
                this.renderer.bindTexture(getFor(entitylivingbaseIn.getPrevMimicState()));
                AMRenderTypes.beginEntityTranslucent();
                GlStateManager.color(r, g, b, alphaPrev);
                model.render(entitylivingbaseIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
                AMRenderTypes.endEntityTranslucent();
            }
            float alphaCurrent = transProgress * 0.2F;
            this.renderer.bindTexture(getFor(entitylivingbaseIn.getMimicState()));
            AMRenderTypes.beginEntityTranslucent();
            GlStateManager.color(r, g, b, a * alphaCurrent);
            model.render(entitylivingbaseIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
            AMRenderTypes.endEntityTranslucent();
        }

        public ResourceLocation getFor(EntityMimicOctopus.MimicState state) {
            if (state == EntityMimicOctopus.MimicState.CREEPER) {
                return TEXTURE_CREEPER;
            }
            if (state == EntityMimicOctopus.MimicState.GUARDIAN) {
                return TEXTURE_GUARDIAN;
            }
            if (state == EntityMimicOctopus.MimicState.PUFFERFISH) {
                return TEXTURE_PUFFERFISH;
            }
            if (state == EntityMimicOctopus.MimicState.MIMICUBE) {
                return TEXTURE_MIMICUBE;
            }
            return TEXTURE_OVERLAY;
        }

        @Override
        public boolean shouldCombineTextures() {
            return true;
        }
    }
}
