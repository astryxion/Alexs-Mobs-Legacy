package com.github.alexthe666.alexsmobs.entity.ai;

import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.RandomPositionGenerator;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.pathfinding.Path;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.List;

/**
 * Forge 1.12.2 port of 1.16 {@code AnimalAIFleeAdult}.
 */
public class AnimalAIFleeAdult extends EntityAIBase {

    private final EntityAnimal childAnimal;
    private EntityAnimal parentAnimal;
    private final double moveSpeed;
    private final double fleeDistance;
    private Path path;

    public AnimalAIFleeAdult(EntityAnimal animal, double speed, double fleeDistance) {
        this.childAnimal = animal;
        this.moveSpeed = speed;
        this.fleeDistance = fleeDistance;
        this.setMutexBits(1);
    }

    @Override
    public boolean shouldExecute() {
        if (this.childAnimal.getGrowingAge() >= 0) {
            return false;
        } else {
            List<EntityAnimal> list = this.childAnimal.world.getEntitiesWithinAABB(
                    this.childAnimal.getClass(),
                    this.childAnimal.getEntityBoundingBox().grow(fleeDistance, 4.0D, fleeDistance));
            EntityAnimal animalentity = null;
            double d0 = Double.MAX_VALUE;

            for (EntityAnimal animalentity1 : list) {
                if (animalentity1.getGrowingAge() >= 0) {
                    double d1 = this.childAnimal.getDistanceSq(animalentity1);
                    if (d1 <= d0) {
                        d0 = d1;
                        animalentity = animalentity1;
                    }
                }
            }

            if (animalentity == null) {
                return false;
            } else if (d0 > 19.0D) {
                return false;
            } else {
                this.parentAnimal = animalentity;
                Vec3d vec3d = RandomPositionGenerator.findRandomTargetBlockAwayFrom(
                        this.childAnimal,
                        (int) fleeDistance,
                        7,
                        new Vec3d(this.parentAnimal.posX, this.parentAnimal.posY, this.parentAnimal.posZ));
                if (vec3d == null) {
                    return false;
                } else if (this.parentAnimal.getDistanceSq(vec3d.x, vec3d.y, vec3d.z) < this.parentAnimal.getDistanceSq(this.childAnimal)) {
                    return false;
                } else {
                    this.path = childAnimal.getNavigator().getPathToPos(new BlockPos(vec3d.x, vec3d.y, vec3d.z));
                    return this.path != null;
                }
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
            return !childAnimal.getNavigator().noPath();
        }
    }

    @Override
    public void startExecuting() {
        childAnimal.getNavigator().setPath(this.path, moveSpeed);
    }

    @Override
    public void resetTask() {
        this.parentAnimal = null;
        this.childAnimal.getNavigator().clearPath();
        this.path = null;
    }
}
