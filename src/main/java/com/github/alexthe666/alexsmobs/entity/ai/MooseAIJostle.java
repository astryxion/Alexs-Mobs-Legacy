package com.github.alexthe666.alexsmobs.entity.ai;

import com.github.alexthe666.alexsmobs.entity.EntityMoose;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Forge 1.12.2 port of 1.16 {@code MooseAIJostle}.
 */
public class MooseAIJostle extends EntityAIBase {

    protected EntityMoose targetMoose;
    private final EntityMoose moose;
    private final World world;
    private float angle;

    public MooseAIJostle(EntityMoose moose) {
        this.moose = moose;
        this.world = moose.world;
        this.setMutexBits(7);
    }

    @Override
    public boolean shouldExecute() {
        if (this.moose.isJostling() || !moose.isAntlered() || this.moose.isChild() || this.moose.getAttackTarget() != null || this.moose.jostleCooldown > 0) {
            return false;
        }
        if (this.moose.instantlyTriggerJostleAI || this.moose.getRNG().nextInt(30) == 0) {
            this.moose.instantlyTriggerJostleAI = false;
            if (this.moose.getJostlingPartner() instanceof EntityMoose) {
                targetMoose = (EntityMoose) moose.getJostlingPartner();
                return targetMoose.jostleCooldown == 0;
            } else {
                EntityMoose possiblePartner = this.getNearbyMoose();
                if (possiblePartner != null) {
                    this.moose.setJostlingPartner(possiblePartner);
                    possiblePartner.setJostlingPartner(moose);
                    targetMoose = possiblePartner;
                    targetMoose.instantlyTriggerJostleAI = true;
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void startExecuting() {
        this.moose.jostleTimer = 0;
        this.angle = 0;
        setJostleDirection(this.moose.getRNG().nextBoolean());
    }

    public void setJostleDirection(boolean dir) {
        this.moose.jostleDirection = dir;
        this.targetMoose.jostleDirection = dir;
    }

    @Override
    public void resetTask() {
        this.moose.setJostling(false);
        this.moose.setJostlingPartner(null);
        this.moose.jostleTimer = 0;
        this.angle = 0;
        this.moose.getNavigator().clearPath();
        if (this.targetMoose != null) {
            this.targetMoose.setJostling(false);
            this.targetMoose.setJostlingPartner(null);
            this.targetMoose.jostleTimer = 0;
            this.targetMoose = null;
        }
    }

    @Override
    public void updateTask() {
        if (targetMoose != null) {
            this.moose.getLookHelper().setLookPositionWithEntity(targetMoose, 360.0F, 180.0F);
            this.moose.setJostling(true);
            double dist = this.moose.getDistance(targetMoose);
            if (dist < 3.5F) {
                this.moose.getNavigator().clearPath();
                this.moose.moveStrafing = -0.5F;
            } else if (dist > 4F) {
                this.moose.setJostling(false);
                this.moose.getNavigator().tryMoveToEntityLiving(targetMoose, 1.0D);
            } else {
                this.moose.getLookHelper().setLookPositionWithEntity(targetMoose, 360.0F, 180.0F);
                if (moose.jostleDirection) {
                    if (angle < 30) {
                        angle++;
                    }
                    this.moose.moveStrafing = 0;
                    this.moose.moveForward = -0.2F;
                }
                if (!moose.jostleDirection) {
                    if (angle > -30) {
                        angle--;
                    }
                    this.moose.moveStrafing = 0;
                    this.moose.moveForward = 0.2F;
                }
                if (this.moose.getRNG().nextInt(55) == 0 && this.moose.onGround) {
                    moose.pushBackJostling(targetMoose, 0.2F);
                }
                if (this.moose.getRNG().nextInt(25) == 0 && this.moose.onGround) {
                    moose.playJostleSound();
                }
                moose.setJostleAngle(angle);
                if (this.moose.jostleTimer % 60 == 0 || this.moose.getRNG().nextInt(80) == 0) {
                    this.setJostleDirection(!moose.jostleDirection);
                }
                this.moose.jostleTimer++;
                this.targetMoose.jostleTimer++;
                if (this.moose.jostleTimer > 1000) {
                    moose.isAirBorne = true;
                    if (moose.onGround) {
                        moose.pushBackJostling(targetMoose, 0.9F);
                    }
                    if (targetMoose.onGround) {
                        targetMoose.pushBackJostling(moose, 0.9F);
                    }
                    this.moose.jostleTimer = 0;
                    this.targetMoose.jostleTimer = 0;
                    this.moose.jostleCooldown = 500 + this.moose.getRNG().nextInt(2000);
                    this.targetMoose.jostleTimer = 0;
                    this.targetMoose.jostleCooldown = 500 + this.targetMoose.getRNG().nextInt(2000);
                    this.resetTask();
                }
            }
        }
    }

    @Override
    public boolean shouldContinueExecuting() {
        return !this.moose.isChild() && this.moose.isAntlered() && this.moose.getAttackTarget() == null && targetMoose != null && this.targetMoose.isAntlered() && this.targetMoose.isEntityAlive() && moose.jostleCooldown == 0 && targetMoose.jostleCooldown == 0;
    }

    @Nullable
    private EntityMoose getNearbyMoose() {
        List<EntityMoose> listOfMeese = this.world.getEntitiesWithinAABB(EntityMoose.class, this.moose.getEntityBoundingBox().grow(16.0D));
        double best = Double.MAX_VALUE;
        EntityMoose closest = null;
        for (EntityMoose other : listOfMeese) {
            if (other != this.moose && other.isEntityAlive() && this.moose.canJostleWith(other) && this.moose.getDistanceSq(other) < best) {
                closest = other;
                best = this.moose.getDistanceSq(other);
            }
        }
        return closest;
    }
}
