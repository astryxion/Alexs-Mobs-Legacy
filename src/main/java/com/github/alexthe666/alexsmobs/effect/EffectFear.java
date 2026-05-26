package com.github.alexthe666.alexsmobs.effect;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;

public class EffectFear extends EffectAlexsMobs {

    protected EffectFear() {
        super(false, 0X7474F7, "fear");
        this.registerPotionAttributeModifier(SharedMonsterAttributes.MOVEMENT_SPEED, "7107DE5E-7CE8-4030-940E-514C1F160890", (double)-1.0F, 1);
    }

    public void performEffect(EntityLivingBase entity, int amplifier) {
        if(entity.motionY > 0 && !entity.isInWater()){
            entity.motionY = 0;
        }
    }

    public boolean isReady(int duration, int amplifier) {
        return duration > 0;
    }

    public String getName() {
        return "alexsmobs.potion.fear";
    }
}
