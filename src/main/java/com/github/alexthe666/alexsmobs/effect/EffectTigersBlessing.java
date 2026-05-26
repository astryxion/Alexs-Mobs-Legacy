package com.github.alexthe666.alexsmobs.effect;

import net.minecraft.entity.EntityLivingBase;

public class EffectTigersBlessing extends EffectAlexsMobs {

    protected EffectTigersBlessing() {
        super(false, 0XFFD75E, "tigers_blessing");
    }

    public void performEffect(EntityLivingBase entity, int amplifier) {
    }

    public boolean isReady(int duration, int amplifier) {
        return duration > 0;
    }

    public String getName() {
        return "alexsmobs.potion.tigers_blessing";
    }
}
