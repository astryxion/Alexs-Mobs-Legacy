package com.github.alexthe666.alexsmobs.effect;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;

public class EffectKnockbackResistance extends EffectAlexsMobs {

    public EffectKnockbackResistance() {
        super(false, 0X865337, "knockback_resistance");
        this.registerPotionAttributeModifier(SharedMonsterAttributes.KNOCKBACK_RESISTANCE, "03C3C89D-7037-4B42-869F-B146BCB64D2F", 0.5D, 0);
    }

    public void performEffect(EntityLivingBase LivingEntityIn, int amplifier) {
    }

    public boolean isReady(int duration, int amplifier) {
        return duration > 0;
    }

    public String getName() {
        return "alexsmobs.potion.knockback_resistance";
    }

}
