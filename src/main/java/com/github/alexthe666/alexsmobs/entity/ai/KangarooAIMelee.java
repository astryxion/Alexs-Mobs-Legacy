package com.github.alexthe666.alexsmobs.entity.ai;

import com.github.alexthe666.alexsmobs.entity.EntityKangaroo;
import com.github.alexthe666.citadel.animation.IAnimatedEntity;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIAttackMelee;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

import java.util.Random;

public class KangarooAIMelee extends EntityAIAttackMelee {

    private final EntityKangaroo kangaroo;
    private BlockPos waterPos;
    private int waterCheckTick = 0;
    private int waterTimeout = 0;

    public KangarooAIMelee(EntityKangaroo kangaroo, double speedIn, boolean useLongMemory) {
        super(kangaroo, speedIn, useLongMemory);
        this.kangaroo = kangaroo;
    }

    @Override
    public boolean shouldExecute() {
        return super.shouldExecute();
    }

    @Override
    public void updateTask() {
        boolean dontSuper = false;
        EntityLivingBase target = kangaroo.getAttackTarget();
        if (target != null) {
            if (target == kangaroo.getRevengeTarget()) {
                if (target.getDistance(kangaroo) < kangaroo.width + 1F && target.isInWater()) {
                    target.motionY -= 0.09D;
                    target.setAir(target.getAir() - 30);
                }
                if (waterPos == null || kangaroo.world.getBlockState(waterPos).getMaterial() != Material.WATER) {
                    kangaroo.setVisualFlag(0);
                    waterCheckTick++;
                    waterPos = generateWaterPos();
                } else {
                    kangaroo.setPathPriority(PathNodeType.WATER, 0.0F);
                    double localSpeed = MathHelper.clamp(kangaroo.getDistanceSq(waterPos.getX(), waterPos.getY(), waterPos.getZ()) * 0.5F, 1D, 2.3D);
                    kangaroo.getNavigator().tryMoveToXYZ(waterPos.getX(), waterPos.getY(), waterPos.getZ(), localSpeed);
                    if (kangaroo.isInWater()) {
                        waterTimeout++;
                    }
                    if (waterTimeout < 1400) {
                        dontSuper = true;
                        checkAndPerformAttack(target, kangaroo.getDistanceSq(target));
                    }
                    if (kangaroo.isInWater() || kangaroo.getDistanceSq(waterPos.getX() + 0.5D, waterPos.getY(), waterPos.getZ() + 0.5D) < 10) {
                        kangaroo.totalMovingProgress = 0;
                    }
                    if (kangaroo.getDistanceSq(waterPos.getX() + 0.5D, waterPos.getY(), waterPos.getZ() + 0.5D) > 10) {
                        kangaroo.setVisualFlag(0);
                    }
                    if (kangaroo.getDistanceSq(waterPos.getX() + 0.5D, waterPos.getY(), waterPos.getZ() + 0.5D) < 3 && kangaroo.isInWater()) {
                        kangaroo.setStanding(true);
                        kangaroo.maxStandTime = 100;
                        kangaroo.getLookHelper().setLookPositionWithEntity(target, 360.0F, 180.0F);
                        kangaroo.setVisualFlag(1);
                    }
                }
            }
            if (!dontSuper) {
                super.updateTask();
            }
        }
    }

    @Override
    public boolean shouldContinueExecuting() {
        return waterPos != null && this.kangaroo.getAttackTarget() != null || super.shouldContinueExecuting();
    }

    @Override
    public void resetTask() {
        super.resetTask();
        waterCheckTick = 0;
        waterTimeout = 0;
        waterPos = null;
        kangaroo.setVisualFlag(0);
        kangaroo.setPathPriority(PathNodeType.WATER, 8.0F);
    }

    public BlockPos generateWaterPos() {
        BlockPos blockpos = null;
        Random random = new Random();
        int range = 15;
        for (int i = 0; i < 15; i++) {
            BlockPos blockpos1 = this.kangaroo.getPosition().add(random.nextInt(range) - range / 2, 3, random.nextInt(range) - range / 2);
            while (this.kangaroo.world.isAirBlock(blockpos1) && blockpos1.getY() > 1) {
                blockpos1 = blockpos1.down();
            }
            if (this.kangaroo.world.getBlockState(blockpos1).getMaterial() == Material.WATER) {
                blockpos = blockpos1;
            }
        }
        return blockpos;
    }

    @Override
    protected void checkAndPerformAttack(EntityLivingBase enemy, double distToEnemySqr) {
        double d0 = this.getAttackReachSqr(enemy) + 5D;
        if (distToEnemySqr <= d0) {
            if (kangaroo.isInWater()) {
                float f1 = kangaroo.rotationYaw * ((float) Math.PI / 180F);
                kangaroo.motionX += (double) (-MathHelper.sin(f1) * 0.3F);
                kangaroo.motionZ += (double) (MathHelper.cos(f1) * 0.3F);
                enemy.knockBack(kangaroo, 1F, enemy.posX - kangaroo.posX, enemy.posZ - kangaroo.posZ);
            }
            this.attackTick = this.attackInterval;
            if (kangaroo.getAnimation() == IAnimatedEntity.NO_ANIMATION) {
                if (kangaroo.getRNG().nextBoolean()) {
                    kangaroo.setAnimation(EntityKangaroo.ANIMATION_KICK);
                } else {
                    if (!kangaroo.getHeldItemMainhand().isEmpty()) {
                        kangaroo.setAnimation(kangaroo.isLeftHanded() ? EntityKangaroo.ANIMATION_PUNCH_L : EntityKangaroo.ANIMATION_PUNCH_R);
                    } else {
                        kangaroo.setAnimation(kangaroo.getRNG().nextBoolean() ? EntityKangaroo.ANIMATION_PUNCH_R : EntityKangaroo.ANIMATION_PUNCH_L);
                    }
                }
            }
        }
    }
}
