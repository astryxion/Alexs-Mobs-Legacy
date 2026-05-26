package com.github.alexthe666.alexsmobs.entity.ai;

import com.github.alexthe666.alexsmobs.entity.EntityStraddler;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;

public class StraddlerAIShoot extends EntityAIBase {

    private final EntityStraddler entity;
    private final double moveSpeedAmp;
    private int attackCooldown;
    private final float maxAttackDistance;
    private int attackTime = -1;
    private int seeTime;
    private boolean strafingClockwise;
    private boolean strafingBackwards;
    private int strafingTime = -1;
    private int animationCooldown = 0;

    public StraddlerAIShoot(EntityStraddler mob, double moveSpeedAmpIn, int attackCooldownIn, float maxAttackDistanceIn) {
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
        return (this.shouldExecute() || !this.entity.getNavigator().noPath()) && this.isBowInMainhand();
    }

    @Override
    public void startExecuting() {
        this.seeTime = 0;
    }

    @Override
    public void resetTask() {
        this.seeTime = 0;
        this.attackTime = -1;
    }

    @Override
    public void updateTask() {
        EntityLivingBase livingentity = this.entity.getAttackTarget();
        if (this.animationCooldown > 0) {
            this.animationCooldown--;
        }
        if (livingentity != null) {
            double d0 = this.entity.getDistanceSq(livingentity.posX, livingentity.posY, livingentity.posZ);
            boolean flag = this.entity.getEntitySenses().canSee(livingentity);
            boolean flag1 = this.seeTime > 0;
            if (flag != flag1) {
                this.seeTime = 0;
            }

            if (flag) {
                ++this.seeTime;
            } else {
                --this.seeTime;
            }

            if (!(d0 > (double) this.maxAttackDistance) && this.seeTime >= 20) {
                this.entity.getNavigator().clearPath();
                ++this.strafingTime;
            } else {
                this.entity.getNavigator().tryMoveToEntityLiving(livingentity, this.moveSpeedAmp);
                this.strafingTime = -1;
            }

            if (this.strafingTime >= 20) {
                if ((double) this.entity.getRNG().nextFloat() < 0.3D) {
                    this.strafingClockwise = !this.strafingClockwise;
                }

                if ((double) this.entity.getRNG().nextFloat() < 0.3D) {
                    this.strafingBackwards = !this.strafingBackwards;
                }

                this.strafingTime = 0;
            }

            if (this.strafingTime > -1) {
                if (d0 > (double) (this.maxAttackDistance * 0.75F)) {
                    this.strafingBackwards = false;
                } else if (d0 < (double) (this.maxAttackDistance * 0.25F)) {
                    this.strafingBackwards = true;
                }

                this.entity.getMoveHelper().strafe(this.strafingBackwards ? -0.5F : 0.5F, this.strafingClockwise ? 0.5F : -0.5F);
                this.entity.getLookHelper().setLookPositionWithEntity(livingentity, 30.0F, 30.0F);
            } else {
                this.entity.getLookHelper().setLookPositionWithEntity(livingentity, 30.0F, 30.0F);
            }
            if (flag) {
                if (this.entity.getAnimation() != EntityStraddler.ANIMATION_LAUNCH) {
                    this.entity.setAnimation(EntityStraddler.ANIMATION_LAUNCH);
                    this.attackTime = this.attackCooldown;
                }
            }
        }
    }
}
