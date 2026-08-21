package com.github.alexthe666.citadel.client.gui;

import com.github.alexthe666.citadel.client.gui.data.EntityLinkData;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import org.lwjgl.opengl.GL11;

/**
 * Index-page mob icon. 1.16 draws a black inset, then the dark-gray widget well, then
 * the entity, then the tinted frame. The well texture is semi-transparent; without that
 * inset it composites onto parchment and disappears.
 */
public class EntityLinkButton extends GuiButton {
   private static final Map<String, Entity> RENDERED_ENTITIES = new HashMap<String, Entity>();
   private static final Set<String> COMPILED_MODELS = new HashSet<String>();
   private static final int NEW_RENDERS_PER_FRAME = 6;
   private static final int PREWARM_PER_FRAME = 4;
   private static int newRendersThisFrame = 0;

   private final EntityLinkData data;
   private final GuiBasicBook bookGUI;

   public EntityLinkButton(GuiBasicBook bookGUI, EntityLinkData linkData, int k, int l) {
      super(0, k + linkData.getX() - 12, l + linkData.getY(), (int) (24.0F * linkData.getScale()), (int) (24.0F * linkData.getScale()), "");
      this.data = linkData;
      this.bookGUI = bookGUI;
   }

   public static void beginFrame() {
      newRendersThisFrame = 0;
   }

   public static void prewarm(Minecraft minecraft, String namespace) {
      if (minecraft == null || minecraft.world == null || minecraft.player == null) {
         return;
      }
      if (minecraft.currentScreen instanceof GuiBasicBook) {
         return;
      }
      int warmed = 0;
      for (ResourceLocation id : ForgeRegistries.ENTITIES.getKeys()) {
         if (!namespace.equals(id.getResourceDomain())) {
            continue;
         }
         String key = id.toString();
         if (COMPILED_MODELS.contains(key)) {
            continue;
         }
         Entity model = getOrCreateEntity(minecraft, key);
         if (model == null) {
            COMPILED_MODELS.add(key);
            continue;
         }
         try {
            GuiBasicBook.drawEntityOnScreen(-500, -500, 1.0F, false, 30.0D, 0.0D, 0.0D, 0.0F, 0.0F, model);
         } catch (Throwable ignored) {
         } finally {
            GuiBasicBook.restoreGuiLighting();
         }
         COMPILED_MODELS.add(key);
         if (++warmed >= PREWARM_PER_FRAME) {
            return;
         }
      }
   }

   @Override
   public void drawButton(Minecraft minecraft, int mouseX, int mouseY, float partialTicks) {
      if (!this.visible) {
         return;
      }
      this.hovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;
      float f = (float) this.data.getScale();

      prepare2D();
      GlStateManager.pushMatrix();
      GlStateManager.translate((float) this.x, (float) this.y, 0.0F);
      GlStateManager.scale(f, f, 1.0F);
      GlStateManager.disableBlend();
      drawRect(2, 2, 22, 22, -16777216);
      GlStateManager.enableBlend();
      GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
      GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
      BookBlit.setRGB(255, 255, 255, 255);
      minecraft.getTextureManager().bindTexture(this.bookGUI.getBookWidgetTexture());
      this.drawTexturedModalRect(0, 0, 0, 30, 24, 24);

      Entity model = getOrCreateEntity(minecraft, this.data.getEntity());
      if (model != null && minecraft.player != null && canRenderModel()) {
         model.ticksExisted = minecraft.player.ticksExisted;
         enableScissor(minecraft,
               this.x + Math.round(f * 4.0F),
               this.y + Math.round(f * 4.0F),
               Math.max(1, Math.round(f * 16.0F)),
               Math.max(1, Math.round(f * 16.0F)));
         GlStateManager.enableDepth();
         GlStateManager.enableRescaleNormal();
         try {
            GuiBasicBook.drawEntityOnScreen(
                  (int) (12.0F + this.data.getOffset_x()),
                  (int) (24.0F + this.data.getOffset_y()),
                  10.0F * (float) this.data.getEntityScale(),
                  false, 30.0D, -130.0D + this.bookGUI.getBookEntityYawOffset(), 0.0D, 0.0F, 0.0F, model);
         } finally {
            disableScissor();
            prepare2D();
         }
      }

      int overlayU = this.hovered ? 48 : 24;
      int color = this.bookGUI.getWidgetColor();
      BookBlit.setRGB((color & 16711680) >> 16, (color & '\uff00') >> 8, color & 255, 255);
      minecraft.getTextureManager().bindTexture(this.bookGUI.getBookWidgetTexture());
      BookBlit.func_238464_a_(0, 0, 0, (float) overlayU, 30.0F, 24, 24, 256, 256);
      GlStateManager.popMatrix();
      prepare2D();

      if (this.hovered) {
         this.bookGUI.setEntityTooltip(this.data.getHoverText());
      }
   }

   private boolean canRenderModel() {
      if (COMPILED_MODELS.contains(this.data.getEntity())) {
         return true;
      }
      if (newRendersThisFrame >= NEW_RENDERS_PER_FRAME) {
         return false;
      }
      newRendersThisFrame++;
      COMPILED_MODELS.add(this.data.getEntity());
      return true;
   }

   private static void prepare2D() {
      GuiBasicBook.restoreGuiLighting();
      GlStateManager.disableDepth();
      GL11.glDisable(GL11.GL_DEPTH_TEST);
   }

   private static Entity getOrCreateEntity(Minecraft minecraft, String entityId) {
      Entity cached = RENDERED_ENTITIES.get(entityId);
      if (cached != null) {
         return cached;
      }
      if (minecraft.world == null) {
         return null;
      }
      ResourceLocation id = new ResourceLocation(entityId);
      if (!EntityList.isRegistered(id)) {
         return null;
      }
      Entity created = EntityList.createEntityByIDFromName(id, minecraft.world);
      if (created != null) {
         RENDERED_ENTITIES.put(entityId, created);
      }
      return created;
   }

   private static void enableScissor(Minecraft mc, int x, int y, int width, int height) {
      ScaledResolution res = new ScaledResolution(mc);
      int factor = res.getScaleFactor();
      GL11.glEnable(GL11.GL_SCISSOR_TEST);
      GL11.glScissor(x * factor, mc.displayHeight - (y + height) * factor, width * factor, height * factor);
   }

   private static void disableScissor() {
      GL11.glDisable(GL11.GL_SCISSOR_TEST);
   }
}
