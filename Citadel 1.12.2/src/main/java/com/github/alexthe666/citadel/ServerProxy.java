package com.github.alexthe666.citadel;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber(
   modid = "citadel"
)
public class ServerProxy {
   public void onPreInit() {
   }

   public void handleAnimationPacket(int entityId, int index) {
   }

   @SubscribeEvent
   public static void onItemsRegistry(RegistryEvent.Register<Item> registry) {
      Citadel.CITADEL_BOOK.setUnlocalizedName("item.citadel.citadel_book");
      registry.getRegistry().register(Citadel.CITADEL_BOOK);
      if (Citadel.DEBUG) {
         registry.getRegistry().registerAll(new Item[]{Citadel.DEBUG_ITEM, Citadel.EFFECT_ITEM, Citadel.FANCY_ITEM});
      }
   }

   public void handlePropertiesPacket(String propertyID, NBTTagCompound compound, int entityID) {
   }

   public Item setupISTER(Item item) {
      return item;
   }

   public void openBookGUI(ItemStack book) {
   }
}
