package com.github.alexthe666.alexsmobs.entity.ai;

import com.github.alexthe666.alexsmobs.entity.EntityMimicube;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.init.Items;
import net.minecraft.item.ItemBow;

public class MimiCubeAIRangedAttack extends EntityAIBase {

    private final EntityMimicube entity;
    private final double moveSpeedAmp;
    private int attackCooldown;
    private final float maxAttackDistance;
    private int attackTime = -1;
    private int seeTime;

    public MimiCubeAIRangedAttack(EntityMimicube mob, double moveSpeedAmpIn, int attackCooldownIn, float maxAttackDistanceIn) {
        this.entity = mob;
        this.moveSpeedAmp = moveSpeedAmpIn;
        this.attackCooldown = attackCooldownIn;
        this.maxAttackDistance = maxAttackDistanceIn * maxAttackDistanceIn;
        this.setMutexBits(3);
    }

    public void setAttackCooldown(int attackCooldownIn) {
        this.attackCooldown = attackCooldownIn;
    }

    @Override
    public boolean shouldExecute() {
        return this.entity.getAttackTarget() != null && this.isBowInMainhand();
    }

    protected boolean isBowInMainhand() {
        return this.entity.shouldShoot();
    }

    @Override
    public boolean shouldContinueExecuting() {
        return this.shouldExecute() || !this.entity.getNavigator().noPath();
    }

    @Override
    public void startExecuting() {
        this.entity.setAggroed(true);
    }

    @Override
    public void resetTask() {
        this.entity.setAggroed(false);
        this.seeTime = 0;
        this.attackTime = -1;
    }

    @Override
    public void updateTask() {
        EntityLivingBase target = this.entity.getAttackTarget();
        if (target != null) {
            double d0 = this.entity.getDistanceSq(target);
            boolean flag = this.entity.canEntityBeSeen(target);
            if (flag) {
                ++this.seeTime;
            } else {
                --this.seeTime;
            }

            if (d0 > (double) this.maxAttackDistance || this.seeTime < 20) {
                this.entity.getNavigator().tryMoveToEntityLiving(target, this.moveSpeedAmp);
            } else {
                this.entity.getNavigator().clearPath();
                this.entity.getLookHelper().setLookPositionWithEntity(target, 30.0F, 30.0F);
                if (--this.attackTime <= 0) {
                    float velocity = ItemBow.getArrowVelocity(20);
                    this.entity.attackEntityWithRangedAttack(target, velocity);
                    this.attackTime = this.attackCooldown;
                }
            }
        }
    }
}
