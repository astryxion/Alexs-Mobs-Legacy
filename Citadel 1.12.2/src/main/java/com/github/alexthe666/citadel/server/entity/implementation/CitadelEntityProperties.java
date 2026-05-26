package com.github.alexthe666.citadel.server.entity.implementation;

import com.github.alexthe666.citadel.server.entity.datatracker.EntityProperties;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;

public class CitadelEntityProperties extends EntityProperties {
   public int testInteger = 0;

   public void init() {
   }

   public int getTrackingTime() {
      return 20;
   }

   public void saveNBTData(NBTTagCompound compound) {
      compound.setInteger("TestInteger", this.testInteger);
   }

   public void loadNBTData(NBTTagCompound compound) {
      this.testInteger = compound.getInteger("TestInteger");
   }

   public String getID() {
      return "citadel:test_properties";
   }

   public Class getEntityClass() {
      return EntityLivingBase.class;
   }
}
