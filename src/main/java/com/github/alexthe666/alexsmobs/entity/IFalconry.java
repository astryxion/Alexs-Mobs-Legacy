package com.github.alexthe666.alexsmobs.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;

public interface IFalconry {
    void onLaunch(EntityPlayer player, Entity pointedEntity);

    default int getRidingFalcons(EntityLivingBase player) {
        int count = 0;
        for (Entity e : player.getPassengers()) {
            if (e instanceof IFalconry) {
                count++;
            }
        }
        return count;
    }

    float getHandOffset();
}
