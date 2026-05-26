package com.github.alexthe666.alexsmobs.entity.ai;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityMoveHelper;
import net.minecraft.util.math.MathHelper;

/**
 * 1.12 {@link EntityMoveHelper} counterpart to 1.16 {@code MovementController} flight steering.
 */
public class FlightMoveController extends EntityMoveHelper {

    private final EntityLiving parentEntity;
    private final float speedGeneral;
    private final boolean shouldLookAtTarget;
    private final boolean needsYSupport;

    public FlightMoveController(EntityLiving bird, float speedGeneral, boolean shouldLookAtTarget, boolean needsYSupport) {
        super(bird);
        this.parentEntity = bird;
        this.shouldLookAtTarget = shouldLookAtTarget;
        this.speedGeneral = speedGeneral;
        this.needsYSupport = needsYSupport;
    }

    public FlightMoveController(EntityLiving bird, float speedGeneral, boolean shouldLookAtTarget) {
        this(bird, speedGeneral, shouldLookAtTarget, false);
    }

    public FlightMoveController(EntityLiving bird, float speedGeneral) {
        this(bird, speedGeneral, true);
    }

    @Override
    public void onUpdateMoveHelper() {
        if (this.isUpdating()) {
            double tx = this.getX() - parentEntity.posX;
            double ty = this.getY() - parentEntity.posY;
            double tz = this.getZ() - parentEntity.posZ;
            double d0 = MathHelper.sqrt(tx * tx + ty * ty + tz * tz);
            double edge = parentEntity.getEntityBoundingBox().getAverageEdgeLength();
            if (d0 < edge) {
                this.action = net.minecraft.entity.ai.EntityMoveHelper.Action.WAIT;
                parentEntity.motionX *= 0.5D;
                parentEntity.motionY *= 0.5D;
                parentEntity.motionZ *= 0.5D;
            } else {
                double sp = this.getSpeed();
                parentEntity.motionX += tx / d0 * sp * speedGeneral * 0.05D;
                parentEntity.motionY += ty / d0 * sp * speedGeneral * 0.05D;
                parentEntity.motionZ += tz / d0 * sp * speedGeneral * 0.05D;
                if (needsYSupport) {
                    double d1 = this.getY() - parentEntity.posY;
                    parentEntity.motionY += (double) parentEntity.getAIMoveSpeed() * speedGeneral * MathHelper.clamp(d1, -1.0D, 1.0D) * 0.6F;
                }
                if (parentEntity.getAttackTarget() == null || !shouldLookAtTarget) {
                    parentEntity.rotationYaw = -((float) MathHelper.atan2(parentEntity.motionX, parentEntity.motionZ)) * (180F / (float) Math.PI);
                    parentEntity.renderYawOffset = parentEntity.rotationYaw;
                } else {
                    EntityLivingBase t = parentEntity.getAttackTarget();
                    double d2 = t.posX - parentEntity.posX;
                    double d3 = t.posZ - parentEntity.posZ;
                    parentEntity.rotationYaw = -((float) MathHelper.atan2(d2, d3)) * (180F / (float) Math.PI);
                    parentEntity.renderYawOffset = parentEntity.rotationYaw;
                }
            }
        }
    }
}
