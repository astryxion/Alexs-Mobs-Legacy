package com.github.alexthe666.citadel.client.gui;

import com.github.alexthe666.citadel.client.gui.data.EntityLinkData;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.util.ResourceLocation;

public class EntityLinkButton extends GuiButton {
   private static Map<String, Entity> renderedEntites = new HashMap();
   private EntityLinkData data;
   private GuiBasicBook bookGUI;
   private EnttyRenderWindow window = new EnttyRenderWindow();
   private final int k;
   private final int l;

   public EntityLinkButton(GuiBasicBook bookGUI, EntityLinkData linkData, int k, int l) {
      super(0, k + linkData.getX() - 12, l + linkData.getY(), (int)((double)24.0F * linkData.getScale()), (int)((double)24.0F * linkData.getScale()), "");
      this.data = linkData;
      this.bookGUI = bookGUI;
      this.k = k;
      this.l = l;
   }

   public void func_191745_a(Minecraft minecraft, int mouseX, int mouseY, float partialTicks) {
      if (!this.visible) {
         return;
      }

      this.hovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;
      int lvt_5_1_ = 0;
      int lvt_6_1_ = 30;
      float f = (float)this.data.getScale();
      minecraft.getTextureManager().bindTexture(this.bookGUI.getBookWidgetTexture());
      GlStateManager.pushMatrix();
      GlStateManager.translate((float)this.x, (float)this.y, 0.0F);
      GlStateManager.scale(f, f, 1.0F);
      this.drawBtn(false, 0, 0, lvt_5_1_, lvt_6_1_, 24, 24);
      Entity model = null;
      ResourceLocation entityId = new ResourceLocation(this.data.getEntity());
      if (EntityList.isRegistered(entityId)) {
         model = renderedEntites.get(this.data.getEntity());
         if (model == null) {
            model = EntityList.createEntityByIDFromName(entityId, minecraft.world);
            if (model != null) {
               renderedEntites.put(this.data.getEntity(), model);
            }
         }
      }

      if (model != null) {
         float renderScale = 10.0F * (float)this.data.getEntityScale();
         int entityX = (int)(12.0F + this.data.getOffset_x());
         int entityY = (int)(24.0F + this.data.getOffset_y());
         this.window.renderEntityWindow(model, entityX, entityY, renderScale);
      }

      GlStateManager.depthFunc(515);
      GlStateManager.disableDepth();
      if (this.hovered) {
         this.bookGUI.setEntityTooltip(this.data.getHoverText());
         lvt_5_1_ = 48;
      } else {
         lvt_5_1_ = 24;
      }

      int color = this.bookGUI.getWidgetColor();
      int r = (color & 16711680) >> 16;
      int g = (color & '\uff00') >> 8;
      int b = color & 255;
      BookBlit.setRGB(r, g, b, 255);
      minecraft.getTextureManager().bindTexture(this.bookGUI.getBookWidgetTexture());
      this.drawBtn(!this.hovered, 0, 0, lvt_5_1_, lvt_6_1_, 24, 24);
      GlStateManager.popMatrix();
   }

   public void drawBtn(boolean color, int p_238474_2_, int p_238474_3_, int p_238474_4_, int p_238474_5_, int p_238474_6_, int p_238474_7_) {
      if (color) {
         BookBlit.func_238464_a_(p_238474_2_, p_238474_3_, 0, (float)p_238474_4_, (float)p_238474_5_, p_238474_6_, p_238474_7_, 256, 256);
      } else {
         this.drawTexturedModalRect(p_238474_2_, p_238474_3_, p_238474_4_, p_238474_5_, p_238474_6_, p_238474_7_);
      }

   }

   /**
    * Same facing as animal dictionary mob pages ({@code rot_x=30}, {@code rot_y=225+180} for 1.12 mirror fix).
    */
   private static void renderEntityInInventory(int xPos, int yPos, float scale, Entity entity) {
      GuiBasicBook.drawEntityOnScreen(xPos, yPos, scale, false, 30.0D, 405.0D, 0.0D, 0.0F, 0.0F, entity);
   }

   private class EnttyRenderWindow extends Gui {
      private EnttyRenderWindow() {
      }

      public void renderEntityWindow(Entity toRender, int entityX, int entityY, float renderScale) {
         GlStateManager.pushMatrix();
         GlStateManager.enableDepth();
         GlStateManager.translate(0.0F, 0.0F, 950.0F);
         GlStateManager.colorMask(false, false, false, false);
         BookBlit.func_238467_a_(4680, 2260, -4680, -2260, -16777216);
         GlStateManager.colorMask(true, true, true, true);
         GlStateManager.translate(0.0F, 0.0F, -950.0F);
         GlStateManager.depthFunc(518);
         BookBlit.func_238467_a_(22, 22, 2, 2, -16777216);
         GlStateManager.depthFunc(515);
         Minecraft.getMinecraft().getTextureManager().bindTexture(EntityLinkButton.this.bookGUI.getBookWidgetTexture());
         BookBlit.func_238463_a_(0, 0, 0.0F, 30.0F, 24, 24, 256, 256);
         if (toRender != null) {
            toRender.ticksExisted = Minecraft.getMinecraft().player.ticksExisted;
            renderEntityInInventory(entityX, entityY, renderScale, toRender);
         }

         GlStateManager.depthFunc(518);
         GlStateManager.translate(0.0F, 0.0F, -950.0F);
         GlStateManager.colorMask(false, false, false, false);
         BookBlit.func_238467_a_(4680, 2260, -4680, -2260, -16777216);
         GlStateManager.colorMask(true, true, true, true);
         GlStateManager.translate(0.0F, 0.0F, 950.0F);
         GlStateManager.depthFunc(515);
         GlStateManager.popMatrix();
      }
   }
}
