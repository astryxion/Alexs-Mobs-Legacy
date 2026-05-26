package com.github.alexthe666.alexsmobs.effect;

import net.minecraft.entity.EntityLivingBase;

public class EffectLavaVision extends EffectAlexsMobs {

    public EffectLavaVision() {
        super(false, 0XFF6A00, "lava_vision");
    }

    public void performEffect(EntityLivingBase LivingEntityIn, int amplifier) {
    }

    public boolean isReady(int duration, int amplifier) {
        return duration > 0;
    }

    public String getName() {
        return "alexsmobs.potion.lava_vision";
    }

}
