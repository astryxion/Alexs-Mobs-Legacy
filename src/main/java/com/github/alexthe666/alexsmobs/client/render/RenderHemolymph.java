package com.github.alexthe666.alexsmobs.client.render;

import com.github.alexthe666.alexsmobs.entity.EntityHemolymph;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class RenderHemolymph extends Render<EntityHemolymph> {

    private static final ResourceLocation TEXTURE = new ResourceLocation("alexsmobs:textures/entity/hemolymph.png");

    public RenderHemolymph(RenderManager renderManagerIn) {
        super(renderManagerIn);
    }

    @Override
    public void doRender(EntityHemolymph entity, double x, double y, double z, float entityYaw, float partialTicks) {
        GlStateManager.pushMatrix();
        GlStateManager.translate((float) x, (float) y, (float) z);
        GlStateManager.rotate((entity.prevRotationYaw + (entity.rotationYaw - entity.prevRotationYaw) * partialTicks) - 90.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate((entity.prevRotationPitch + (entity.rotationPitch - entity.prevRotationPitch) * partialTicks), 0.0F, 0.0F, 1.0F);
        float shake = 0.0F;
        if (shake > 0.0F) {
            float shakeRot = -MathHelper.sin(shake * 3.0F) * shake;
            GlStateManager.rotate(shakeRot, 0.0F, 0.0F, 1.0F);
        }
        GlStateManager.rotate(45.0F, 1.0F, 0.0F, 0.0F);
        GlStateManager.scale(0.05625F, 0.05625F, 0.05625F);
        GlStateManager.translate(-4.0D, 0.0D, 0.0D);
        this.bindTexture(TEXTURE);

        int br = entity.getBrightnessForRender();
        int lx = br % 65536;
        int ly = br / 65536;
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, (float) lx / 1.0F, (float) ly / 1.0F);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR_NORMAL);
        this.drawVertex(buffer, -7, -2, -2, 0.0F, 0.15625F, -1, 0, 0);
        this.drawVertex(buffer, -7, -2, 2, 0.15625F, 0.15625F, -1, 0, 0);
        this.drawVertex(buffer, -7, 2, 2, 0.15625F, 0.3125F, -1, 0, 0);
        this.drawVertex(buffer, -7, 2, -2, 0.0F, 0.3125F, -1, 0, 0);
        this.drawVertex(buffer, -7, 2, -2, 0.0F, 0.15625F, 1, 0, 0);
        this.drawVertex(buffer, -7, 2, 2, 0.15625F, 0.15625F, 1, 0, 0);
        this.drawVertex(buffer, -7, -2, 2, 0.15625F, 0.3125F, 1, 0, 0);
        this.drawVertex(buffer, -7, -2, -2, 0.0F, 0.3125F, 1, 0, 0);

        for (int i = 0; i < 4; ++i) {
            GlStateManager.rotate(90.0F, 1.0F, 0.0F, 0.0F);
            this.drawVertex(buffer, -8, -2, 0, 0.0F, 0.0F, 0, 1, 0);
            this.drawVertex(buffer, 8, -2, 0, 0.5F, 0.0F, 0, 1, 0);
            this.drawVertex(buffer, 8, 2, 0, 0.5F, 0.15625F, 0, 1, 0);
            this.drawVertex(buffer, -8, 2, 0, 0.0F, 0.15625F, 0, 1, 0);
        }

        tessellator.draw();
        GlStateManager.popMatrix();
    }

    private void drawVertex(BufferBuilder buffer, int x, int y, int z, float u, float v, int nx, int ny, int nz) {
        buffer.pos(x, y, z).tex(u, v).color(255, 255, 255, 255).normal(nx, ny, nz).endVertex();
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityHemolymph entity) {
        return TEXTURE;
    }
}
