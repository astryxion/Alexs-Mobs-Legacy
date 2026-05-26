package com.github.alexthe666.alexsmobs.entity.ai;

import com.github.alexthe666.alexsmobs.entity.EntityCrocodile;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIAttackMelee;
import net.minecraft.util.EnumHand;

public class CrocodileAIMelee extends EntityAIAttackMelee {

    private final EntityCrocodile crocodile;

    public CrocodileAIMelee(EntityCrocodile crocodile, double speedIn, boolean useLongMemory) {
        super(crocodile, speedIn, useLongMemory);
        this.crocodile = crocodile;
    }

    @Override
    public boolean shouldExecute() {
        return super.shouldExecute() && this.crocodile.getPassengers().isEmpty();
    }

    @Override
    public boolean shouldContinueExecuting() {
        return super.shouldContinueExecuting() && this.crocodile.getPassengers().isEmpty();
    }

    @Override
    protected void checkAndPerformAttack(EntityLivingBase enemy, double distToEnemySqr) {
        double d0 = this.getAttackReachSqr(enemy);
        if (distToEnemySqr <= d0) {
            this.attackTick = this.attackInterval;
            this.attacker.swingArm(EnumHand.MAIN_HAND);
            this.attacker.attackEntityAsMob(enemy);
        }
    }
}
