package com.github.alexthe666.alexsmobs.entity.ai;

import com.github.alexthe666.alexsmobs.AlexsMobs;
import com.github.alexthe666.alexsmobs.entity.EntityElephant;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.util.math.Vec3d;

import java.util.List;

public class ElephantAIFollowCaravan extends EntityAIBase {
    public final EntityElephant elephant;
    private double speedModifier;
    private int distCheckCounter;

    public ElephantAIFollowCaravan(EntityElephant llamaIn, double speedModifierIn) {
        this.elephant = llamaIn;
        this.speedModifier = speedModifierIn;
        this.setMutexBits(1);
    }

    @Override
    public boolean shouldExecute() {
        if (elephant.aiItemFlag || elephant.getControllingPassenger() != null) {
            return false;
        }
        if (!this.elephant.isTusked() && !this.elephant.inCaravan() && !elephant.isSitting()) {
            double dist = 32D;
            List<EntityElephant> list = elephant.world.getEntitiesWithinAABB(EntityElephant.class, elephant.getEntityBoundingBox().grow(dist, dist / 2, dist));
            EntityElephant head = null;
            double d0 = Double.MAX_VALUE;

            for (Entity entity : list) {
                EntityElephant elephant1 = (EntityElephant) entity;
                if (elephant1.inCaravan() && !elephant1.hasCaravanTrail()) {
                    double d1 = this.elephant.getDistanceSq(elephant1);
                    if (!(d1 > d0)) {
                        d0 = d1;
                        head = elephant1;
                    }
                }
            }

            if (head == null) {
                for (Entity entity1 : list) {
                    EntityElephant llamaentity2 = (EntityElephant) entity1;
                    if (llamaentity2.isTusked() && !llamaentity2.isChild() && !llamaentity2.hasCaravanTrail()) {
                        double d2 = this.elephant.getDistanceSq(llamaentity2);
                        if (!(d2 > d0)) {
                            d0 = d2;
                            head = llamaentity2;
                        }
                    }
                }
            }

            if (head == null) {
                return false;
            } else if (d0 < 5.0D) {
                return false;
            } else if (!head.isTusked() && !head.isChild() && !this.firstIsTusk(head, 1)) {
                return false;
            } else {
                this.elephant.joinCaravan(head);
                return true;
            }
        } else {
            return false;
        }
    }

    @Override
    public boolean shouldContinueExecuting() {
        if (elephant.isSitting() || elephant.aiItemFlag) {
            return false;
        }
        if (this.elephant.inCaravan() && this.elephant.getCaravanHead().isEntityAlive() && this.firstIsTusk(this.elephant, 0)) {
            double d0 = this.elephant.getDistanceSq(this.elephant.getCaravanHead());
            if (d0 > 676.0D) {
                if (this.speedModifier <= 1D) {
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
        this.elephant.leaveCaravan();
        this.speedModifier = 1D;
    }

    @Override
    public void updateTask() {
        if (this.elephant.inCaravan() && !this.elephant.isSitting()) {
            EntityElephant llamaentity = this.elephant.getCaravanHead();
            if (llamaentity != null) {
                double d0 = this.elephant.getDistance(llamaentity);
                Vec3d vector3d = (new Vec3d(llamaentity.posX - this.elephant.posX, llamaentity.posY - this.elephant.posY, llamaentity.posZ - this.elephant.posZ)).normalize().scale(Math.max(d0 - 4.0D, 0.0D));
                if (elephant.getNavigator().noPath()) {
                    try {
                        this.elephant.getNavigator().tryMoveToXYZ(this.elephant.posX + vector3d.x, this.elephant.posY + vector3d.y, this.elephant.posZ + vector3d.z, this.speedModifier);
                    } catch (NullPointerException e) {
                        AlexsMobs.LOGGER.warn("elephant encountered issue following caravan head");
                    }
                }

            }
        }
    }

    private boolean firstIsTusk(EntityElephant llama, int p_190858_2_) {
        if (p_190858_2_ > 8) {
            return false;
        } else if (llama.inCaravan()) {
            if (llama.getCaravanHead().isTusked() && !llama.getCaravanHead().isChild()) {
                return true;
            } else {
                EntityElephant llamaentity = llama.getCaravanHead();
                ++p_190858_2_;
                return this.firstIsTusk(llamaentity, p_190858_2_);
            }
        } else {
            return false;
        }
    }
}
