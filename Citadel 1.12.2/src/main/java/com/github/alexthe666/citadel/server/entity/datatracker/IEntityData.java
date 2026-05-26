package com.github.alexthe666.citadel.server.entity.datatracker;

import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

public interface IEntityData<T extends Entity> {
   void init(T var1, World var2);

   default void init(T entity, World world, boolean init) {
      this.init(entity, world);
   }

   void saveNBTData(NBTTagCompound var1);

   void loadNBTData(NBTTagCompound var1);

   String getID();
}
