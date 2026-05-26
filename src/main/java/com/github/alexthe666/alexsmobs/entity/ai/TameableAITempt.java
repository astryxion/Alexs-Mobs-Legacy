package com.github.alexthe666.alexsmobs.entity.ai;

import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.ai.EntityAITempt;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.item.Item;

/**
 * 1.12 {@link EntityAITempt}: allow tempting when the mob is not a tameable, or when it is tamed.
 */
public class TameableAITempt extends EntityAITempt {

    private final EntityCreature tameable;

    public TameableAITempt(EntityCreature tameable, double speedIn, Item temptItem, boolean scaredByPlayerMovementIn) {
        super(tameable, speedIn, temptItem, scaredByPlayerMovementIn);
        this.tameable = tameable;
    }

    @Override
    public boolean shouldExecute() {
        return (!(tameable instanceof EntityTameable) || ((EntityTameable) tameable).isTamed()) && super.shouldExecute();
    }
}
