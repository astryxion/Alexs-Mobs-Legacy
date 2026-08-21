package com.github.alexthe666.alexsmobs.entity.ai;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.stats.StatList;
import net.minecraft.world.World;

import java.util.List;

/**
 * 1.12.2 equivalent of {@link net.minecraft.entity.ai.EntityAIMate} with {@code spawnBaby} exposed for subclasses.
 * Vanilla {@code EntityAIMate#spawnBaby()} is private and cannot be overridden.
 */
public class AnimalAIMate extends EntityAIBase {

    private final EntityAnimal animal;
    private final Class<? extends EntityAnimal> mateClass;
    protected final World world;
    private EntityAnimal targetMate;
    private int spawnBabyDelay;
    private final double moveSpeed;

    public AnimalAIMate(EntityAnimal animal, double moveSpeed) {
        this(animal, moveSpeed, animal.getClass());
    }

    public AnimalAIMate(EntityAnimal animal, double moveSpeed, Class<? extends EntityAnimal> mateClass) {
        this.animal = animal;
        this.world = animal.world;
        this.mateClass = mateClass;
        this.moveSpeed = moveSpeed;
        this.setMutexBits(3);
    }

    protected EntityAnimal getAnimal() {
        return this.animal;
    }

    protected EntityAnimal getTargetMate() {
        return this.targetMate;
    }

    @Override
    public boolean shouldExecute() {
        if (!this.animal.isInLove()) {
            return false;
        }
        this.targetMate = this.getNearbyMate();
        return this.targetMate != null;
    }

    @Override
    public boolean shouldContinueExecuting() {
        return this.targetMate != null && this.targetMate.isEntityAlive() && this.targetMate.isInLove()
                && this.animal.isInLove() && this.spawnBabyDelay < 60;
    }

    @Override
    public void resetTask() {
        this.targetMate = null;
        this.spawnBabyDelay = 0;
    }

    @Override
    public void updateTask() {
        this.animal.getLookHelper().setLookPositionWithEntity(this.targetMate, 10.0F, (float) this.animal.getVerticalFaceSpeed());
        this.animal.getNavigator().tryMoveToEntityLiving(this.targetMate, this.moveSpeed);
        ++this.spawnBabyDelay;
        if (this.spawnBabyDelay >= 60 && this.animal.getDistanceSq(this.targetMate) < getBreedRangeSq()) {
            this.spawnBaby();
        }
    }

    /**
     * Vanilla mate range is 3 blocks, which is too small for wide aquatic mobs. Scale to AABB contact
     * (half combined width) while keeping the 3-block floor for normal animals.
     */
    private double getBreedRangeSq() {
        double range = Math.max(3.0D, (this.animal.width + this.targetMate.width) * 0.5D);
        return range * range;
    }

    private EntityAnimal getNearbyMate() {
        double search = Math.max(8.0D, this.animal.width + 8.0D);
        List<? extends EntityAnimal> list = this.world.getEntitiesWithinAABB(this.mateClass, this.animal.getEntityBoundingBox().grow(search, 4.0D, search));
        double closest = Double.MAX_VALUE;
        EntityAnimal mate = null;
        for (EntityAnimal candidate : list) {
            if (candidate.isInLove() && candidate != this.animal && this.animal.canMateWith(candidate)) {
                double dist = this.animal.getDistanceSq(candidate);
                if (dist < closest) {
                    mate = candidate;
                    closest = dist;
                }
            }
        }
        return mate;
    }

    protected void spawnBaby() {
        EntityAgeable child = this.animal.createChild(this.targetMate);
        EntityPlayerMP breeder = this.animal.getLoveCause();
        if (breeder == null && this.targetMate.getLoveCause() != null) {
            breeder = this.targetMate.getLoveCause();
        }
        if (breeder != null) {
            breeder.addStat(StatList.ANIMALS_BRED);
            CriteriaTriggers.BRED_ANIMALS.trigger(breeder, this.animal, this.targetMate, child);
        }
        if (child != null) {
            child.setGrowingAge(-24000);
            child.setLocationAndAngles(this.animal.posX, this.animal.posY, this.animal.posZ, 0.0F, 0.0F);
            this.world.spawnEntity(child);
        }
        this.animal.setGrowingAge(6000);
        this.targetMate.setGrowingAge(6000);
        this.animal.resetInLove();
        this.targetMate.resetInLove();
    }
}
