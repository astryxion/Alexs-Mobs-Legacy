package com.github.alexthe666.alexsmobs.entity.ai;

import com.github.alexthe666.alexsmobs.AlexsMobs;
import com.github.alexthe666.alexsmobs.entity.EntityLeafcutterAnt;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.util.math.Vec3d;

import java.util.List;

/**
 * Forge 1.12.2 port of 1.16 {@code LeafcutterAntAIFollowCaravan}.
 */
public class LeafcutterAntAIFollowCaravan extends EntityAIBase {

    public final EntityLeafcutterAnt LeafcutterAnt;
    private double speedModifier;
    private int distCheckCounter;

    public LeafcutterAntAIFollowCaravan(EntityLeafcutterAnt ant, double speedModifierIn) {
        this.LeafcutterAnt = ant;
        this.speedModifier = speedModifierIn;
        this.setMutexBits(1);
    }

    @Override
    public boolean shouldExecute() {
        if (!this.LeafcutterAnt.shouldLeadCaravan() && !LeafcutterAnt.isChild() && !this.LeafcutterAnt.isQueen()
                && !this.LeafcutterAnt.inCaravan() && !this.LeafcutterAnt.hasLeaf()) {
            double dist = 15D;
            List<EntityLeafcutterAnt> list = LeafcutterAnt.world.getEntitiesWithinAABB(EntityLeafcutterAnt.class,
                    LeafcutterAnt.getEntityBoundingBox().grow(dist, dist / 2, dist));
            EntityLeafcutterAnt best = null;
            double d0 = Double.MAX_VALUE;

            for (Entity entity : list) {
                EntityLeafcutterAnt other = (EntityLeafcutterAnt) entity;
                if (other.inCaravan() && !other.hasCaravanTrail()) {
                    double d1 = this.LeafcutterAnt.getDistanceSq(other);
                    if (d1 <= d0) {
                        d0 = d1;
                        best = other;
                    }
                }
            }

            if (best == null) {
                for (Entity entity1 : list) {
                    EntityLeafcutterAnt other = (EntityLeafcutterAnt) entity1;
                    if (other.shouldLeadCaravan() && !other.hasCaravanTrail()) {
                        double d2 = this.LeafcutterAnt.getDistanceSq(other);
                        if (d2 <= d0) {
                            d0 = d2;
                            best = other;
                        }
                    }
                }
            }

            if (best == null) {
                return false;
            } else if (d0 < 2.0D) {
                return false;
            } else if (!best.shouldLeadCaravan() && !this.firstIsSilverback(best, 1)) {
                return false;
            } else {
                this.LeafcutterAnt.joinCaravan(best);
                return true;
            }
        } else {
            return false;
        }
    }

    @Override
    public boolean shouldContinueExecuting() {
        if (this.LeafcutterAnt.inCaravan() && this.LeafcutterAnt.getCaravanHead() != null
                && this.LeafcutterAnt.getCaravanHead().isEntityAlive()
                && this.firstIsSilverback(this.LeafcutterAnt, 0)) {
            double d0 = this.LeafcutterAnt.getDistanceSq(this.LeafcutterAnt.getCaravanHead());
            if (d0 > 676.0D) {
                if (this.speedModifier <= 1.5D) {
                    this.speedModifier *= 1.2D;
                    this.distCheckCounter = 40;
                    return true;
                }

                if (this.distCheckCounter == 0) {
                    return false;
                }
            }

            if (this.distCheckCounter > 0) {
                --this.distCheckCounter;
            }

            return true;
        } else {
            return false;
        }
    }

    @Override
    public void resetTask() {
        this.LeafcutterAnt.leaveCaravan();
        this.speedModifier = 1.5D;
    }

    @Override
    public void updateTask() {
        if (this.LeafcutterAnt.inCaravan() && !this.LeafcutterAnt.shouldLeadCaravan()) {
            EntityLeafcutterAnt head = this.LeafcutterAnt.getCaravanHead();
            if (head != null) {
                double d0 = (double) this.LeafcutterAnt.getDistance(head);
                Vec3d delta = new Vec3d(head.posX - this.LeafcutterAnt.posX, head.posY - this.LeafcutterAnt.posY, head.posZ - this.LeafcutterAnt.posZ);
                double scale = Math.max(d0 - 2.0D, 0.0D);
                Vec3d n = delta.normalize();
                Vec3d vector3d = new Vec3d(n.x * scale, n.y * scale, n.z * scale);
                if (LeafcutterAnt.getNavigator().noPath()) {
                    try {
                        this.LeafcutterAnt.getNavigator().tryMoveToXYZ(
                                this.LeafcutterAnt.posX + vector3d.x,
                                this.LeafcutterAnt.posY + vector3d.y,
                                this.LeafcutterAnt.posZ + vector3d.z,
                                this.speedModifier);
                    } catch (NullPointerException e) {
                        AlexsMobs.LOGGER.warn("leafcutter ant encountered issue following caravan head");
                    }
                }
            }
        }
    }

    private boolean firstIsSilverback(EntityLeafcutterAnt llama, int depth) {
        if (depth > 8) {
            return false;
        } else if (llama.inCaravan()) {
            if (llama.getCaravanHead().shouldLeadCaravan()) {
                return true;
            } else {
                EntityLeafcutterAnt head = llama.getCaravanHead();
                return this.firstIsSilverback(head, depth + 1);
            }
        } else {
            return false;
        }
    }
}
