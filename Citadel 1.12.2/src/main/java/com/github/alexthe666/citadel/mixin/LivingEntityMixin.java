package com.github.alexthe666.citadel.mixin;

import com.github.alexthe666.citadel.server.entity.ICitadelDataEntity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.world.World;

public abstract class LivingEntityMixin extends EntityLivingBase implements ICitadelDataEntity {
   private static final DataParameter<NBTTagCompound> CITADEL_DATA;

   protected LivingEntityMixin(World world) {
      super(world);
   }

   protected void citadel_registerData() {
      this.dataManager.register(CITADEL_DATA, new NBTTagCompound());
   }

   protected void citadel_writeAdditional(NBTTagCompound compoundNBT) {
      NBTTagCompound citadelDat = this.getCitadelEntityData();
      if (citadelDat != null) {
         compoundNBT.setTag("CitadelData", citadelDat);
      }

   }

   protected void citadel_readAdditional(NBTTagCompound compoundNBT) {
      if (compoundNBT.hasKey("CitadelData")) {
         this.setCitadelEntityData(compoundNBT.getCompoundTag("CitadelData"));
      }

   }

   public NBTTagCompound getCitadelEntityData() {
      return (NBTTagCompound)this.dataManager.get(CITADEL_DATA);
   }

   public void setCitadelEntityData(NBTTagCompound nbt) {
      this.dataManager.set(CITADEL_DATA, nbt);
   }

   static {
      CITADEL_DATA = EntityDataManager.createKey(EntityLivingBase.class, DataSerializers.COMPOUND_TAG);
   }
}
