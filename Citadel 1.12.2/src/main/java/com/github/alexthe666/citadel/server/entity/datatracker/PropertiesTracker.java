package com.github.alexthe666.citadel.server.entity.datatracker;

import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;

public class PropertiesTracker<T extends Entity> {
   private int trackingTimer = 0;
   private int trackingUpdateTimer = 0;
   private boolean trackerReady = false;
   private boolean trackerDataChanged = false;
   private NBTTagCompound prevTrackerData = new NBTTagCompound();
   private T entity;
   private EntityProperties properties;

   public PropertiesTracker(T entity, EntityProperties<T> properties) {
      this.entity = entity;
      this.properties = properties;
   }

   public void updateTracker() {
      int trackingFrequency = this.properties.getTrackingTime();
      if (trackingFrequency >= 0 && !this.trackerReady) {
         ++this.trackingTimer;
         if (this.trackingTimer >= trackingFrequency) {
            this.trackerReady = true;
         }
      }

      int trackingUpdateFrequency = this.properties.getTrackingUpdateTime();
      if (this.trackingUpdateTimer < trackingUpdateFrequency) {
         ++this.trackingUpdateTimer;
      }

      if (this.trackingUpdateTimer >= trackingUpdateFrequency && !this.trackerDataChanged) {
         this.trackingUpdateTimer = 0;
         NBTTagCompound currentTrackingData = new NBTTagCompound();
         this.properties.saveTrackingSensitiveData(currentTrackingData);
         if (!currentTrackingData.equals(this.prevTrackerData)) {
            this.trackerDataChanged = true;
         }

         this.prevTrackerData = currentTrackingData;
      }

   }

   public void setReady() {
      this.trackerReady = true;
      this.trackerDataChanged = true;
   }

   public boolean isTrackerReady() {
      boolean ready = this.properties.getTrackingTime() >= 0 && this.trackerReady && this.trackerDataChanged;
      if (ready) {
         this.trackingTimer = 0;
         this.trackerReady = false;
         this.trackerDataChanged = false;
         return true;
      } else {
         return false;
      }
   }

   public void onSync() {
      this.properties.onSync();
   }

   public EntityProperties getProperties() {
      return this.properties;
   }

   public T getEntity() {
      return this.entity;
   }

   public void removeTracker() {
      this.properties.getTrackers().remove(this);
   }
}
