package com.github.alexthe666.alexsmobs.entity.ai;

import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.util.math.BlockPos;

public class AnimalAIMeleeNearby extends EntityAIBase {
    private final EntityCreature entity;
    private final int range;
    private final double speed;
    private BlockPos fightStartPos = null;

    public AnimalAIMeleeNearby(EntityCreature entity, int range, double speed) {
        this.setMutexBits(1);
        this.entity = entity;
        this.range = range;
        this.speed = speed;
    }

    @Override
    public boolean shouldExecute() {
        return entity.getAttackTarget() != null && entity.getAttackTarget().isEntityAlive() && !entity.isBeingRidden();
    }

    @Override
    public void startExecuting() {
        fightStartPos = entity.getPosition();
    }

    @Override
    public void resetTask() {
        entity.getNavigator().clearPath();
        fightStartPos = null;
    }

    @Override
    public void updateTask() {
        EntityLivingBase target = entity.getAttackTarget();
        if (target == null) {
            return;
        }
        if (entity.getDistance(target) < 3F + entity.width + target.width) {
            entity.attackEntityAsMob(target);
            entity.getLookHelper().setLookPositionWithEntity(target, 180F, 180F);
        } else if (fightStartPos != null) {
            double distSq = entity.getDistanceSq(fightStartPos.getX() + 0.5D, fightStartPos.getY() + 0.5D, fightStartPos.getZ() + 0.5D);
            if (distSq < (double) (range * range)) {
                entity.getNavigator().tryMoveToEntityLiving(target, speed);
            } else {
                entity.getNavigator().tryMoveToXYZ(fightStartPos.getX() + 0.5D, fightStartPos.getY() + 0.5D, fightStartPos.getZ() + 0.5D, 0.4D + speed);
            }
        }
    }
}
