package com.github.alexthe666.alexsmobs.effect;

import net.minecraft.entity.EntityLivingBase;

public class EffectOiled extends EffectAlexsMobs {

    public EffectOiled() {
        super(false, 0XFFE89C, "oiled");
    }

    public void performEffect(EntityLivingBase entity, int amplifier) {
       if(entity.isInWater()){
           if(!entity.isSneaking()){
               entity.motionY += 0.1D;
           }else{
               entity.fallDistance = 0;
           }
           if (!entity.onGround) {
               entity.motionX *= 1.0D;
               entity.motionY *= 0.9D;
               entity.motionZ *= 1.0D;

           }
       }
    }

    public boolean isReady(int duration, int amplifier) {
        return duration > 0;
    }

    public String getName() {
        return "alexsmobs.potion.oiled";
    }

}
