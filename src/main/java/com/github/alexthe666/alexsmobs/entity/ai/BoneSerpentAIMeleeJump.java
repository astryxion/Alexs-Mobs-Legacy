package com.github.alexthe666.alexsmobs.entity.ai;

import com.github.alexthe666.alexsmobs.entity.EntityBoneSerpent;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class BoneSerpentAIMeleeJump extends JumpGoal {
    private final EntityBoneSerpent dolphin;
    private int attackCooldown = 0;
    private boolean inWater;

    public BoneSerpentAIMeleeJump(EntityBoneSerpent dolphin) {
        this.dolphin = dolphin;
    }

    @Override
    public boolean shouldExecute() {
        if (this.dolphin.getAttackTarget() == null || this.dolphin.onGround
                || (!this.dolphin.isInLava() && !this.dolphin.isInWater()) || this.dolphin.jumpCooldown > 0) {
            return false;
        } else {
            return true;
        }
    }

    @Override
    public boolean shouldContinueExecuting() {
            double d0 = this.dolphin.motionY;
            return this.dolphin.getAttackTarget() != null && this.dolphin.jumpCooldown > 0
                    && (!(d0 * d0 < (double) 0.03F) || this.dolphin.rotationPitch == 0.0F || !(Math.abs(this.dolphin.rotationPitch) < 10.0F) || !this.dolphin.isInWater())
                    && !this.dolphin.onGround;
    }

    @Override
    public boolean isPreemptible() {
        return false;
    }

    @Override
    public void startExecuting() {
        EntityLivingBase target = this.dolphin.getAttackTarget();
        if (target != null) {
            double distanceSq = this.dolphin.getDistanceSq(target.posX, this.dolphin.posY, target.posZ);
            if (distanceSq < 150) {
                this.dolphin.getLookHelper().setLookPositionWithEntity(target, 260, 30);
                double smoothX = MathHelper.clamp(Math.abs(target.posX - this.dolphin.posX), 0, 1);
                double smoothY = MathHelper.clamp(Math.abs(target.posY - this.dolphin.posY), 0, 1);
                double smoothZ = MathHelper.clamp(Math.abs(target.posZ - this.dolphin.posZ), 0, 1);
                double d0 = (target.posX - this.dolphin.posX) * 0.3 * smoothX;
                double d1 = Math.signum(target.posY - this.dolphin.posY);
                double d2 = (target.posZ - this.dolphin.posZ) * 0.3 * smoothZ;
                float up = 1F + this.dolphin.getRNG().nextFloat() * 0.8F;
                this.dolphin.motionX += d0 * 0.3D;
                this.dolphin.motionY += up;
                this.dolphin.motionZ += d2 * 0.3D;
                this.dolphin.getNavigator().clearPath();
                this.dolphin.jumpCooldown = this.dolphin.getRNG().nextInt(32) + 64;
            } else {
                this.dolphin.getNavigator().tryMoveToEntityLiving(target, 1.0D);
            }
        }
    }

    @Override
    public void resetTask() {
        this.dolphin.rotationPitch = 0.0F;
        this.attackCooldown = 0;
    }

    @Override
    public void updateTask() {
        boolean flag = this.inWater;
        if (!flag) {
            Material mat = this.dolphin.world.getBlockState(this.dolphin.getPosition()).getMaterial();
            this.inWater = mat == Material.LAVA || mat == Material.WATER;
        }
        if (this.attackCooldown > 0) {
            this.attackCooldown--;
        }
        if (this.inWater && !flag) {
            this.dolphin.playSound(SoundEvents.ENTITY_GENERIC_SPLASH, 1.0F, 1.0F);
        }
        EntityLivingBase target = this.dolphin.getAttackTarget();
        if (target != null) {
            if (this.dolphin.getDistance(target) < 3F && this.attackCooldown <= 0) {
                this.dolphin.attackEntityAsMob(target);
                this.attackCooldown = 20;
            }
        }

        double vHoriz = this.dolphin.motionX * this.dolphin.motionX + this.dolphin.motionZ * this.dolphin.motionZ;
        if (this.dolphin.motionY * this.dolphin.motionY < (double) 0.1F && this.dolphin.rotationPitch != 0.0F) {
            this.dolphin.rotationPitch = this.dolphin.rotationPitch + (0.0F - this.dolphin.rotationPitch) * 0.2F;
        } else {
            Vec3d vel = new Vec3d(this.dolphin.motionX, this.dolphin.motionY, this.dolphin.motionZ);
            double d0 = Math.sqrt(vHoriz);
            double d1 = Math.signum(-this.dolphin.motionY) * Math.acos(d0 / vel.lengthVector()) * (double) (180F / (float) Math.PI);
            this.dolphin.rotationPitch = (float) d1;
        }
    }
}
