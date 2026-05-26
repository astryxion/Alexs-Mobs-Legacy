package com.github.alexthe666.alexsmobs.entity.ai;

import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;

/**
 * Forge 1.12.2 port of 1.16 {@code KomodoDragonAITargetHurtAndBabies} (unused in goals; kept for parity).
 */
public class KomodoDragonAITargetHurtAndBabies extends EntityAINearestAttackableTarget {

    public KomodoDragonAITargetHurtAndBabies(EntityCreature goalOwnerIn, Class<?> targetClassIn, boolean checkSight) {
        super(goalOwnerIn, targetClassIn, checkSight);
    }
}
