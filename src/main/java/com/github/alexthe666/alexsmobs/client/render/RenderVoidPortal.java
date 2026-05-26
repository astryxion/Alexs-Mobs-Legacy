package com.github.alexthe666.alexsmobs.client.render;

import com.github.alexthe666.alexsmobs.entity.EntityVoidPortal;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class RenderVoidPortal extends Render<EntityVoidPortal> {

    private static final ResourceLocation TEXTURE_0 = new ResourceLocation("alexsmobs:textures/entity/portal/portal_idle_0.png");
    private static final ResourceLocation TEXTURE_1 = new ResourceLocation("alexsmobs:textures/entity/portal/portal_idle_1.png");
    private static final ResourceLocation TEXTURE_2 = new ResourceLocation("alexsmobs:textures/entity/portal/portal_idle_2.png");
    private static final ResourceLocation[] TEXTURE_PROGRESS = new ResourceLocation[10];

    public RenderVoidPortal(RenderManager renderManagerIn) {
        super(renderManagerIn);
        for (int i = 0; i < 10; i++) {
            TEXTURE_PROGRESS[i] = new ResourceLocation("alexsmobs:textures/entity/portal/portal_grow_" + i + ".png");
        }
    }

    @Override
    public void doRender(EntityVoidPortal entityIn, double x, double y, double z, float entityYaw, float partialTicks) {
        GlStateManager.pushMatrix();
        GlStateManager.translate((float) x + 0.5F, (float) y, (float) z + 0.5F);
        applyFacingRotation(entityIn.getAttachmentFacing().getOpposite());
        ResourceLocation tex;
        if (entityIn.getLifespan() < 20) {
            tex = getGrowingTexture((int) ((entityIn.getLifespan() * 0.5F) % 10));
        } else if (entityIn.ticksExisted < 20) {
            tex = getGrowingTexture((int) ((entityIn.ticksExisted * 0.5F) % 10));
        } else {
            tex = getIdleTexture(entityIn.ticksExisted % 9);
        }
        GlStateManager.scale(2.0F, 2.0F, 2.0F);
        renderPortalQuad(entityIn, tex);
        GlStateManager.popMatrix();
        super.doRender(entityIn, x, y, z, entityYaw, partialTicks);
    }

    private void applyFacingRotation(EnumFacing facing) {
        int horizontal = facing.getHorizontalIndex();
        GlStateManager.rotate(horizontal * 90.0F, 0.0F, 1.0F, 0.0F);
        if (facing == EnumFacing.UP) {
            GlStateManager.rotate(-90.0F, 1.0F, 0.0F, 0.0F);
        } else if (facing == EnumFacing.DOWN) {
            GlStateManager.rotate(90.0F, 1.0F, 0.0F, 0.0F);
        }
    }

    private void renderPortalQuad(EntityVoidPortal entity, ResourceLocation res) {
        Minecraft.getMinecraft().renderEngine.bindTexture(res);
        int br = entity.getBrightnessForRender();
        int lx = br % 65536;
        int ly = br / 65536;
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, (float) lx / 1.0F, (float) ly / 1.0F);

        GlStateManager.disableCull();
        GlStateManager.enableAlpha();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
        GlStateManager.alphaFunc(GL11.GL_GREATER, 0.003921569F);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR_NORMAL);
        buffer.pos(-1.0D, 0.0D, -1.0D).tex(0.0D, 0.0D).color(255, 255, 255, 255).normal(1, 0, 0).endVertex();
        buffer.pos(-1.0D, 0.0D, 1.0D).tex(0.0D, 1.0D).color(255, 255, 255, 255).normal(1, 0, 0).endVertex();
        buffer.pos(1.0D, 0.0D, 1.0D).tex(1.0D, 1.0D).color(255, 255, 255, 255).normal(1, 0, 0).endVertex();
        buffer.pos(1.0D, 0.0D, -1.0D).tex(1.0D, 0.0D).color(255, 255, 255, 255).normal(1, 0, 0).endVertex();
        tessellator.draw();

        GlStateManager.disableBlend();
        GlStateManager.enableCull();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityVoidPortal entity) {
        return TEXTURE_0;
    }

    public ResourceLocation getIdleTexture(int age) {
        if (age < 3) {
            return TEXTURE_0;
        } else if (age < 6) {
            return TEXTURE_1;
        } else if (age < 10) {
            return TEXTURE_2;
        } else {
            return TEXTURE_0;
        }
    }

    public ResourceLocation getGrowingTexture(int age) {
        return TEXTURE_PROGRESS[MathHelper.clamp(age, 0, 9)];
    }
}
