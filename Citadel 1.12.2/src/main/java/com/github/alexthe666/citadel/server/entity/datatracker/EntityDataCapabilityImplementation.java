package com.github.alexthe666.citadel.server.entity.datatracker;

import com.github.alexthe666.citadel.Citadel;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

/** @deprecated */
@Deprecated
public class EntityDataCapabilityImplementation implements IEntityData {
   private Entity entity;

   public static IEntityData getCapability(Entity entity) {
      return (IEntityData)entity.getCapability(Citadel.ENTITY_DATA_CAPABILITY, (EnumFacing)null);
   }

   public void init(Entity entity, World world) {
   }

   public void init(Entity entity, World world, boolean init) {
      this.entity = entity;
      if (init) {
         for(IEntityData entityData : EntityDataHandler.INSTANCE.getEntityData(entity)) {
            entityData.init(entity, world);
         }
      }

   }

   public void saveNBTData(NBTTagCompound compound) {
      for(IEntityData entityData : EntityDataHandler.INSTANCE.getEntityData(this.entity)) {
         NBTTagCompound managerTag = new NBTTagCompound();
         entityData.saveNBTData(managerTag);
         compound.setTag(entityData.getID(), managerTag);
      }

   }

   public void loadNBTData(NBTTagCompound compound) {
      for(IEntityData entityData : EntityDataHandler.INSTANCE.getEntityData(this.entity)) {
         NBTTagCompound managerTag = compound.getCompoundTag(entityData.getID());
         entityData.loadNBTData(managerTag);
      }

   }

   public String getID() {
      return "data_cap_citadel";
   }
}
