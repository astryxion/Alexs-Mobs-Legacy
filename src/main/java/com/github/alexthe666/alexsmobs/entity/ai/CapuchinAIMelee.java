package com.github.alexthe666.alexsmobs.entity.ai;

import com.github.alexthe666.alexsmobs.entity.EntityCapuchinMonkey;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIAttackMelee;

public class CapuchinAIMelee extends EntityAIAttackMelee {

    private final EntityCapuchinMonkey monkey;

    public CapuchinAIMelee(EntityCapuchinMonkey monkey, double speedIn, boolean useLongMemory) {
        super(monkey, speedIn, useLongMemory);
        this.monkey = monkey;
    }

    @Override
    public boolean shouldExecute() {
        return super.shouldExecute() && !monkey.attackDecision;
    }

    @Override
    public boolean shouldContinueExecuting() {
        return super.shouldContinueExecuting() && !monkey.attackDecision;
    }

    @Override
    protected void checkAndPerformAttack(EntityLivingBase enemy, double distToEnemySqr) {
        double d0 = this.getAttackReachSqr(enemy);
        if (distToEnemySqr <= d0) {
            this.attackTick = this.attackInterval;
            this.attacker.swingArm(net.minecraft.util.EnumHand.MAIN_HAND);
            this.attacker.attackEntityAsMob(enemy);
        }
    }
}
