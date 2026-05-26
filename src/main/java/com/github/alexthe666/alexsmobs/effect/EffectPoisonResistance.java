package com.github.alexthe666.alexsmobs.effect;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.MobEffects;

public class EffectPoisonResistance extends EffectAlexsMobs {

    public EffectPoisonResistance() {
        super(false, 0X51FFAF, "poison_resistance");
    }

    public void performEffect(EntityLivingBase LivingEntityIn, int amplifier) {
        if(LivingEntityIn.isPotionActive(MobEffects.POISON)){
            LivingEntityIn.removePotionEffect(MobEffects.POISON);
        }
    }

    public boolean isReady(int duration, int amplifier) {
        return duration > 0;
    }

    public String getName() {
        return "alexsmobs.potion.poison_resistance";
    }

}
