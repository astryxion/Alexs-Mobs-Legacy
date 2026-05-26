package com.github.alexthe666.alexsmobs.effect;

import net.minecraft.entity.EntityLivingBase;

public class EffectBugPheromones extends EffectAlexsMobs {

    public EffectBugPheromones() {
        super(false, 0X78464B, "bug_pheromones");
    }

    public void performEffect(EntityLivingBase entity, int amplifier) {
    }

    public boolean isReady(int duration, int amplifier) {
        return duration > 0;
    }

    public String getName() {
        return "alexsmobs.potion.bug_pheromones";
    }

}
