package com.github.alexthe666.alexsmobs.effect;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;

public class EffectSunbird extends EffectAlexsMobs {
    public boolean curse;

    public EffectSunbird(boolean curse) {
        super(curse, 0XFFEAB9, curse ? "sunbird_curse" : "sunbird_blessing");
        this.curse = curse;
    }

    public void performEffect(EntityLivingBase entity, int amplifier) {
        if (curse) {
            if (entity.isElytraFlying()) {
                if (entity instanceof EntityPlayerMP) {
                    ((EntityPlayerMP) entity).clearElytraFlying();
                }
            }
            boolean forceFall = false;
            if (entity instanceof EntityPlayer) {
                EntityPlayer player = (EntityPlayer) entity;
                if (!player.isCreative() || !player.capabilities.isFlying) {
                    forceFall = true;
                }
            }
            if ((forceFall || !(entity instanceof EntityPlayer)) && !entity.onGround) {
                entity.motionY -= 0.2F;
            }
        } else {
            entity.fallDistance = 0.0F;
            if (entity.isElytraFlying()) {
                if (entity.rotationPitch < -10) {
                    float pitchMulti = Math.abs(entity.rotationPitch) / 90F;
                    entity.motionY += 0.02 + pitchMulti * 0.02;
                }
            } else if (!entity.onGround && !entity.isSneaking()) {
                if (entity.motionY < 0.0D) {
                    entity.motionX *= 1.0D;
                    entity.motionY *= 0.6D;
                    entity.motionZ *= 1.0D;
                }
            }

        }
    }

    public boolean isReady(int duration, int amplifier) {
        return duration > 0;
    }

    public String getName() {
        return curse ? "alexsmobs.potion.sunbird_curse" : "alexsmobs.potion.sunbird_blessing";
    }

}
