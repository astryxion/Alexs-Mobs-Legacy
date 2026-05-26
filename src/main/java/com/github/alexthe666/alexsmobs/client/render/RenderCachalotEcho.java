package com.github.alexthe666.alexsmobs.client.render;

import com.github.alexthe666.alexsmobs.entity.EntityCachalotEcho;
import net.minecraft.client.Minecraft;
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

/**
 * Forge 1.12.2 billboard render for echolocation arcs (1.16 used {@code MatrixStack} / {@code IRenderTypeBuffer}).
 */
@SideOnly(Side.CLIENT)
public class RenderCachalotEcho extends Render<EntityCachalotEcho> {

    private static final ResourceLocation TEXTURE_0 = new ResourceLocation("alexsmobs:textures/entity/cachalot/whale_echo_0.png");
    private static final ResourceLocation TEXTURE_1 = new ResourceLocation("alexsmobs:textures/entity/cachalot/whale_echo_1.png");
    private static final ResourceLocation TEXTURE_2 = new ResourceLocation("alexsmobs:textures/entity/cachalot/whale_echo_2.png");
    private static final ResourceLocation TEXTURE_3 = new ResourceLocation("alexsmobs:textures/entity/cachalot/whale_echo_3.png");

    public RenderCachalotEcho(RenderManager renderManagerIn) {
        super(renderManagerIn);
    }

    @Override
    public void doRender(EntityCachalotEcho entity, double x, double y, double z, float entityYaw, float partialTicks) {
        GlStateManager.pushMatrix();
        GlStateManager.translate((float) x, (float) y + 0.25F, (float) z);

        float yaw = (entity.prevRotationYaw + (entity.rotationYaw - entity.prevRotationYaw) * partialTicks) - 90.0F;
        float pitch = (entity.prevRotationPitch + (entity.rotationPitch - entity.prevRotationPitch) * partialTicks);
        GlStateManager.rotate(yaw, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(pitch, 1.0F, 0.0F, 0.0F);

        int arcs = MathHelper.clamp(MathHelper.floor(entity.ticksExisted / 5F), 1, 4);
        GlStateManager.translate(0.0D, 0.0F, 0.4D);
        for (int i = 0; i < arcs; i++) {
            GlStateManager.pushMatrix();
            GlStateManager.translate(0.0F, 0.0F, -0.5F * (float) i);
            int age = (i + 1) * 5;
            this.renderArc(entity, age, entity.isFasterAnimation());
            GlStateManager.popMatrix();
        }

        GlStateManager.popMatrix();
        super.doRender(entity, x, y, z, entityYaw, partialTicks);
    }

    private void renderArc(EntityCachalotEcho entity, int age, boolean fast) {
        ResourceLocation res = fast ? this.getEchoTextureFaster(age) : this.getEchoTextureByAge(age);
        Minecraft.getMinecraft().renderEngine.bindTexture(res);

        int br = entity.getBrightnessForRender();
        int lx = br % 65536;
        int ly = br / 65536;
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, (float) lx / 1.0F, (float) ly / 1.0F);

        GlStateManager.disableCull();
        GlStateManager.enableAlpha();
        GlStateManager.enableBlend();
        GlStateManager.alphaFunc(GL11.GL_GREATER, 0.003921569F);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        buffer.pos(-1.0D, 0.0D, -1.0D).tex(0.0D, 0.0D).endVertex();
        buffer.pos(-1.0D, 0.0D, 1.0D).tex(0.0D, 1.0D).endVertex();
        buffer.pos(1.0D, 0.0D, 1.0D).tex(1.0D, 1.0D).endVertex();
        buffer.pos(1.0D, 0.0D, -1.0D).tex(1.0D, 0.0D).endVertex();
        tessellator.draw();

        GlStateManager.disableBlend();
        GlStateManager.enableCull();
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityCachalotEcho entity) {
        return TEXTURE_0;
    }

    public ResourceLocation getEchoTextureByAge(int age) {
        if (age < 5) {
            return TEXTURE_0;
        } else if (age < 10) {
            return TEXTURE_1;
        } else if (age < 15) {
            return TEXTURE_2;
        } else {
            return TEXTURE_3;
        }
    }

    public ResourceLocation getEchoTextureFaster(int age) {
        if (age < 3) {
            return TEXTURE_0;
        } else if (age < 6) {
            return TEXTURE_1;
        } else if (age < 9) {
            return TEXTURE_2;
        } else {
            return TEXTURE_3;
        }
    }
}
