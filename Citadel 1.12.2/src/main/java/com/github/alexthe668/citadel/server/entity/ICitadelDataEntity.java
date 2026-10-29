package com.github.alexthe668.citadel.server.entity;

import net.minecraft.nbt.NBTTagCompound;

public interface ICitadelDataEntity {
   NBTTagCompound getCitadelEntityData();

   void setCitadelEntityData(NBTTagCompound var1);
}
