package com.github.alexthe666.citadel.client.gui;

import java.awt.image.BufferedImage;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

/**
 * BSL's custom lightmap makes dictionary mobs look like dusk silhouettes. Do not flip
 * OptiFine {@code shaderPackLoaded} — that leaves {@link BufferBuilder} mid-build and
 * crashes at framebuffer blit ({@code Already building!}). Unbind the GL program, keep
 * {@code isRenderingWorld} false so {@code nextEntity} is a no-op, and bind a white lightmap.
 */
@EventBusSubscriber(
   modid = "citadel",
   value = Side.CLIENT
)
public final class GuiShaderCompat {
   private static final String[] SHADER_CLASSES = {
      "net.optifine.shaders.Shaders",
      "shadersmod.client.Shaders"
   };

   private static int depth;
   private static boolean resolved;
   private static Method useProgram;
   private static Field isRenderingWorld;
   private static Object programNone;
   private static Boolean savedRenderingWorld;
   private static int savedGlProgram;
   private static int savedLightmapTex;
   private static int whiteLightmapTex;
   private static Field bufferDrawing;

   private GuiShaderCompat() {
   }

   public static boolean isDrawingGuiEntity() {
      return depth > 0;
   }

   public static void beginGuiEntity() {
      depth++;
      if (depth == 1) {
         resolve();
         savedGlProgram = GL11.glGetInteger(GL20.GL_CURRENT_PROGRAM);
         savedRenderingWorld = getBoolean(isRenderingWorld);
         GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
         savedLightmapTex = GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);
         GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
         setBoolean(isRenderingWorld, Boolean.FALSE);
      }
      applyGuiEntityLighting();
   }

   public static void endGuiEntity() {
      if (depth <= 0) {
         return;
      }
      depth--;
      if (depth == 0) {
         finishTessellatorIfDrawing();
         setBoolean(isRenderingWorld, savedRenderingWorld);
         invokeUseProgram(programNone);
         if (savedGlProgram >= 0) {
            GL20.glUseProgram(savedGlProgram);
         }
         restoreLightmapTexture();
         savedRenderingWorld = null;
         savedGlProgram = 0;
         savedLightmapTex = 0;
      }
   }

   public static void applyGuiEntityLighting() {
      setBoolean(isRenderingWorld, Boolean.FALSE);
      invokeUseProgram(programNone);
      GL20.glUseProgram(0);
      bindWhiteLightmap();
      RenderHelper.enableStandardItemLighting();
      GlStateManager.enableLighting();
      GlStateManager.enableColorMaterial();
      GlStateManager.enableRescaleNormal();
      GlStateManager.disableFog();
      GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
   }

   @SubscribeEvent(
      priority = EventPriority.HIGHEST
   )
   public static void onLivingPre(RenderLivingEvent.Pre event) {
      if (depth > 0) {
         applyGuiEntityLighting();
      }
   }

   private static void bindWhiteLightmap() {
      ensureWhiteLightmap();
      if (whiteLightmapTex <= 0) {
         OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0F, 240.0F);
         return;
      }
      GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
      GlStateManager.matrixMode(GL11.GL_TEXTURE);
      GlStateManager.loadIdentity();
      float scale = 0.00390625F;
      GlStateManager.scale(scale, scale, scale);
      GlStateManager.translate(8.0F, 8.0F, 8.0F);
      GlStateManager.matrixMode(GL11.GL_MODELVIEW);
      GlStateManager.bindTexture(whiteLightmapTex);
      GlStateManager.enableTexture2D();
      GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
      GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
      GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_CLAMP);
      GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_CLAMP);
      GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
      OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0F, 240.0F);
   }

   private static void restoreLightmapTexture() {
      if (savedLightmapTex > 0) {
         GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
         GlStateManager.bindTexture(savedLightmapTex);
         GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
      }
   }

   private static void ensureWhiteLightmap() {
      if (whiteLightmapTex > 0 || isTessellatorDrawing()) {
         return;
      }
      BufferedImage image = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
      for (int x = 0; x < 16; x++) {
         for (int y = 0; y < 16; y++) {
            image.setRGB(x, y, 0xFFFFFFFF);
         }
      }
      whiteLightmapTex = TextureUtil.glGenTextures();
      TextureUtil.uploadTextureImage(whiteLightmapTex, image);
   }

   private static boolean isTessellatorDrawing() {
      try {
         BufferBuilder buf = Tessellator.getInstance().getBuffer();
         return drawingField().getBoolean(buf);
      } catch (Throwable t) {
         return false;
      }
   }

   private static void finishTessellatorIfDrawing() {
      try {
         if (isTessellatorDrawing()) {
            Tessellator.getInstance().draw();
         }
      } catch (Throwable ignored) {
      }
   }

   private static Field drawingField() throws Exception {
      if (bufferDrawing == null) {
         bufferDrawing = BufferBuilder.class.getDeclaredField("isDrawing");
         bufferDrawing.setAccessible(true);
      }
      return bufferDrawing;
   }

   private static void resolve() {
      if (resolved) {
         return;
      }
      resolved = true;
      for (int i = 0; i < SHADER_CLASSES.length && useProgram == null; i++) {
         try {
            Class<?> shaders = Class.forName(SHADER_CLASSES[i]);
            isRenderingWorld = field(shaders, "isRenderingWorld");
            try {
               Class<?> program = Class.forName("net.optifine.shaders.Program");
               programNone = shaders.getField("ProgramNone").get(null);
               useProgram = shaders.getMethod("useProgram", program);
            } catch (ClassNotFoundException e) {
               useProgram = shaders.getMethod("useProgram", Integer.TYPE);
               try {
                  programNone = shaders.getField("ProgramNone").get(null);
               } catch (NoSuchFieldException e2) {
                  programNone = Integer.valueOf(0);
               }
            }
         } catch (Throwable ignored) {
         }
      }
   }

   private static Field field(Class<?> type, String name) {
      try {
         Field f = type.getField(name);
         f.setAccessible(true);
         return f;
      } catch (Throwable t) {
         try {
            Field f = type.getDeclaredField(name);
            f.setAccessible(true);
            return f;
         } catch (Throwable t2) {
            return null;
         }
      }
   }

   private static void invokeUseProgram(Object program) {
      if (useProgram == null || program == null) {
         return;
      }
      try {
         useProgram.invoke(null, program);
      } catch (Throwable ignored) {
      }
   }

   private static Boolean getBoolean(Field field) {
      if (field == null) {
         return null;
      }
      try {
         return Boolean.valueOf(field.getBoolean(null));
      } catch (Throwable t) {
         return null;
      }
   }

   private static void setBoolean(Field field, Boolean value) {
      if (field == null || value == null) {
         return;
      }
      try {
         field.setBoolean(null, value.booleanValue());
      } catch (Throwable ignored) {
      }
   }
}
