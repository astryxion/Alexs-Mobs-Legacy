package com.github.alexthe666.citadel.server.entity.datatracker;

import com.github.alexthe666.citadel.Citadel;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;

/** @deprecated */
@Deprecated
public enum EntityPropertiesHandler {
   INSTANCE;

   private Map<Class<? extends EntityProperties>, String> propertiesIDMap = new HashMap();
   private Map<Class<? extends Entity>, List<Class<? extends EntityProperties<?>>>> registeredProperties = new HashMap();
   private Map<Class<? extends Entity>, List<String>> entityPropertiesCache = new HashMap();
   private Map<EntityPlayerMP, List<PropertiesTracker<?>>> trackerMap = new WeakIdentityHashMap<EntityPlayerMP, List<PropertiesTracker<?>>>();

   public <E extends Entity, T extends EntityProperties<E>> void registerProperties(Class<T> propertiesClass) {
      T properties;
      try {
         Constructor<T> constructor = propertiesClass.getConstructor();
         properties = (T)(constructor.newInstance());
      } catch (Exception e) {
         Citadel.LOGGER.fatal("Failed to register entity properties", e);
         return;
      }

      if (this.propertiesIDMap.containsValue(properties.getID())) {
         Citadel.LOGGER.fatal("Duplicate entity properties with ID {}", properties.getID());
      } else {
         this.propertiesIDMap.put(propertiesClass, properties.getID());
         Class<E> entityClass = ((EntityProperties)properties).getEntityClass();
         List<Class<? extends EntityProperties<?>>> list = (List)this.registeredProperties.computeIfAbsent(entityClass, (k) -> new ArrayList());
         list.add(propertiesClass);
      }
   }

   public <T extends EntityProperties<?>> T getProperties(Entity entity, Class<T> propertiesClass) {
      return (T)(entity != null && this.propertiesIDMap.containsKey(propertiesClass) ? (EntityProperties)EntityDataHandler.INSTANCE.getEntityData(entity, (String)this.propertiesIDMap.get(propertiesClass)) : null);
   }

   public <T extends Entity> void addTracker(EntityPlayerMP player, T entity) {
      List<String> entityProperties = (List)this.entityPropertiesCache.get(entity.getClass());
      if (entityProperties != null) {
         List<PropertiesTracker<?>> trackerList = (List)this.trackerMap.computeIfAbsent(player, (k) -> new ArrayList());

         for(String propID : entityProperties) {
            IEntityData extendedProperties = EntityDataHandler.INSTANCE.getEntityData(entity, propID);
            if (extendedProperties instanceof EntityProperties) {
               EntityProperties properties = (EntityProperties)extendedProperties;
               if (properties.getTrackingTime() >= 0) {
                  PropertiesTracker<T> tracker = properties.createTracker(entity);
                  tracker.setReady();
                  trackerList.add(tracker);
               }
            }
         }
      }

   }

   public void removeTracker(EntityPlayerMP player, Entity entity) {
      List<PropertiesTracker<?>> trackerList = (List)this.trackerMap.get(player);
      if (trackerList != null && trackerList.size() > 0) {
         Iterator<PropertiesTracker<?>> iterator = trackerList.iterator();

         while(iterator.hasNext()) {
            PropertiesTracker<?> tracker = (PropertiesTracker)iterator.next();
            if (tracker.getEntity().equals(entity)) {
               iterator.remove();
               tracker.removeTracker();
            }
         }
      }

   }

   public boolean hasEntityInCache(Class<? extends Entity> entityClass) {
      return this.entityPropertiesCache.containsKey(entityClass);
   }

   public void addEntityToCache(Class<? extends Entity> entityClass, List<String> propertyIDs) {
      this.entityPropertiesCache.put(entityClass, propertyIDs);
   }

   public Stream<Map.Entry<Class<? extends Entity>, List<Class<? extends EntityProperties<?>>>>> getRegisteredProperties() {
      return this.registeredProperties.entrySet().stream();
   }

   public List<PropertiesTracker<?>> getEntityTrackers(EntityPlayerMP player) {
      return (List)this.trackerMap.get(player);
   }

   public Iterator<Map.Entry<EntityPlayerMP, List<PropertiesTracker<?>>>> getTrackerIterator() {
      return this.trackerMap.entrySet().iterator();
   }
}
