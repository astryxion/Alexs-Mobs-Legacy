package com.github.alexthe666.alexsmobs.entity.ai;

import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.passive.EntityAnimal;

public class AnimalAIHurtByTargetNotBaby extends EntityAIHurtByTarget {

    private final EntityCreature creature;

    public AnimalAIHurtByTargetNotBaby(EntityAnimal creatureIn) {
        super(creatureIn, false);
        this.creature = creatureIn;
    }

    public AnimalAIHurtByTargetNotBaby(EntityAnimal creatureIn, boolean callForHelp) {
        super(creatureIn, callForHelp);
        this.creature = creatureIn;
    }

    public AnimalAIHurtByTargetNotBaby(EntityCreature creatureIn, boolean callForHelp) {
        super(creatureIn, callForHelp);
        this.creature = creatureIn;
    }

    public AnimalAIHurtByTargetNotBaby(EntityAnimal creatureIn, Class<?>... excludeReinforcementTypes) {
        super(creatureIn, false, excludeReinforcementTypes);
        this.creature = creatureIn;
    }

    private boolean isBaby() {
        return this.creature instanceof EntityAgeable && ((EntityAgeable) this.creature).isChild();
    }

    @Override
    public void startExecuting() {
        super.startExecuting();
        if (this.isBaby()) {
            this.alertOthers();
            this.resetTask();
        }
    }

    @Override
    protected void setEntityAttackTarget(EntityCreature mobIn, EntityLivingBase targetIn) {
        if (!this.isBaby()) {
            super.setEntityAttackTarget(mobIn, targetIn);
        }
    }
}
