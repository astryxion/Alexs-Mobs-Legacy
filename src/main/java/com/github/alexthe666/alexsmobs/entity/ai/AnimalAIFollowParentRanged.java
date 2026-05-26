package com.github.alexthe666.alexsmobs.entity.ai;

import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.passive.EntityAnimal;

import java.util.Iterator;
import java.util.List;

/**
 * 1.12.2 port of ranged parent-following for baby {@link EntityAnimal} (1.16 used {@code Goal}, {@code AnimalEntity}).
 */
public class AnimalAIFollowParentRanged extends EntityAIBase {
    private final EntityAnimal childAnimal;
    private EntityAnimal parentAnimal;
    private final double moveSpeed;
    private int delayCounter;
    private float range = 8F;
    private float minDist = 3F;

    public AnimalAIFollowParentRanged(EntityAnimal p_i1626_1_, double p_i1626_2_, float range, float minDist) {
        this.childAnimal = p_i1626_1_;
        this.moveSpeed = p_i1626_2_;
        this.range = range;
        this.minDist = minDist;
    }

    @Override
    public boolean shouldExecute() {
        if (this.childAnimal.getGrowingAge() >= 0) {
            return false;
        } else {
            List<EntityAnimal> lvt_1_1_ = this.childAnimal.world.getEntitiesWithinAABB(this.childAnimal.getClass(), this.childAnimal.getEntityBoundingBox().grow(range, range * 0.5D, range));
            EntityAnimal lvt_2_1_ = null;
            double lvt_3_1_ = Double.MAX_VALUE;
            Iterator<EntityAnimal> var5 = lvt_1_1_.iterator();

            while (var5.hasNext()) {
                EntityAnimal lvt_6_1_ = var5.next();
                if (lvt_6_1_.getGrowingAge() >= 0) {
                    double lvt_7_1_ = this.childAnimal.getDistanceSq(lvt_6_1_);
                    if (lvt_7_1_ <= lvt_3_1_) {
                        lvt_3_1_ = lvt_7_1_;
                        lvt_2_1_ = lvt_6_1_;
                    }
                }
            }

            if (lvt_2_1_ == null) {
                return false;
            } else if (lvt_3_1_ < minDist * minDist) {
                return false;
            } else {
                this.parentAnimal = lvt_2_1_;
                return true;
            }
        }
    }

    @Override
    public boolean shouldContinueExecuting() {
        if (this.childAnimal.getGrowingAge() >= 0) {
            return false;
        } else if (!this.parentAnimal.isEntityAlive()) {
            return false;
        } else {
            double lvt_1_1_ = this.childAnimal.getDistanceSq(this.parentAnimal);
            return lvt_1_1_ >= minDist * minDist && lvt_1_1_ <= range * range;
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
    }
}
