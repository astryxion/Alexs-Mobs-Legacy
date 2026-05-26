package com.github.alexthe666.alexsmobs.entity;

import net.minecraft.entity.EntityLivingBase;

public interface IHurtableMultipart {

    void onAttackedFromServer(EntityLivingBase parent, float damage);
}
