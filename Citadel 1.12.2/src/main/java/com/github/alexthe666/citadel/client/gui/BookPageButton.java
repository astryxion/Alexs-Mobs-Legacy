package com.github.alexthe666.citadel.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.init.SoundEvents;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class BookPageButton extends GuiButton {
   private final boolean isForward;
   private final boolean playTurnSound;
   private final GuiBasicBook bookGUI;

   public BookPageButton(GuiBasicBook bookGUI, int x, int y, boolean isForward, boolean playTurnSound) {
      super(0, x, y, 23, 13, "");
      this.isForward = isForward;
      this.playTurnSound = playTurnSound;
      this.bookGUI = bookGUI;
   }

   @Override
   public void drawButton(Minecraft minecraft, int mouseX, int mouseY, float partialTicks) {
      if (this.visible) {
         this.hovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;
         int u = this.hovered ? 23 : 0;
         int v = this.isForward ? 0 : 13;
         int color = this.bookGUI.getWidgetColor();
         int red = (color & 16711680) >> 16;
         int green = (color & '\uff00') >> 8;
         int blue = color & 255;
         BookBlit.setRGB(red, green, blue, 255);
         GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
         minecraft.getTextureManager().bindTexture(this.bookGUI.getBookWidgetTexture());
         this.drawNextArrow(this.x, this.y, u, v, 18, 12);
         BookBlit.setRGB(255, 255, 255, 255);
         GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
      }
   }

   public void drawNextArrow(int x, int y, int u, int v, int width, int height) {
      if (this.hovered) {
         BookBlit.func_238464_a_(x, y, 0, (float)u, (float)v, width, height, 256, 256);
      } else {
         this.drawTexturedModalRect(x, y, u, v, width, height);
      }
   }

   @Override
   public void playPressSound(SoundHandler soundHandler) {
      if (this.playTurnSound) {
         soundHandler.playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.0F));
      }
   }
}
