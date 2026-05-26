package com.github.alexthe666.alexsmobs.effect;

import com.github.alexthe666.alexsmobs.entity.AMEntityRegistry;
import com.github.alexthe666.alexsmobs.entity.EntityEnderiophage;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.DamageSource;

public class EffectEnderFlu extends EffectAlexsMobs {

    private int lastDuration = -1;

    public EffectEnderFlu() {
        super(true, 0X6836AA, "ender_flu");
    }

    public void performEffect(EntityLivingBase entity, int amplifier) {
        if (lastDuration == 1) {
            int phages = amplifier + 1;
            entity.attackEntityFrom(DamageSource.MAGIC, phages * 10);
            for (int i = 0; i < phages; i++) {
                EntityEnderiophage phage = (EntityEnderiophage) AMEntityRegistry.ENDERIOPHAGE.newInstance(entity.world);
                phage.setPositionAndRotation(entity.posX, entity.posY, entity.posZ, entity.rotationYaw, entity.rotationPitch);
                phage.onSpawnFromEffect();
                phage.setSkinForDimension();
                if (!entity.world.isRemote) {
                    phage.setStandardFleeTime();
                    entity.world.spawnEntity(phage);
                }
            }
        }
    }

    public boolean isReady(int duration, int amplifier) {
        lastDuration = duration;
        return duration > 0;
    }

    public String getName() {
        return "alexsmobs.potion.ender_flu";
    }

}
