package com.github.alexthe666.alexsmobs.client.render;

import com.github.alexthe666.alexsmobs.client.model.ModelMungus;
import com.github.alexthe666.alexsmobs.entity.EntityMungus;
import net.minecraft.block.state.IBlockState;
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
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class RenderMungus extends RenderLiving<EntityMungus> {
    private static final ResourceLocation TEXTURE = new ResourceLocation("alexsmobs:textures/entity/mungus.png");
    private static final ResourceLocation BEAM_TEXTURE = new ResourceLocation("alexsmobs:textures/entity/mungus_beam.png");
    private static final ResourceLocation TEXTURE_BEAM_OVERLAY = new ResourceLocation("alexsmobs:textures/entity/mungus_beam_overlay.png");
    private static final ResourceLocation TEXTURE_SACK_OVERLAY = new ResourceLocation("alexsmobs:textures/entity/mungus_sack.png");
    private static final ResourceLocation TEXTURE_SHOES = new ResourceLocation("alexsmobs:textures/entity/mungus_shoes.png");

    public RenderMungus(RenderManager renderManagerIn) {
        super(renderManagerIn, new ModelMungus(0), 0.5F);
        this.addLayer(new MungusSackLayer(this));
        this.addLayer(new MungusMushroomLayer(this));
    }

    private static void addBeamVertex(BufferBuilder buffer, float x, float y, float z, int r, int g, int b, float u, float v) {
        buffer.pos(x, y, z).tex(u, v).color(r, g, b, 255).lightmap(0, 240).normal(0.0F, 1.0F, 0.0F).endVertex();
    }

    @Override
    protected void applyRotations(EntityMungus entityLiving, float ageInTicks, float rotationYaw, float partialTicks) {
        if (entityLiving.deathTime > 0) {
            GlStateManager.rotate(180.0F - rotationYaw, 0.0F, 1.0F, 0.0F);
            float f = ((float) entityLiving.deathTime + partialTicks - 1.0F) / 20.0F * 1.6F;
            f = MathHelper.sqrt(f);
            if (f > 1.0F) {
                f = 1.0F;
            }
            GlStateManager.rotate(f * this.getDeathMaxRotation(entityLiving), 1.0F, 0.0F, 0.0F);
        } else {
            super.applyRotations(entityLiving, ageInTicks, rotationYaw, partialTicks);
        }
    }

    @Override
    protected float getDeathMaxRotation(EntityMungus entity) {
        return 0F;
    }

    @Override
    protected void preRenderCallback(EntityMungus entitylivingbaseIn, float partialTickTime) {
        String s = entitylivingbaseIn.getName();
        if (s != null && s.toLowerCase().contains("drip")) {
            GlStateManager.translate(0F, entitylivingbaseIn.isChild() ? -0.075F : -0.15F, 0F);
        }
    }

    @Override
    public boolean shouldRender(EntityMungus livingEntityIn, ICamera camera, double camX, double camY, double camZ) {
        if (super.shouldRender(livingEntityIn, camera, camX, camY, camZ)) {
            return true;
        }
        if (livingEntityIn.getBeamTarget() != null) {
            BlockPos pos = livingEntityIn.getBeamTarget();
            Vec3d vector3d = new Vec3d(pos.getX(), pos.getY(), pos.getZ());
            Vec3d vector3dCorner = new Vec3d(pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1);
            Vec3d vector3d1 = this.getPosition(livingEntityIn, livingEntityIn.getEyeHeight(), 1.0F);
            return camera.isBoundingBoxInFrustum(new AxisAlignedBB(vector3d1.x, vector3d1.y, vector3d1.z, vector3d.x, vector3d.y, vector3d.z))
                    || camera.isBoundingBoxInFrustum(new AxisAlignedBB(vector3d1.x, vector3d1.y, vector3d1.z, vector3dCorner.x, vector3dCorner.y, vector3dCorner.z));
        }
        return false;
    }

    private Vec3d getPosition(EntityLivingBase entityLivingBaseIn, double eyeHeight, float partialTicks) {
        double d0 = entityLivingBaseIn.lastTickPosX + (entityLivingBaseIn.posX - entityLivingBaseIn.lastTickPosX) * partialTicks;
        double d1 = entityLivingBaseIn.lastTickPosY + (entityLivingBaseIn.posY - entityLivingBaseIn.lastTickPosY) * partialTicks + eyeHeight;
        double d2 = entityLivingBaseIn.lastTickPosZ + (entityLivingBaseIn.posZ - entityLivingBaseIn.lastTickPosZ) * partialTicks;
        return new Vec3d(d0, d1, d2);
    }

    @Override
    public void doRender(EntityMungus entityIn, double x, double y, double z, float entityYaw, float partialTicks) {
        super.doRender(entityIn, x, y, z, entityYaw, partialTicks);
        BlockPos target = entityIn.getBeamTarget();
        if (target != null) {
            float f1 = (float) entityIn.world.getTotalWorldTime() + partialTicks;
            float f2 = -1.0F * (f1 * 0.15F % 1.0F);
            float f3 = 1.13F;
            if (entityIn.isChild()) {
                f3 = 0.555F;
            }

            Vec3d targetVec = new Vec3d(target.getX() + 0.5D, target.getY() + 0.15D + 0.5D, target.getZ() + 0.5D);
            Vec3d entityVec = this.getPosition(entityIn, f3, partialTicks);
            Vec3d delta = targetVec.subtract(entityVec);
            float f4 = (float) delta.lengthVector();
            delta = delta.normalize();
            float f5 = (float) Math.acos(delta.y);
            float f6 = (float) Math.atan2(delta.z, delta.x);

            this.bindTexture(BEAM_TEXTURE);
            GlStateManager.depthMask(false);
            GlStateManager.disableLighting();
            GlStateManager.disableCull();
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0F, 240.0F);

            GlStateManager.pushMatrix();
            GlStateManager.translate((float) x, (float) y + f3, (float) z);
            GlStateManager.rotate((float) Math.toDegrees(((float) Math.PI / 2F) - f6), 0.0F, 1.0F, 0.0F);
            GlStateManager.rotate((float) Math.toDegrees(f5), 1.0F, 0.0F, 0.0F);

            int j = 255;
            int k = 255;
            int l = 255;
            float f29 = -1.0F + f2;
            float f30 = f4 * 0.5F + f29;
            float f11 = MathHelper.cos(0 + 2.3561945F) * 0.8F;
            float f12 = MathHelper.sin(0 + 2.3561945F) * 0.8F;
            float f13 = MathHelper.cos(0 + ((float) Math.PI / 4F)) * 0.8F;
            float f14 = MathHelper.sin(0 + ((float) Math.PI / 4F)) * 0.8F;
            float f15 = MathHelper.cos(0 + 3.926991F) * 0.8F;
            float f16 = MathHelper.sin(0 + 3.926991F) * 0.8F;
            float f17 = MathHelper.cos(0 + 5.4977875F) * 0.8F;
            float f18 = MathHelper.sin(0 + 5.4977875F) * 0.8F;
            float f19 = MathHelper.cos(0 + (float) Math.PI) * 0.4F;
            float f20 = MathHelper.sin(0 + (float) Math.PI) * 0.4F;
            float f21 = MathHelper.cos(0 + 0.0F) * 0.4F;
            float f22 = MathHelper.sin(0 + 0.0F) * 0.4F;
            float f23 = MathHelper.cos(0 + ((float) Math.PI / 2F)) * 0.4F;
            float f24 = MathHelper.sin(0 + ((float) Math.PI / 2F)) * 0.4F;
            float f25 = MathHelper.cos(0 + ((float) Math.PI * 1.5F)) * 0.4F;
            float f26 = MathHelper.sin(0 + ((float) Math.PI * 1.5F)) * 0.4F;
            float f31 = 0.0F;
            if (entityIn.ticksExisted % 4 > 1) {
                f31 = 0.5F;
            }

            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder bufferbuilder = tessellator.getBuffer();
            bufferbuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_LMAP_COLOR);
            addBeamVertex(bufferbuilder, f19, f4, f20, j, k, l, 0.4999F, f30);
            addBeamVertex(bufferbuilder, f19, 0.0F, f20, j, k, l, 0.4999F, f29);
            addBeamVertex(bufferbuilder, f21, 0.0F, f22, j, k, l, 0.0F, f29);
            addBeamVertex(bufferbuilder, f21, f4, f22, j, k, l, 0.0F, f30);
            addBeamVertex(bufferbuilder, f23, f4, f24, j, k, l, 0.4999F, f30);
            addBeamVertex(bufferbuilder, f23, 0.0F, f24, j, k, l, 0.4999F, f29);
            addBeamVertex(bufferbuilder, f25, 0.0F, f26, j, k, l, 0.0F, f29);
            addBeamVertex(bufferbuilder, f25, f4, f26, j, k, l, 0.0F, f30);
            addBeamVertex(bufferbuilder, f11, f4, f12, j, k, l, 0.5F, f31 + 0.5F);
            addBeamVertex(bufferbuilder, f13, f4, f14, j, k, l, 1.0F, f31 + 0.5F);
            addBeamVertex(bufferbuilder, f17, f4, f18, j, k, l, 1.0F, f31);
            addBeamVertex(bufferbuilder, f15, f4, f16, j, k, l, 0.5F, f31);
            tessellator.draw();
            GlStateManager.popMatrix();

            GlStateManager.enableLighting();
            GlStateManager.enableCull();
            GlStateManager.disableBlend();
            GlStateManager.depthMask(true);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        }
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityMungus entity) {
        return TEXTURE;
    }

    @SideOnly(Side.CLIENT)
    private class MungusSackLayer implements LayerRenderer<EntityMungus> {
        private final RenderMungus renderer;

        MungusSackLayer(RenderMungus renderer) {
            this.renderer = renderer;
        }

        @Override
        public void doRenderLayer(EntityMungus entitylivingbaseIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
            this.renderer.bindTexture(TEXTURE_SACK_OVERLAY);
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
            GlStateManager.disableLighting();
            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0F, 240.0F);
            float alpha = 0.75F + (MathHelper.cos(ageInTicks * 0.2F) + 1F) * 0.125F;
            GlStateManager.color(1.0F, 1.0F, 1.0F, alpha);
            this.renderer.getMainModel().render(entitylivingbaseIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
            if (entitylivingbaseIn.getBeamTarget() != null) {
                this.renderer.bindTexture(TEXTURE_BEAM_OVERLAY);
                float beamAlpha = 0.75F + (MathHelper.cos(ageInTicks) + 1F) * 0.125F;
                GlStateManager.color(1.0F, 1.0F, 1.0F, beamAlpha);
                this.renderer.getMainModel().render(entitylivingbaseIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
            }
            String s = entitylivingbaseIn.getName();
            if (s != null && s.toLowerCase().contains("drip")) {
                this.renderer.bindTexture(TEXTURE_SHOES);
                GlStateManager.enableAlpha();
                GlStateManager.alphaFunc(GL11.GL_GREATER, 0.1F);
                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                ModelMungus model = (ModelMungus) this.renderer.getMainModel();
                model.renderShoes();
                this.renderer.getMainModel().render(entitylivingbaseIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
                model.postRenderShoes();
            }
            GlStateManager.enableLighting();
            GlStateManager.disableBlend();
            GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            int light = entitylivingbaseIn.getBrightnessForRender();
            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, light % 65536, light / 65536);
        }

        @Override
        public boolean shouldCombineTextures() {
            return false;
        }
    }

    @SideOnly(Side.CLIENT)
    private class MungusMushroomLayer implements LayerRenderer<EntityMungus> {
        private final RenderMungus renderer;

        MungusMushroomLayer(RenderMungus renderer) {
            this.renderer = renderer;
        }

        @Override
        public void doRenderLayer(EntityMungus entitylivingbaseIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
            IBlockState blockstate = entitylivingbaseIn.getMushroomState();
            if (blockstate == null || entitylivingbaseIn.getMushroomCount() <= 0) {
                return;
            }
            boolean altOrder = entitylivingbaseIn.isAltOrderMushroom();
            int mushroomCount = entitylivingbaseIn.getMushroomCount();
            net.minecraft.item.ItemStack mushroom = new net.minecraft.item.ItemStack(net.minecraft.item.Item.getItemFromBlock(blockstate.getBlock()), 1, blockstate.getBlock().getMetaFromState(blockstate));
            if (mushroom.isEmpty()) {
                return;
            }
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            GlStateManager.enableRescaleNormal();
            GlStateManager.enableCull();
            GlStateManager.disableBlend();
            int light = entitylivingbaseIn.getBrightnessForRender();
            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, light % 65536, light / 65536);
            GlStateManager.pushMatrix();
            if (entitylivingbaseIn.isChild()) {
                GlStateManager.scale(0.5F, 0.5F, 0.5F);
                GlStateManager.translate(0.0F, 1.5F, 0.0F);
            }
            GlStateManager.pushMatrix();
            translateToBody(scale);
            if (mushroomCount == 1 && !altOrder || mushroomCount >= 2) {
                renderMushroomItem(mushroom, 0.2F, -1.4F, 0.15F, 0.0F);
            }
            if (mushroomCount == 1 && altOrder || mushroomCount >= 2) {
                renderMushroomItem(mushroom, -0.2F, -1.5F, -0.2F, 0.0F);
            }
            if (mushroomCount >= 3) {
                renderMushroomItem(mushroom, 0.76F, -0.4F, 0.1F, 90.0F);
            }
            if (mushroomCount >= 4) {
                renderMushroomItem(mushroom, -0.76F, -1.0F, 0.1F, -60.0F);
            }
            if (mushroomCount >= 5) {
                renderMushroomItem(mushroom, -0.76F, -0.1F, 0.1F, -100.0F);
            }
            GlStateManager.popMatrix();
            GlStateManager.popMatrix();
            GlStateManager.disableCull();
            GlStateManager.disableRescaleNormal();
        }

        private void renderMushroomItem(net.minecraft.item.ItemStack mushroom, float x, float y, float z, float roll) {
            GlStateManager.pushMatrix();
            GlStateManager.translate(x, y, z);
            if (roll != 0.0F) {
                GlStateManager.rotate(roll, 0.0F, 0.0F, 1.0F);
            }
            GlStateManager.scale(0.85F, 0.85F, 0.85F);
            Minecraft.getMinecraft().getRenderItem().renderItem(mushroom, net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType.GROUND);
            GlStateManager.popMatrix();
        }

        protected void translateToBody(float scale) {
            ModelMungus model = (ModelMungus) this.renderer.getMainModel();
            model.root.postRender(scale);
            model.body.postRender(scale);
        }

        @Override
        public boolean shouldCombineTextures() {
            return false;
        }
    }
}
