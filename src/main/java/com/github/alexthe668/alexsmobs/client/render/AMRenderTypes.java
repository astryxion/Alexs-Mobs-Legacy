package com.github.alexthe668.alexsmobs.client.render;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

/**
 * 1.12 equivalent of 1.16 {@code RenderType} helpers used by Alex's Mobs render layers.
 * Call {@code begin*} before drawing and {@code end*} after.
 */
@SideOnly(Side.CLIENT)
public final class AMRenderTypes {

    private AMRenderTypes() {
    }

    public static void beginEyesNoCull() {
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.disableLighting();
        GlStateManager.disableCull();
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0F, 240.0F);
    }

    public static void endEyesNoCull() {
        GlStateManager.enableLighting();
        GlStateManager.enableCull();
        GlStateManager.disableBlend();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    }

    public static void beginEyesFlickering(float alpha) {
        beginEyesNoCull();
        GlStateManager.color(1.0F, 1.0F, 1.0F, alpha);
    }

    public static void endEyesFlickering() {
        endEyesNoCull();
    }

    public static void beginTransparentMimicube() {
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        GlStateManager.enableLighting();
        GlStateManager.disableCull();
    }

    public static void endTransparentMimicube() {
        GlStateManager.enableCull();
        GlStateManager.disableBlend();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    }

    public static void beginRainbowGlint() {
        setupRainbowTexturing(1.2F, 4L);
        beginGlintState();
    }

    public static void beginCombJellyRainbowGlint() {
        setupRainbowTexturing(2.0F, 16L);
        beginGlintState();
    }

    public static void beginWeezerRainbowGlint() {
        setupRainbowTexturing2(7.0F, 16L);
        beginGlintState();
    }

    public static void beginBrazilRainbowGlint() {
        setupRainbowTexturing2(5.0F, 14L);
        beginGlintState();
    }

    public static void endRainbowGlint() {
        GlStateManager.depthFunc(GL11.GL_LEQUAL);
        GlStateManager.depthMask(true);
        GlStateManager.enableLighting();
        GlStateManager.enableCull();
        GlStateManager.disableBlend();
        GlStateManager.matrixMode(5890);
        GlStateManager.popMatrix();
        GlStateManager.matrixMode(5888);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    }

    public static void beginFrilledSharkTeeth() {
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.disableLighting();
        GlStateManager.disableCull();
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0F, 240.0F);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    }

    public static void endFrilledSharkTeeth() {
        GlStateManager.enableLighting();
        GlStateManager.enableCull();
        GlStateManager.disableBlend();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    }

    public static void beginFrilledSharkTeethGlint() {
        setupFrilledSharkGlintTexturing(8.0F);
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_COLOR, GlStateManager.DestFactor.ONE, GlStateManager.SourceFactor.ZERO, GlStateManager.DestFactor.ONE);
        GlStateManager.disableLighting();
        GlStateManager.disableCull();
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0F, 240.0F);
    }

    public static void endFrilledSharkTeethGlint() {
        GlStateManager.enableLighting();
        GlStateManager.enableCull();
        GlStateManager.disableBlend();
        GlStateManager.matrixMode(5890);
        GlStateManager.popMatrix();
        GlStateManager.matrixMode(5888);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    }

    public static void beginGhost() {
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        GlStateManager.disableLighting();
        GlStateManager.disableCull();
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0F, 240.0F);
    }

    public static void endGhost() {
        GlStateManager.enableLighting();
        GlStateManager.enableCull();
        GlStateManager.disableBlend();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    }

    public static void beginEntityCutoutNoCull() {
        GlStateManager.enableAlpha();
        GlStateManager.disableCull();
    }

    public static void endEntityCutoutNoCull() {
        GlStateManager.enableCull();
    }

    public static void beginEntityTranslucent() {
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.disableCull();
    }

    public static void endEntityTranslucent() {
        GlStateManager.enableCull();
        GlStateManager.disableBlend();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    }

    public static void beginSnappingTurtleMoss(float alpha) {
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        GlStateManager.enableLighting();
        GlStateManager.disableCull();
        GlStateManager.color(1.0F, 1.0F, 1.0F, alpha);
    }

    public static void endSnappingTurtleMoss() {
        GlStateManager.enableCull();
        GlStateManager.disableBlend();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    }

    public static void beginSpectreBones() {
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.disableLighting();
        GlStateManager.disableCull();
    }

    public static void endSpectreBones() {
        GlStateManager.enableLighting();
        GlStateManager.enableCull();
        GlStateManager.disableBlend();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    }

    public static void beginMungusBeam() {
        beginGhost();
    }

    public static void endMungusBeam() {
        endGhost();
    }

    public static void beginWormEyesAlpha() {
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        GlStateManager.disableLighting();
        GlStateManager.disableCull();
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0F, 240.0F);
    }

    public static void endWormEyesAlpha() {
        endEyesNoCull();
    }

    private static void beginGlintState() {
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_COLOR, GlStateManager.DestFactor.ONE, GlStateManager.SourceFactor.ZERO, GlStateManager.DestFactor.ONE);
        GlStateManager.disableLighting();
        GlStateManager.disableCull();
        GlStateManager.depthMask(false);
        GlStateManager.depthFunc(GL11.GL_EQUAL);
    }

    private static void setupRainbowTexturing(float scaleIn, long time) {
        GlStateManager.matrixMode(5890);
        GlStateManager.pushMatrix();
        GlStateManager.loadIdentity();
        long i = System.currentTimeMillis() * time;
        float f1 = (float) (i % 30000L) / 30000.0F;
        GlStateManager.translate(0.0F, f1, 0.0F);
        GlStateManager.scale(scaleIn, scaleIn, scaleIn);
        GlStateManager.matrixMode(5888);
    }

    private static void setupRainbowTexturing2(float scaleIn, long time) {
        GlStateManager.matrixMode(5890);
        GlStateManager.pushMatrix();
        GlStateManager.loadIdentity();
        long i = System.currentTimeMillis() * time;
        float f1 = (float) (i % 30000L) / 30000.0F;
        float f2 = (float) Math.sin(i / 30000.0F);
        GlStateManager.translate(f1, f2, 0.0F);
        GlStateManager.scale(scaleIn, scaleIn, scaleIn);
        GlStateManager.matrixMode(5888);
    }

    private static void setupFrilledSharkGlintTexturing(float scaleIn) {
        GlStateManager.matrixMode(5890);
        GlStateManager.pushMatrix();
        GlStateManager.loadIdentity();
        long i = System.currentTimeMillis() * 64L;
        float f1 = (float) (i % 110000L) / 110000.0F;
        GlStateManager.translate(f1, f1, 0.0F);
        GlStateManager.rotate(0.0F, 0.0F, 0.0F, 1.0F);
        GlStateManager.scale(scaleIn, scaleIn, scaleIn);
        GlStateManager.matrixMode(5888);
    }
}
