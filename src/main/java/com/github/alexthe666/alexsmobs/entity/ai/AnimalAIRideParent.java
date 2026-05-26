package com.github.alexthe666.alexsmobs.entity.ai;

import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.passive.EntityAnimal;

import java.util.List;

public class AnimalAIRideParent extends EntityAIBase {
    private final EntityAnimal childAnimal;
    private EntityAnimal parentAnimal;
    private final double moveSpeed;
    private int delayCounter;

    public AnimalAIRideParent(EntityAnimal animal, double speed) {
        this.childAnimal = animal;
        this.moveSpeed = speed;
        this.setMutexBits(1);
    }

    @Override
    public boolean shouldExecute() {
        if (this.childAnimal.getGrowingAge() >= 0 || this.childAnimal.isRiding()) {
            return false;
        } else {
            List<EntityAnimal> list = this.childAnimal.world.getEntitiesWithinAABB(this.childAnimal.getClass(), this.childAnimal.getEntityBoundingBox().grow(8.0D, 4.0D, 8.0D));
            EntityAnimal animalentity = null;
            double d0 = Double.MAX_VALUE;

            for (EntityAnimal animalentity1 : list) {
                if (animalentity1.getGrowingAge() >= 0 && !animalentity1.isBeingRidden()) {
                    double d1 = this.childAnimal.getDistanceSq(animalentity1);
                    if (d1 < d0) {
                        d0 = d1;
                        animalentity = animalentity1;
                    }
                }
            }

            if (animalentity == null) {
                return false;
            } else if (d0 < 2.0D) {
                return false;
            } else {
                this.parentAnimal = animalentity;
                return true;
            }
        }
    }

    @Override
    public boolean shouldContinueExecuting() {
        if (this.childAnimal.getGrowingAge() >= 0) {
            return false;
        } else if (parentAnimal == null || !this.parentAnimal.isEntityAlive() || this.parentAnimal.isBeingRidden()) {
            return false;
        } else {
            double d0 = this.childAnimal.getDistanceSq(this.parentAnimal);
            return d0 >= 2.0D && d0 <= 256.0D && this.childAnimal.getRidingEntity() != this.parentAnimal;
        }
    }

    @Override
    public void startExecuting() {
        this.delayCounter = 0;
    }

    @Override
    public void resetTask() {
        this.parentAnimal = null;
    }

    @Override
    public void updateTask() {
        if (--this.delayCounter <= 0) {
            this.delayCounter = 10;
            this.childAnimal.getNavigator().tryMoveToEntityLiving(this.parentAnimal, this.moveSpeed);
        }
        if (this.childAnimal.getDistance(this.parentAnimal) < 2.0D) {
            this.childAnimal.startRiding(this.parentAnimal, true);
            this.resetTask();
        }
    }
}
