package com.github.alexthe666.citadel.server.entity;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;

public class CitadelEntityData {
   public static NBTTagCompound getOrCreateCitadelTag(EntityLivingBase entity) {
      NBTTagCompound tag = getCitadelTag(entity);
      return tag == null ? new NBTTagCompound() : tag;
   }

   public static NBTTagCompound getCitadelTag(EntityLivingBase entity) {
      return entity instanceof ICitadelDataEntity ? ((ICitadelDataEntity)entity).getCitadelEntityData() : new NBTTagCompound();
   }

   public static void setCitadelTag(EntityLivingBase entity, NBTTagCompound tag) {
      if (entity instanceof ICitadelDataEntity) {
         ((ICitadelDataEntity)entity).setCitadelEntityData(tag);
      }

   }
}
