package com.github.alexthe666.citadel.server.entity;

import net.minecraft.nbt.NBTTagCompound;

public interface ICitadelDataEntity {
   NBTTagCompound getCitadelEntityData();

   void setCitadelEntityData(NBTTagCompound var1);
}
