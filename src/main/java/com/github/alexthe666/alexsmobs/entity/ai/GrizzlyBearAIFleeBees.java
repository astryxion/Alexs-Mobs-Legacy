package com.github.alexthe666.alexsmobs.entity.ai;

import com.github.alexthe666.alexsmobs.entity.EntityGrizzlyBear;
import com.github.alexthe666.alexsmobs.entity.EntityLeafcutterAnt;
import com.google.common.base.Predicate;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.RandomPositionGenerator;
import net.minecraft.pathfinding.Path;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Forge 1.12.2 port of 1.16 {@code GrizzlyBearAIFleeBees}: flee angry leafcutter ants (vanilla bees do not exist in 1.12).
 */
public class GrizzlyBearAIFleeBees extends EntityAIBase {

    private final double farSpeed;
    private final double nearSpeed;
    private final float avoidDistance;
    private final Predicate<EntityLeafcutterAnt> avoidTargetSelector;
    protected EntityGrizzlyBear entity;
    protected EntityLeafcutterAnt closestLivingEntity;
    private Path path;

    public GrizzlyBearAIFleeBees(EntityGrizzlyBear entityIn, float avoidDistanceIn, double farSpeedIn, double nearSpeedIn) {
        this.avoidTargetSelector = new Predicate<EntityLeafcutterAnt>() {
            @Override
            public boolean apply(@Nullable EntityLeafcutterAnt ant) {
                return ant != null && ant.isEntityAlive()
                        && GrizzlyBearAIFleeBees.this.entity.getEntitySenses().canSee(ant)
                        && !GrizzlyBearAIFleeBees.this.entity.isOnSameTeam(ant)
                        && ant.getAngerTime() > 0;
            }
        };
        this.entity = entityIn;
        this.avoidDistance = avoidDistanceIn;
        this.farSpeed = farSpeedIn;
        this.nearSpeed = nearSpeedIn;
        this.setMutexBits(1);
    }

    @Override
    public boolean shouldExecute() {
        if (this.entity.isTamed()) {
            return false;
        }
        if (this.entity.isSitting() && !entity.forcedSit) {
            this.entity.setSitting(false);
        }
        if (this.entity.isSitting()) {
            return false;
        }
        List<EntityLeafcutterAnt> antEntities = this.entity.world.getEntitiesWithinAABB(
                EntityLeafcutterAnt.class,
                this.entity.getEntityBoundingBox().grow((double) avoidDistance, 8.0D, (double) avoidDistance),
                this.avoidTargetSelector);
        if (antEntities.isEmpty()) {
            return false;
        } else {
            this.closestLivingEntity = antEntities.get(0);
            Vec3d vec3d = RandomPositionGenerator.findRandomTargetBlockAwayFrom(
                    this.entity, 16, 7,
                    new Vec3d(this.closestLivingEntity.posX, this.closestLivingEntity.posY, this.closestLivingEntity.posZ));
            if (vec3d == null) {
                return false;
            } else if (this.closestLivingEntity.getDistanceSq(vec3d.x, vec3d.y, vec3d.z) < this.closestLivingEntity.getDistanceSq(this.entity)) {
                return false;
            } else {
                this.path = entity.getNavigator().getPathToPos(new BlockPos(vec3d.x, vec3d.y, vec3d.z));
                return this.path != null;
            }
        }
    }

    @Override
    public boolean shouldContinueExecuting() {
        return !entity.getNavigator().noPath();
    }

    @Override
    public void startExecuting() {
        entity.getNavigator().setPath(this.path, farSpeed);
    }

    @Override
    public void resetTask() {
        this.entity.getNavigator().clearPath();
        this.closestLivingEntity = null;
    }

    @Override
    public void updateTask() {
        if (closestLivingEntity != null && closestLivingEntity.getAngerTime() <= 0) {
            this.resetTask();
        }
        this.entity.getNavigator().setSpeed(getRunSpeed());
    }

    public double getRunSpeed() {
        return 0.7F;
    }
}
