package com.github.alexthe666.alexsmobs.effect;

import net.minecraft.entity.EntityLivingBase;

public class EffectSoulsteal extends EffectAlexsMobs {

    public EffectSoulsteal() {
        super(false, 0X93FDFF, "soulsteal");
    }

    public void performEffect(EntityLivingBase entity, int amplifier) {
    }

    public boolean isReady(int duration, int amplifier) {
        return duration > 0;
    }

    public String getName() {
        return "alexsmobs.potion.soulsteal";
    }

}
