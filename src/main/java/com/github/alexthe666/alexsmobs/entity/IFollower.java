package com.github.alexthe666.alexsmobs.entity;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntityTameable;

public interface IFollower {
    boolean shouldFollow();

   default void followEntity(EntityTameable tameable, EntityLivingBase owner, double followSpeed){
       tameable.getNavigator().tryMoveToEntityLiving(owner, followSpeed);
   }
}
