package com.github.alexthe666.citadel.server.entity.datatracker;

import javax.annotation.Nullable;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;

public class EntityDataCapabilityStorage implements Capability.IStorage<IEntityData> {
   @Nullable
   public NBTBase writeNBT(Capability<IEntityData> capability, IEntityData instance, EnumFacing side) {
      NBTTagCompound compound = new NBTTagCompound();
      instance.saveNBTData(compound);
      return compound;
   }

   public void readNBT(Capability<IEntityData> capability, IEntityData instance, EnumFacing side, NBTBase nbt) {
      instance.loadNBTData((NBTTagCompound)nbt);
   }
}
