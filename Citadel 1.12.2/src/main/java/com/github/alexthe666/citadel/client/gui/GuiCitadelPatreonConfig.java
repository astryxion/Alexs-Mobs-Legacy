package com.github.alexthe666.citadel.client.gui;

import com.github.alexthe666.citadel.Citadel;
import com.github.alexthe666.citadel.client.CitadelPatreonRenderer;
import com.github.alexthe666.citadel.server.entity.CitadelEntityData;
import com.github.alexthe666.citadel.server.message.PropertiesMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.fml.client.config.GuiSlider;

@SideOnly(Side.CLIENT)
public class GuiCitadelPatreonConfig extends GuiScreen {
   private GuiSlider distSlider;
   private GuiSlider speedSlider;
   private GuiSlider heightSlider;
   private GuiButton changeButton;
   private final GuiSlider.ISlider distSliderResponder;
   private final GuiSlider.ISlider speedSliderResponder;
   private final GuiSlider.ISlider heightSliderResponder;
   private final GuiScreen parentScreen;
   private float rotateDist;
   private float rotateSpeed;
   private float rotateHeight;
   private String followType = "citadel";

   public GuiCitadelPatreonConfig(GuiScreen parentScreenIn) {
      this.parentScreen = parentScreenIn;
      NBTTagCompound tag = CitadelEntityData.getOrCreateCitadelTag(Minecraft.getMinecraft().player);
      float distance = tag.hasKey("CitadelRotateDistance") ? tag.getFloat("CitadelRotateDistance") : 2.0F;
      float speed = tag.hasKey("CitadelRotateSpeed") ? tag.getFloat("CitadelRotateSpeed") : 1.0F;
      float height = tag.hasKey("CitadelRotateHeight") ? tag.getFloat("CitadelRotateHeight") : 1.0F;
      this.rotateDist = roundTo(distance, 3);
      this.rotateSpeed = roundTo(speed, 3);
      this.rotateHeight = roundTo(height, 3);
      this.followType = tag.hasKey("CitadelFollowerType") ? tag.getString("CitadelFollowerType") : "citadel";
      this.distSliderResponder = new GuiSlider.ISlider() {
         public void onChangeSliderValue(GuiSlider slider) {
            GuiCitadelPatreonConfig.this.setSliderValue(0, (float)slider.getValue());
         }
      };
      this.speedSliderResponder = new GuiSlider.ISlider() {
         public void onChangeSliderValue(GuiSlider slider) {
            GuiCitadelPatreonConfig.this.setSliderValue(1, (float)slider.getValue());
         }
      };
      this.heightSliderResponder = new GuiSlider.ISlider() {
         public void onChangeSliderValue(GuiSlider slider) {
            GuiCitadelPatreonConfig.this.setSliderValue(2, (float)slider.getValue());
         }
      };
   }

   private void setSliderValue(int i, float sliderValue) {
      boolean flag = false;
      NBTTagCompound tag = CitadelEntityData.getOrCreateCitadelTag(Minecraft.getMinecraft().player);
      if (i == 0) {
         this.rotateDist = roundTo(sliderValue * 5.0F, 3);
         tag.setFloat("CitadelRotateDistance", this.rotateDist);
      } else if (i == 1) {
         this.rotateSpeed = roundTo(sliderValue * 5.0F, 3);
         tag.setFloat("CitadelRotateSpeed", this.rotateSpeed);
      } else {
         this.rotateHeight = roundTo(sliderValue * 2.0F, 3);
         tag.setFloat("CitadelRotateHeight", this.rotateHeight);
      }

      CitadelEntityData.setCitadelTag(Minecraft.getMinecraft().player, tag);
      Citadel.sendMSGToServer(new PropertiesMessage("CitadelPatreonConfig", tag, Minecraft.getMinecraft().player.getEntityId()));
   }

   public static float roundTo(float value, int places) {
      return value;
   }

   public void drawScreen(int mouseX, int mouseY, float partialTicks) {
      this.drawDefaultBackground();
      this.drawCenteredString(this.fontRenderer, (new TextComponentTranslation("citadel.gui.patreon_customization")).getFormattedText(), this.width / 2, 20, 16777215);
      super.drawScreen(mouseX, mouseY, partialTicks);
   }

   public void initGui() {
      super.initGui();
      int i = this.width / 2;
      int j = this.height / 6;
      this.buttonList.add(new GuiButton(0, i - 100, j + 120, 200, 20, (new TextComponentTranslation("gui.done")).getFormattedText()));
      this.buttonList.add(this.distSlider = new GuiSlider(1, i - 75 - 25, j + 30, 150, 20, (new TextComponentTranslation("citadel.gui.orbit_dist")).getFormattedText() + ": ", "", 0.125D, 5.0D, (double)this.rotateDist, true, true, this.distSliderResponder));
      this.buttonList.add(new GuiButton(2, i - 75 + 135, j + 30, 40, 20, (new TextComponentTranslation("citadel.gui.reset")).getFormattedText()));
      this.buttonList.add(this.speedSlider = new GuiSlider(3, i - 75 - 25, j + 60, 150, 20, (new TextComponentTranslation("citadel.gui.orbit_speed")).getFormattedText() + ": ", "", 0.0D, 5.0D, (double)this.rotateSpeed, true, true, this.speedSliderResponder));
      this.buttonList.add(new GuiButton(4, i - 75 + 135, j + 60, 40, 20, (new TextComponentTranslation("citadel.gui.reset")).getFormattedText()));
      this.buttonList.add(this.heightSlider = new GuiSlider(5, i - 75 - 25, j + 90, 150, 20, (new TextComponentTranslation("citadel.gui.orbit_height")).getFormattedText() + ": ", "", 0.0D, 2.0D, (double)this.rotateHeight, true, true, this.heightSliderResponder));
      this.buttonList.add(new GuiButton(6, i - 75 + 135, j + 90, 40, 20, (new TextComponentTranslation("citadel.gui.reset")).getFormattedText()));
      this.changeButton = new GuiButton(7, i - 100, j, 200, 20, this.getTypeText().getFormattedText());
      this.buttonList.add(this.changeButton);
   }

   protected void actionPerformed(GuiButton button) {
      if (button.id == 0) {
         this.mc.displayGuiScreen(this.parentScreen);
      } else if (button.id == 2) {
         this.setSliderValue(0, 0.4F);
         this.distSlider.setValue(0.4D);
      } else if (button.id == 4) {
         this.setSliderValue(1, 0.2F);
         this.speedSlider.setValue(0.2D);
      } else if (button.id == 6) {
         this.setSliderValue(2, 0.5F);
         this.heightSlider.setValue(0.5D);
      } else if (button.id == 7) {
         this.followType = CitadelPatreonRenderer.getIdOfNext(this.followType);
         NBTTagCompound tag = CitadelEntityData.getOrCreateCitadelTag(Minecraft.getMinecraft().player);
         if (tag != null) {
            tag.setString("CitadelFollowerType", this.followType);
            CitadelEntityData.setCitadelTag(Minecraft.getMinecraft().player, tag);
         }

         Citadel.sendMSGToServer(new PropertiesMessage("CitadelPatreonConfig", tag, Minecraft.getMinecraft().player.getEntityId()));
         this.changeButton.displayString = this.getTypeText().getFormattedText();
      }
   }

   private ITextComponent getTypeText() {
      return new TextComponentTranslation("citadel.gui.follower_type", new Object[]{new TextComponentTranslation("citadel.follower." + this.followType)});
   }
}
