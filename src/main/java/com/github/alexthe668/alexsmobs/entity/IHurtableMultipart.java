package com.github.alexthe668.alexsmobs.entity;

import net.minecraft.entity.EntityLivingBase;

public interface IHurtableMultipart {

    void onAttackedFromServer(EntityLivingBase parent, float damage);
}
