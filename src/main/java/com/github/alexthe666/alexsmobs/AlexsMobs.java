package com.github.alexthe666.alexsmobs;

import com.github.alexthe666.alexsmobs.block.AMBlockRegistry;
import com.github.alexthe666.alexsmobs.config.AMConfig;
import com.github.alexthe666.alexsmobs.config.BiomeConfig;
import com.github.alexthe666.alexsmobs.config.ConfigHolder;
import com.github.alexthe666.alexsmobs.item.AMItemRegistry;
import com.github.alexthe666.alexsmobs.item.ItemTarantulaHawkElytra;
import com.github.alexthe666.alexsmobs.misc.AMTagRegistry;
import com.github.alexthe666.alexsmobs.misc.EmeraldsForItemsTrade;
import com.github.alexthe666.alexsmobs.misc.ItemsForEmeraldsTrade;
import com.github.alexthe666.alexsmobs.world.AMWorldRegistry;
import com.github.alexthe666.alexsmobs.misc.AMSmeltingRecipes;
import com.github.alexthe666.alexsmobs.world.spawn.AMSpawnBiomeConfiguration;
import com.github.alexthe666.alexsmobs.event.ServerEvents;
import com.github.alexthe666.alexsmobs.message.*;
import com.github.alexthe666.alexsmobs.misc.AMAdvancementTriggerRegistry;
import com.github.alexthe666.alexsmobs.misc.AMItemGroup;
import com.github.alexthe666.alexsmobs.tileentity.AMTileEntityRegistry;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ResourceLocation;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraftforge.registries.IForgeRegistryEntry;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.common.registry.VillagerRegistry;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerAboutToStartEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;

@Mod(modid = AlexsMobs.MODID, name = "Alex's Mobs (Legacy)", version = AlexsMobs.VERSION, acceptedMinecraftVersions = "[1.12.2]", dependencies = "required-after:citadel;after:jei")
public class AlexsMobs {

    public static final Logger LOGGER = LogManager.getLogger();
    public static final String MODID = "alexsmobs";
    public static final String VERSION = "1.12.2-1.3.0";

    @Mod.Instance(MODID)
    public static AlexsMobs instance;

    @SidedProxy(clientSide = "com.github.alexthe666.alexsmobs.ClientProxy", serverSide = "com.github.alexthe666.alexsmobs.CommonProxy")
    public static CommonProxy PROXY;

    public static final SimpleNetworkWrapper NETWORK_WRAPPER = new SimpleNetworkWrapper(MODID);
    private static int packetId;

    public static CreativeTabs TAB = new AMItemGroup();

    /**
     * 1.12.2 {@link Item#setRegistryName} / {@link Block#setRegistryName} do not set {@code unlocalizedName};
     * Forge may overwrite names during {@code register()}, so this must run after registration (and again at
     * {@link EventPriority#LOWEST} on the registry event).
     */
    public static void applyUnlocalizedNameFromRegistry(IForgeRegistryEntry<?> entry) {
        ResourceLocation rl = entry.getRegistryName();
        if (rl != null && MODID.equals(rl.getResourceDomain())) {
            String key = rl.toString().replace(':', '.');
            if (entry instanceof Item) {
                ((Item) entry).setUnlocalizedName(key);
            } else if (entry instanceof Block) {
                ((Block) entry).setUnlocalizedName(key);
            }
        }
    }

    @Mod.EventBusSubscriber(modid = MODID)
    public static class RegistryNamingFix {
        @SubscribeEvent(priority = EventPriority.LOWEST)
        public static void onItemsRegistered(RegistryEvent.Register<Item> event) {
            for (Item item : ForgeRegistries.ITEMS.getValuesCollection()) {
                applyUnlocalizedNameFromRegistry(item);
            }
        }

        @SubscribeEvent(priority = EventPriority.LOWEST)
        public static void onBlocksRegistered(RegistryEvent.Register<Block> event) {
            for (Block block : ForgeRegistries.BLOCKS.getValuesCollection()) {
                applyUnlocalizedNameFromRegistry(block);
            }
        }
    }

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        ConfigHolder.initCommonConfiguration(new File(event.getModConfigurationDirectory(), "alexsmobs.cfg"));
        AMSpawnBiomeConfiguration.init(event.getModConfigurationDirectory());
        BiomeConfig.init();
        registerPackets();
        AMTileEntityRegistry.register();
        AMTagRegistry.loadDataBlockTags();
        AMTagRegistry.loadDataItemTags();
        AMTagRegistry.loadDataEntityTypeTags();
        PROXY.init();
        PROXY.preInitClient();
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        AMSmeltingRecipes.register();
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(new ServerEvents());
        AMAdvancementTriggerRegistry.init();
        registerVillagerTrades();
        PROXY.initClient();
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        // Natural spawns are registered on the logical server only (see serverAboutToStart).
        // PostInit runs on both sides in singleplayer; registering there left the server with no spawns.
    }

    /** Registers biome spawns on the dedicated/logical server after registries and config are ready. */
    @Mod.EventHandler
    public void serverAboutToStart(FMLServerAboutToStartEvent event) {
        AMConfig.bake();
        AMWorldRegistry.register();
    }

    /**
     * 1.12.2 has no wandering trader; trades that 1.16 registered on {@code WandererTradesEvent} are added to the
     * librarian career (master tiers) so the same offers remain obtainable from villagers.
     */
    private static void registerVillagerTrades() {
        VillagerRegistry.VillagerProfession farmer = ForgeRegistries.VILLAGER_PROFESSIONS.getValue(new ResourceLocation("minecraft:farmer"));
        if (farmer != null) {
            VillagerRegistry.VillagerCareer fisherman = farmer.getCareer(1);
            fisherman.addTrade(3, new EmeraldsForItemsTrade(AMItemRegistry.AMBERGRIS, 20, 3, 4));
        }
        if (!AMConfig.wanderingTraderOffers) {
            return;
        }
        VillagerRegistry.VillagerProfession librarian = ForgeRegistries.VILLAGER_PROFESSIONS.getValue(new ResourceLocation("minecraft:librarian"));
        if (librarian == null) {
            return;
        }
        VillagerRegistry.VillagerCareer librarianCareer = librarian.getCareer(0);
        librarianCareer.addTrade(4,
                new ItemsForEmeraldsTrade(AMItemRegistry.ANIMAL_DICTIONARY, 4, 1, 2, 1),
                new ItemsForEmeraldsTrade(AMItemRegistry.ACACIA_BLOSSOM, 3, 2, 2, 1));
        if (AMConfig.cockroachSpawnWeight > 0) {
            librarianCareer.addTrade(4, new ItemsForEmeraldsTrade(AMItemRegistry.COCKROACH_OOTHECA, 2, 1, 2, 1));
        }
        if (AMConfig.blobfishSpawnWeight > 0) {
            librarianCareer.addTrade(4, new ItemsForEmeraldsTrade(AMItemRegistry.BLOBFISH_BUCKET, 4, 1, 3, 1));
        }
        librarianCareer.addTrade(4,
                new ItemsForEmeraldsTrade(AMItemRegistry.BEAR_FUR, 1, 1, 2, 1),
                new ItemsForEmeraldsTrade(AMItemRegistry.CROCODILE_SCUTE, 5, 1, 2, 1),
                new ItemsForEmeraldsTrade(AMItemRegistry.MOSQUITO_LARVA, 1, 3, 5, 1));
        librarianCareer.addTrade(5,
                new ItemsForEmeraldsTrade(AMItemRegistry.SOMBRERO, 20, 1, 1, 1),
                new ItemsForEmeraldsTrade(AMBlockRegistry.BANANA_PEEL, 1, 2, 1, 1),
                new ItemsForEmeraldsTrade(AMItemRegistry.BLOOD_SAC, 5, 2, 3, 1));
    }

    /**
     * Server-&gt;client packets: handler runs on {@link Side#CLIENT}.
     * Client-&gt;server packets: handler runs on {@link Side#SERVER}.
     * Bidirectional message types are registered twice with distinct ids.
     */
    private static void registerPackets() {
        NETWORK_WRAPPER.registerMessage(MessageMosquitoMountPlayer.Handler.class, MessageMosquitoMountPlayer.class, packetId++, Side.CLIENT);
        NETWORK_WRAPPER.registerMessage(MessageMosquitoDismount.Handler.class, MessageMosquitoDismount.class, packetId++, Side.CLIENT);
        NETWORK_WRAPPER.registerMessage(MessageMosquitoDismount.Handler.class, MessageMosquitoDismount.class, packetId++, Side.SERVER);
        NETWORK_WRAPPER.registerMessage(MessageHurtMultipart.Handler.class, MessageHurtMultipart.class, packetId++, Side.CLIENT);
        NETWORK_WRAPPER.registerMessage(MessageCrowMountPlayer.Handler.class, MessageCrowMountPlayer.class, packetId++, Side.CLIENT);
        NETWORK_WRAPPER.registerMessage(MessageCrowDismount.Handler.class, MessageCrowDismount.class, packetId++, Side.CLIENT);
        NETWORK_WRAPPER.registerMessage(MessageMungusBiomeChange.Handler.class, MessageMungusBiomeChange.class, packetId++, Side.CLIENT);
        NETWORK_WRAPPER.registerMessage(MessageKangarooInventorySync.Handler.class, MessageKangarooInventorySync.class, packetId++, Side.CLIENT);
        NETWORK_WRAPPER.registerMessage(MessageKangarooEat.Handler.class, MessageKangarooEat.class, packetId++, Side.CLIENT);
        NETWORK_WRAPPER.registerMessage(MessageUpdateCapsid.Handler.class, MessageUpdateCapsid.class, packetId++, Side.CLIENT);
        NETWORK_WRAPPER.registerMessage(MessageSwingArm.Handler.class, MessageSwingArm.class, packetId++, Side.SERVER);
        NETWORK_WRAPPER.registerMessage(MessageUpdateEagleControls.Handler.class, MessageUpdateEagleControls.class, packetId++, Side.SERVER);
        NETWORK_WRAPPER.registerMessage(MessageSyncEntityPos.Handler.class, MessageSyncEntityPos.class, packetId++, Side.CLIENT);
        NETWORK_WRAPPER.registerMessage(MessageSyncEntityPos.Handler.class, MessageSyncEntityPos.class, packetId++, Side.SERVER);
        NETWORK_WRAPPER.registerMessage(MessageTarantulaHawkSting.Handler.class, MessageTarantulaHawkSting.class, packetId++, Side.CLIENT);
        NETWORK_WRAPPER.registerMessage(ItemTarantulaHawkElytra.MessageStartGlide.Handler.class, ItemTarantulaHawkElytra.MessageStartGlide.class, packetId++, Side.SERVER);
        NETWORK_WRAPPER.registerMessage(com.github.alexthe666.alexsmobs.entity.util.VineLassoUtil.MessageVineLassoSync.Handler.class, com.github.alexthe666.alexsmobs.entity.util.VineLassoUtil.MessageVineLassoSync.class, packetId++, Side.CLIENT);
    }

    public static void sendMSGToServer(net.minecraftforge.fml.common.network.simpleimpl.IMessage message) {
        NETWORK_WRAPPER.sendToServer(message);
    }

    public static void sendMSGToAll(net.minecraftforge.fml.common.network.simpleimpl.IMessage message) {
        net.minecraft.server.MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
        if (server == null) {
            return;
        }
        for (EntityPlayerMP player : server.getPlayerList().getPlayers()) {
            sendNonLocal(message, player);
        }
    }

    public static void sendNonLocal(net.minecraftforge.fml.common.network.simpleimpl.IMessage msg, EntityPlayerMP player) {
        if (player.mcServer.isDedicatedServer() || player.mcServer.isSinglePlayer() || !player.getName().equals(player.mcServer.getServerOwner())) {
            NETWORK_WRAPPER.sendTo(msg, player);
        }
    }
}
