package com.github.alexthe666.alexsmobs.entity.ai;

import com.github.alexthe666.alexsmobs.entity.EntityOrca;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.math.MathHelper;

public class OrcaAIMeleeJump extends JumpGoal {
    private final EntityOrca dolphin;
    private int attackCooldown = 0;
    private boolean inWater;

    public OrcaAIMeleeJump(EntityOrca dolphin) {
        this.dolphin = dolphin;
    }

    @Override
    public boolean shouldExecute() {
        return this.dolphin.getAttackTarget() != null && dolphin.shouldUseJumpAttack(this.dolphin.getAttackTarget()) && !this.dolphin.onGround && dolphin.isInWater() && dolphin.jumpCooldown <= 0;
    }

    @Override
    public boolean shouldContinueExecuting() {
        double d0 = this.dolphin.motionY;
        return dolphin.getAttackTarget() != null && dolphin.jumpCooldown > 0 && (!(d0 * d0 < (double) 0.03F) || this.dolphin.rotationPitch == 0.0F || !(Math.abs(this.dolphin.rotationPitch) < 10.0F) || !this.dolphin.isInWater()) && !this.dolphin.onGround;
    }

    @Override
    public boolean isPreemptible() {
        return false;
    }

    @Override
    public void startExecuting() {
        EntityLivingBase target = this.dolphin.getAttackTarget();
        if (target != null) {
            double distanceXZ = dolphin.getDistanceSq(target.posX, dolphin.posY, target.posZ);
            if (distanceXZ < 150) {
                dolphin.getLookHelper().setLookPositionWithEntity(target, 260, 30);
                double smoothX = MathHelper.clamp(Math.abs(target.posX - dolphin.posX), 0, 1);
                double smoothZ = MathHelper.clamp(Math.abs(target.posZ - dolphin.posZ), 0, 1);
                double d0 = (target.posX - this.dolphin.posX) * 0.3 * smoothX;
                double d2 = (target.posZ - this.dolphin.posZ) * 0.3 * smoothZ;
                float up = 1F + dolphin.getRNG().nextFloat() * 0.8F;
                this.dolphin.motionX += d0 * 0.3D;
                this.dolphin.motionY += up;
                this.dolphin.motionZ += d2 * 0.3D;
                this.dolphin.getNavigator().clearPath();
                this.dolphin.jumpCooldown = dolphin.getRNG().nextInt(32) + 64;
            } else {
                dolphin.getNavigator().tryMoveToEntityLiving(target, 1.0D);
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
            IBlockState state = this.dolphin.world.getBlockState(this.dolphin.getPosition());
            this.inWater = state.getMaterial() == Material.WATER;
        }
        if (attackCooldown > 0) {
            attackCooldown--;
        }
        if (this.inWater && !flag) {
            this.dolphin.playSound(SoundEvents.ENTITY_GENERIC_SPLASH, 1.0F, 1.0F);
        }
        EntityLivingBase target = this.dolphin.getAttackTarget();
        if (target != null) {
            if (this.dolphin.getDistance(target) < 3F && attackCooldown <= 0) {
                this.dolphin.onJumpHit(target);
                attackCooldown = 20;
            } else if (this.dolphin.getDistance(target) < 5F) {
                this.dolphin.setAnimation(EntityOrca.ANIMATION_BITE);
            }
        }

        double motY = this.dolphin.motionY;
        if (motY * motY < (double) 0.1F && this.dolphin.rotationPitch != 0.0F) {
            this.dolphin.rotationPitch = this.dolphin.rotationPitch + MathHelper.wrapDegrees(0.0F - this.dolphin.rotationPitch) * 0.2F;
        } else {
            double horiz = Math.sqrt(this.dolphin.motionX * this.dolphin.motionX + this.dolphin.motionZ * this.dolphin.motionZ);
            double len = Math.sqrt(this.dolphin.motionX * this.dolphin.motionX + motY * motY + this.dolphin.motionZ * this.dolphin.motionZ);
            if (len > 1.0E-4D) {
                double d1 = Math.signum(-motY) * Math.acos(horiz / len) * (double) (180F / (float) Math.PI);
                this.dolphin.rotationPitch = (float) d1;
            }
        }
    }
}
