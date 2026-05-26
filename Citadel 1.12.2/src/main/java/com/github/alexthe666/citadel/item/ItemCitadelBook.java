package com.github.alexthe666.citadel.item;

import com.github.alexthe666.citadel.Citadel;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;

public class ItemCitadelBook extends Item {
   public ItemCitadelBook() {
      this.setMaxStackSize(1);
   }

   public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn) {
      ItemStack itemStackIn = playerIn.getHeldItem(handIn);
      if (worldIn.isRemote) {
         Citadel.PROXY.openBookGUI(itemStackIn);
      }

      return new ActionResult(EnumActionResult.PASS, itemStackIn);
   }
}
