package com.github.alexthe666.alexsmobs.effect;

import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.EntityLivingBase;

import java.util.UUID;

public class EffectFleetFooted extends EffectAlexsMobs {

    private static final UUID SPRINT_JUMP_SPEED_MODIFIER = UUID.fromString("7E0292F2-9434-48D5-A29F-9583AF7DF29A");
    private static final AttributeModifier SPRINT_JUMP_SPEED_BONUS = new AttributeModifier(SPRINT_JUMP_SPEED_MODIFIER, "fleetfooted speed bonus", 0.2D, 1);
    private int lastDuration = -1;
    private int removeEffectAfter = 0;

    protected EffectFleetFooted() {
        super(false, 0X685441, "fleet_footed");
    }

    @Override
    public void performEffect(EntityLivingBase entity, int amplifier) {
        IAttributeInstance speed = entity.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED);
        if (speed == null) {
            return;
        }
        boolean applyEffect = entity.isSprinting() && !entity.onGround && lastDuration > 2;
        if (removeEffectAfter > 0) {
            removeEffectAfter--;
        }
        if (applyEffect) {
            if (speed.getModifier(SPRINT_JUMP_SPEED_BONUS.getID()) == null) {
                speed.applyModifier(SPRINT_JUMP_SPEED_BONUS);
            }
            removeEffectAfter = 5;
        }
        if (removeEffectAfter <= 0 || lastDuration < 2) {
            speed.removeModifier(SPRINT_JUMP_SPEED_BONUS);
        }
    }

    @Override
    public void removeAttributesModifiersFromEntity(EntityLivingBase entityLivingBaseIn, net.minecraft.entity.ai.attributes.AbstractAttributeMap attributeMapIn, int amplifier) {
        IAttributeInstance speed = entityLivingBaseIn.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED);
        if (speed != null && speed.getModifier(SPRINT_JUMP_SPEED_BONUS.getID()) != null) {
            speed.removeModifier(SPRINT_JUMP_SPEED_BONUS);
        }
    }

    @Override
    public boolean isReady(int duration, int amplifier) {
        lastDuration = duration;
        return duration > 0;
    }
}
