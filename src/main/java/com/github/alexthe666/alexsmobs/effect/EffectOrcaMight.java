package com.github.alexthe666.alexsmobs.effect;

import net.minecraft.entity.SharedMonsterAttributes;

public class EffectOrcaMight extends EffectAlexsMobs {

    public EffectOrcaMight() {
        super(false, 0X4A4A52, "orcas_might");
        this.registerPotionAttributeModifier(SharedMonsterAttributes.ATTACK_SPEED, "03C3C89D-7037-4B42-869F-B146BCB64D3A", 3D, 0);
    }

    public boolean isReady(int duration, int amplifier) {
        return duration > 0;
    }

    public String getName() {
        return "alexsmobs.potion.orcas_might";
    }

}
