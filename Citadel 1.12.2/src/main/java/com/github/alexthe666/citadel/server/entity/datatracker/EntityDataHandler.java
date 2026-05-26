package com.github.alexthe666.citadel.server.entity.datatracker;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.minecraft.entity.Entity;

public enum EntityDataHandler {
   INSTANCE;

   private Map<Entity, List<IEntityData<?>>> registeredEntityData = new WeakIdentityHashMap<Entity, List<IEntityData<?>>>();

   public <T extends Entity> void registerExtendedEntityData(T entity, IEntityData<T> entityData) {
      List<IEntityData<?>> registered = (List)this.registeredEntityData.get(entity);
      if (registered == null) {
         registered = new ArrayList();
      }

      if (!registered.contains(entityData)) {
         registered.add(entityData);
      }

      this.registeredEntityData.put(entity, registered);
   }

   public <T extends Entity> void stopTracking(T entity) {
      this.registeredEntityData.remove(entity);
   }

   public <T extends Entity> IEntityData<T> getEntityData(T entity, String identifier) {
      List<IEntityData<T>> managers = this.getEntityData(entity);
      if (managers != null) {
         for(IEntityData manager : managers) {
            if (manager.getID().equals(identifier)) {
               return manager;
            }
         }
      }

      return null;
   }

   public <T extends Entity> List<IEntityData<T>> getEntityData(T entity) {
      return (List<IEntityData<T>>)(this.registeredEntityData.containsKey(entity) ? (List)this.registeredEntityData.get(entity) : new ArrayList());
   }
}
