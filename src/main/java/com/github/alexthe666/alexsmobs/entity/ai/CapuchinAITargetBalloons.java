package com.github.alexthe666.alexsmobs.entity.ai;

import com.github.alexthe666.alexsmobs.entity.EntityCapuchinMonkey;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.pathfinding.Path;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.scoreboard.Team;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.entity.EntityList;

import javax.annotation.Nullable;
import com.google.common.base.Predicate;

public class CapuchinAITargetBalloons extends EntityAIBase {

    private final EntityCapuchinMonkey monkey;
    protected final boolean shouldCheckSight;
    private final boolean nearbyOnly;
    private int targetSearchStatus;
    private int targetSearchDelay;
    private int targetUnseenTicks;
    protected Entity target;
    protected int unseenMemoryTicks = 60;
    protected final int targetChance;

    public static final Predicate<Entity> TARGET_BLOON = new Predicate<Entity>() {
        @Override
        public boolean apply(Entity balloon) {
            if (EntityList.getKey(balloon) == null) {
                return false;
            }
            String s = EntityList.getKey(balloon).toString();
            return s.contains("balloon") || s.contains("balloom");
        }
    };

    public CapuchinAITargetBalloons(EntityCapuchinMonkey mobIn, boolean checkSight) {
        this(mobIn, checkSight, false, 40);
    }

    public CapuchinAITargetBalloons(EntityCapuchinMonkey mobIn, boolean checkSight, boolean nearbyOnlyIn, int targetChance) {
        this.setMutexBits(1);
        this.monkey = mobIn;
        this.shouldCheckSight = checkSight;
        this.nearbyOnly = nearbyOnlyIn;
        this.targetChance = targetChance;
    }

    @Override
    public boolean shouldExecute() {
        if (this.targetChance > 0 && this.monkey.getRNG().nextInt(this.targetChance) != 0) {
            return false;
        } else {
            this.findNearestTarget();
            return this.target != null;
        }
    }

    protected AxisAlignedBB getTargetableArea(double targetDistance) {
        return this.monkey.getEntityBoundingBox().grow(targetDistance, targetDistance, targetDistance);
    }

    protected void findNearestTarget() {
        Entity closest = null;
        for (Entity bloon : this.monkey.world.getEntitiesWithinAABB(Entity.class, getTargetableArea(getTargetDistance()), TARGET_BLOON)) {
            if (closest == null || closest.getDistance(monkey) > bloon.getDistance(monkey)) {
                closest = bloon;
            }
        }
        this.target = closest;
    }

    @Override
    public boolean shouldContinueExecuting() {
        Entity entity = this.monkey.getDartTarget();
        if (entity == null) {
            entity = this.target;
        }

        if (entity == null) {
            return false;
        } else if (!entity.isEntityAlive()) {
            return false;
        } else {
            Team team = this.monkey.getTeam();
            Team team1 = entity.getTeam();
            if (team != null && team1 == team) {
                return false;
            } else {
                double d0 = this.getTargetDistance();
                if (this.monkey.getDistanceSq(entity) > d0 * d0) {
                    return false;
                } else {
                    if (this.shouldCheckSight) {
                        if (this.monkey.canEntityBeSeen(entity)) {
                            this.targetUnseenTicks = 0;
                        } else if (++this.targetUnseenTicks > this.unseenMemoryTicks) {
                            return false;
                        }
                    }

                    if (entity instanceof EntityPlayer && ((EntityPlayer) entity).capabilities.disableDamage) {
                        return false;
                    } else {
                        this.monkey.setDartTarget(entity);
                        return true;
                    }
                }
            }
        }
    }

    protected double getTargetDistance() {
        return this.monkey.getEntityAttribute(net.minecraft.entity.SharedMonsterAttributes.FOLLOW_RANGE).getAttributeValue();
    }

    @Override
    public void startExecuting() {
        this.monkey.setDartTarget(this.target);
        this.targetSearchStatus = 0;
        this.targetSearchDelay = 0;
        this.targetUnseenTicks = 0;
    }

    @Override
    public void resetTask() {
        this.monkey.setAttackTarget(null);
        this.monkey.setDartTarget(null);
        this.target = null;
    }

    private boolean canEasilyReach(EntityLivingBase target) {
        this.targetSearchDelay = 10 + this.monkey.getRNG().nextInt(5);
        Path path = this.monkey.getNavigator().getPathToEntityLiving(target);
        if (path == null) {
            return false;
        } else {
            PathPoint pathpoint = path.getFinalPathPoint();
            if (pathpoint == null) {
                return false;
            } else {
                int i = pathpoint.x - MathHelper.floor(target.posX);
                int j = pathpoint.z - MathHelper.floor(target.posZ);
                return (double) (i * i + j * j) <= 2.25D;
            }
        }
    }

    public CapuchinAITargetBalloons setUnseenMemoryTicks(int unseenMemoryTicksIn) {
        this.unseenMemoryTicks = unseenMemoryTicksIn;
        return this;
    }
}
