package com.github.alexthe666.citadel;

import com.github.alexthe666.citadel.config.ConfigHolder;
import com.github.alexthe666.citadel.config.ServerConfig;
import com.github.alexthe666.citadel.item.ItemCitadelBook;
import com.github.alexthe666.citadel.server.CitadelEvents;
import com.github.alexthe666.citadel.server.entity.datatracker.EntityDataCapabilityImplementation;
import com.github.alexthe666.citadel.server.entity.datatracker.EntityDataCapabilityStorage;
import com.github.alexthe666.citadel.server.entity.datatracker.IEntityData;
import com.github.alexthe666.citadel.server.message.AnimationMessage;
import com.github.alexthe666.citadel.server.message.PropertiesMessage;
import com.github.alexthe666.citadel.web.WebHelper;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(modid = "citadel")
public class Citadel {
   public static final Logger LOGGER = LogManager.getLogger("citadel");
   public static final Item DEBUG_ITEM = (new Item()).setRegistryName("citadel:debug");
   public static final boolean DEBUG = false;
   private static final String PROTOCOL_VERSION = Integer.toString(1);
   private static final String PACKET_NETWORK_NAME = "main_channel";
   public static SimpleNetworkWrapper NETWORK_WRAPPER;
   @SidedProxy(
      clientSide = "com.github.alexthe666.citadel.ClientProxy",
      serverSide = "com.github.alexthe666.citadel.ServerProxy"
   )
   public static ServerProxy PROXY;
   @CapabilityInject(IEntityData.class)
   public static Capability<IEntityData> ENTITY_DATA_CAPABILITY;
   public static List<String> PATREONS;
   public static final Item CITADEL_BOOK = (new ItemCitadelBook()).setRegistryName("citadel:citadel_book");
   public static Item EFFECT_ITEM;
   public static Item FANCY_ITEM;

   public Citadel() {
      MinecraftForge.EVENT_BUS.register(this);
      MinecraftForge.EVENT_BUS.register(new CitadelEvents());
   }

   public static <MSG extends IMessage> void sendMSGToServer(MSG message) {
      NETWORK_WRAPPER.sendToServer(message);
   }

   public static <MSG extends IMessage> void sendMSGToAll(MSG message) {
      for(EntityPlayerMP player : FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getPlayers()) {
         sendNonLocal(message, player);
      }

   }

   public static <MSG extends IMessage> void sendNonLocal(MSG msg, EntityPlayerMP player) {
      if (player.getServer() != null && (player.getServer().isDedicatedServer() || !player.getName().equals(player.getServer().getServerOwner()))) {
         NETWORK_WRAPPER.sendTo(msg, player);
      }

   }

   @Mod.EventHandler
   public void setup(FMLPreInitializationEvent event) {
      MinecraftForge.EVENT_BUS.register(PROXY);
      NETWORK_WRAPPER = NetworkRegistry.INSTANCE.newSimpleChannel(PACKET_NETWORK_NAME);
      EFFECT_ITEM = PROXY.setupISTER((new Item()).setRegistryName("citadel:effect_item"));
      FANCY_ITEM = PROXY.setupISTER((new Item()).setRegistryName("citadel:fancy_item"));
      PROXY.onPreInit();
      int packetsRegistered = 0;
      NETWORK_WRAPPER.registerMessage(PropertiesMessage.Handler.class, PropertiesMessage.class, packetsRegistered++, Side.CLIENT);
      NETWORK_WRAPPER.registerMessage(PropertiesMessage.Handler.class, PropertiesMessage.class, packetsRegistered++, Side.SERVER);
      NETWORK_WRAPPER.registerMessage(AnimationMessage.Handler.class, AnimationMessage.class, packetsRegistered++, Side.CLIENT);
      NETWORK_WRAPPER.registerMessage(AnimationMessage.Handler.class, AnimationMessage.class, packetsRegistered++, Side.SERVER);
      BufferedReader urlContents = WebHelper.getURLContents("https://raw.githubusercontent.com/Alex-the-666/Citadel/master/src/main/resources/assets/citadel/patreon.txt", "assets/citadel/patreon.txt");
      if (urlContents != null) {
         String line;
         try {
            while((line = urlContents.readLine()) != null) {
               PATREONS.add(line);
            }
         } catch (IOException var5) {
            LOGGER.warn("Failed to load patreon contributor perks");
         }
      } else {
         LOGGER.warn("Failed to load patreon contributor perks");
      }

      CapabilityManager.INSTANCE.register(IEntityData.class, new EntityDataCapabilityStorage(), () -> new EntityDataCapabilityImplementation());
   }

   @Mod.EventHandler
   public void onInit(FMLInitializationEvent event) {
   }

   @Mod.EventHandler
   public void onPostInit(FMLPostInitializationEvent event) {
   }

   @Mod.EventHandler
   public void onServerStarting(FMLServerStartingEvent event) {
   }

   static {
      PATREONS = new ArrayList();
   }
}
