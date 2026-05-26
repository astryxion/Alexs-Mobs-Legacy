package com.github.alexthe666.citadel.server;

import com.github.alexthe666.citadel.Citadel;
import com.github.alexthe666.citadel.config.ServerConfig;
import com.github.alexthe666.citadel.server.entity.CitadelEntityData;
import com.github.alexthe666.citadel.server.entity.datatracker.EntityDataCapabilityImplementation;
import com.github.alexthe666.citadel.server.entity.datatracker.EntityDataHandler;
import com.github.alexthe666.citadel.server.entity.datatracker.EntityProperties;
import com.github.alexthe666.citadel.server.entity.datatracker.EntityPropertiesHandler;
import com.github.alexthe666.citadel.server.entity.datatracker.IEntityData;
import com.github.alexthe666.citadel.server.entity.datatracker.PropertiesTracker;
import com.github.alexthe666.citadel.server.message.PropertiesMessage;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTBase;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingSpawnEvent.CheckSpawn;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;

public class CitadelEvents {
   private int updateTimer;

   @SubscribeEvent(
      priority = EventPriority.HIGHEST
   )
   public void onAttachCapabilities(final AttachCapabilitiesEvent<Entity> event) {
      event.addCapability(new ResourceLocation("citadel", "extended_entity_data_citadel"), new ICapabilitySerializable() {
         public NBTBase serializeNBT() {
            Capability<IEntityData> capability = Citadel.ENTITY_DATA_CAPABILITY;
            IEntityData instance = (IEntityData)capability.getDefaultInstance();
            instance.init((Entity)event.getObject(), ((Entity)event.getObject()).world, false);
            return capability.getStorage().writeNBT(capability, instance, (EnumFacing)null);
         }

         public void deserializeNBT(NBTBase nbt) {
            Capability<IEntityData> capability = Citadel.ENTITY_DATA_CAPABILITY;
            IEntityData instance = (IEntityData)capability.getDefaultInstance();
            instance.init((Entity)event.getObject(), ((Entity)event.getObject()).world, true);
            capability.getStorage().readNBT(capability, instance, (EnumFacing)null, nbt);
         }

         public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
            return capability == Citadel.ENTITY_DATA_CAPABILITY;
         }

         @Nullable
         public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing side) {
            return capability == Citadel.ENTITY_DATA_CAPABILITY ? Citadel.ENTITY_DATA_CAPABILITY.cast(new EntityDataCapabilityImplementation()) : null;
         }
      });
   }

   @SubscribeEvent
   public void onEntityDestroyed(EntityEvent.EntityConstructing event) {
      if (!(event.getEntity() instanceof EntityPlayer)) {
         EntityDataHandler.INSTANCE.stopTracking(event.getEntity());
      }

   }

   @SubscribeEvent
   public void onEntityConstructing(EntityEvent.EntityConstructing event) {
      if (ServerConfig.citadelEntityTrack) {
         boolean cached = EntityPropertiesHandler.INSTANCE.hasEntityInCache(event.getEntity().getClass());
         List<String> entityPropertiesIDCache = !cached ? new ArrayList() : null;
         EntityPropertiesHandler.INSTANCE.getRegisteredProperties().filter((propEntry) -> ((Class)propEntry.getKey()).isAssignableFrom(event.getEntity().getClass())).forEach((propEntry) -> {
            for(Object propClassObj : (List)propEntry.getValue()) {
               try {
                  Class<? extends EntityProperties> propClass = (Class)propClassObj;
                  Constructor<? extends EntityProperties> constructor = propClass.getConstructor();
                  EntityProperties prop = (EntityProperties)constructor.newInstance();
                  String propID = prop.getID();
                  EntityDataHandler.INSTANCE.registerExtendedEntityData(event.getEntity(), prop);
                  if (!cached) {
                     entityPropertiesIDCache.add(propID);
                  }
               } catch (Exception ex) {
                  ex.printStackTrace();
               }
            }

         });
         if (!cached) {
            EntityPropertiesHandler.INSTANCE.addEntityToCache(event.getEntity().getClass(), entityPropertiesIDCache);
         }
      }

   }

   @SubscribeEvent
   public void onEntityUpdate(LivingEvent.LivingUpdateEvent event) {
      if (!event.getEntity().world.isRemote && event.getEntity() instanceof EntityPlayerMP) {
         EntityPlayerMP player = (EntityPlayerMP)event.getEntity();
         List<PropertiesTracker<?>> trackers = EntityPropertiesHandler.INSTANCE.getEntityTrackers(player);
         if (trackers != null && trackers.size() > 0) {
            boolean hasPlayer = false;

            for(PropertiesTracker tracker : trackers) {
               if (hasPlayer = tracker.getEntity() == player) {
                  break;
               }
            }

            if (!hasPlayer) {
               EntityPropertiesHandler.INSTANCE.addTracker(player, player);
            }

            for(PropertiesTracker<?> tracker : trackers) {
               tracker.updateTracker();
               if (tracker.isTrackerReady()) {
                  tracker.onSync();
                  PropertiesMessage message = new PropertiesMessage(tracker.getProperties(), tracker.getEntity());
                  Citadel.sendNonLocal(message, player);
               }
            }
         }

      }
   }

   @SubscribeEvent
   public void onEntityUpdateDebug(LivingEvent.LivingUpdateEvent event) {
   }

   @SubscribeEvent
   public void onJoinWorld(EntityJoinWorldEvent event) {
      if (!event.getWorld().isRemote && event.getEntity() instanceof EntityPlayerMP) {
         EntityPlayerMP player = (EntityPlayerMP)event.getEntity();
         EntityPropertiesHandler.INSTANCE.addTracker(player, player);
      }

   }

   @SubscribeEvent
   public void onEntityStartTracking(PlayerEvent.StartTracking event) {
      if (event.getEntityPlayer() instanceof EntityPlayerMP) {
         EntityPropertiesHandler.INSTANCE.addTracker((EntityPlayerMP)event.getEntityPlayer(), event.getTarget());
      }

   }

   @SubscribeEvent
   public void onEntityStopTracking(PlayerEvent.StopTracking event) {
      if (event.getEntityPlayer() instanceof EntityPlayerMP) {
         EntityPropertiesHandler.INSTANCE.removeTracker((EntityPlayerMP)event.getEntityPlayer(), event.getTarget());
      }

   }

   @SubscribeEvent
   public void onServerTickEvent(TickEvent.ServerTickEvent event) {
      if (event.phase == Phase.END && ServerConfig.citadelEntityTrack) {
         ++this.updateTimer;
         if (this.updateTimer > 20) {
            this.updateTimer = 0;
            Iterator<Map.Entry<EntityPlayerMP, List<PropertiesTracker<?>>>> iterator = EntityPropertiesHandler.INSTANCE.getTrackerIterator();

            while(iterator.hasNext()) {
               Map.Entry<EntityPlayerMP, List<PropertiesTracker<?>>> trackerEntry = (Map.Entry)iterator.next();
               EntityPlayerMP player = (EntityPlayerMP)trackerEntry.getKey();
               WorldServer playerWorld = (WorldServer)player.world;
               if (player != null && !player.isDead && playerWorld != null) {
                  Iterator<PropertiesTracker<?>> it = ((List)trackerEntry.getValue()).iterator();

                  while(it.hasNext()) {
                     PropertiesTracker tracker = (PropertiesTracker)it.next();
                     Entity entity = tracker.getEntity();
                     WorldServer entityWorld = (WorldServer)entity.world;
                     if (entity == null || entity.isDead || entityWorld == null) {
                        it.remove();
                        tracker.removeTracker();
                     }
                  }
               } else {
                  iterator.remove();
                  ((List<PropertiesTracker<?>>)trackerEntry.getValue()).forEach((tracker) -> tracker.removeTracker());
               }
            }
         }
      }

   }

   @SubscribeEvent
   public void onLoadBiome(CheckSpawn event) {
      if (ServerConfig.chunkGenSpawnModifierVal < 1.0D && event.getWorld().rand.nextDouble() > ServerConfig.chunkGenSpawnModifierVal) {
         event.setResult(net.minecraftforge.fml.common.eventhandler.Event.Result.DENY);
      }
   }

   @SubscribeEvent
   public void onPlayerClone(PlayerEvent.Clone event) {
      if (event.getOriginal() != null && CitadelEntityData.getCitadelTag(event.getOriginal()) != null) {
         CitadelEntityData.setCitadelTag(event.getEntityLiving(), CitadelEntityData.getCitadelTag(event.getOriginal()));
      }

   }
}
