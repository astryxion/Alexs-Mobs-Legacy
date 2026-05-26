package com.github.alexthe666.alexsmobs.entity.ai;

import com.github.alexthe666.alexsmobs.entity.EntitySeal;
import net.minecraft.entity.ai.EntityAIBase;

/**
 * Forge 1.12.2 port of 1.16 {@code SealAIBask}.
 */
public class SealAIBask extends EntityAIBase {

    private final EntitySeal seal;

    public SealAIBask(EntitySeal seal) {
        this.seal = seal;
        this.setMutexBits(5);
    }

    @Override
    public boolean shouldContinueExecuting() {
        return this.seal.isBasking() && !this.seal.isInWater();
    }

    @Override
    public boolean shouldExecute() {
        if (this.seal.isInWater()) {
            return false;
        }
        return seal.getRevengeTarget() == null && seal.isBasking();
    }

    @Override
    public void updateTask() {
        this.seal.getNavigator().clearPath();
    }

    @Override
    public void resetTask() {
        this.seal.setBasking(false);
    }
}
