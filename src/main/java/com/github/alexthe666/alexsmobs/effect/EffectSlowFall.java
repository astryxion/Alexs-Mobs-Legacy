package com.github.alexthe666.alexsmobs.effect;

import net.minecraft.entity.EntityLivingBase;

/** 1.20 {@code MobEffects.SLOW_FALLING} — resets fall distance and caps descent speed. */
public class EffectSlowFall extends EffectAlexsMobs {

    public static final double TERMINAL_VELOCITY = -0.58D;

    public EffectSlowFall() {
        super(false, 0xCFE8FF, "slow_fall");
    }

    @Override
    public void performEffect(EntityLivingBase entity, int amplifier) {
        if (!entity.onGround) {
            entity.fallDistance = 0.0F;
            if (entity.motionY < TERMINAL_VELOCITY) {
                entity.motionY = TERMINAL_VELOCITY;
            }
        }
    }

    @Override
    public boolean isReady(int duration, int amplifier) {
        return duration > 0;
    }
}
