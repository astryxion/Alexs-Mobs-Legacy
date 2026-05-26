package com.github.alexthe666.citadel.client.gui;

import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiCitadelBook extends GuiBasicBook {
   public GuiCitadelBook(ItemStack bookStack) {
      super(bookStack, new TextComponentTranslation("citadel_guide_book.title"));
   }

   protected int getBindingColor() {
      return 6595195;
   }

   public ResourceLocation getRootPage() {
      return new ResourceLocation("citadel:book/citadel_book/root.json");
   }

   public String getTextFileDirectory() {
      return "citadel:book/citadel_book/";
   }
}
