package com.github.alexthe666.alexsmobs.effect;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;

public class EffectExsanguination extends EffectAlexsMobs {

    private int lastDuration = -1;

    protected EffectExsanguination() {
        super(true, 0XED5151, "exsanguination");
    }

    public void performEffect(EntityLivingBase entity, int amplifier) {
        entity.attackEntityFrom(DamageSource.MAGIC, Math.min(amplifier + 1, Math.round(lastDuration / 20F)));
        for(int i = 0; i < 3; i++){
            entity.world.spawnParticle(EnumParticleTypes.DAMAGE_INDICATOR, entity.posX + (entity.world.rand.nextDouble() - 0.5D), entity.posY + entity.world.rand.nextDouble(), entity.posZ + (entity.world.rand.nextDouble() - 0.5D), 0, 0, 0);
        }
    }

    public boolean isReady(int duration, int amplifier) {
        lastDuration = duration;
        return duration > 0 && duration % 20 == 0;
    }

    public String getName() {
        return "alexsmobs.potion.exsanguination";
    }

}
