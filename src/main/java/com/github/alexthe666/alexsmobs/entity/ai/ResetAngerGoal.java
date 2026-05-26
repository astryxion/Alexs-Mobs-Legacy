package com.github.alexthe666.alexsmobs.entity.ai;

import com.github.alexthe666.alexsmobs.entity.IAngerable;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;

/**
 * 1.16 {@code net.minecraft.entity.ai.goal.ResetAngerGoal} behavior for Forge 1.12.2.
 */
public class ResetAngerGoal extends EntityAIBase {

    private final IAngerable anger;
    private final EntityLiving entity;
    private final boolean alertOthersOfSameType;

    public ResetAngerGoal(EntityLiving entity, boolean alertOthersOfSameType) {
        if (!(entity instanceof IAngerable)) {
            throw new IllegalArgumentException("ResetAngerGoal requires IAngerable: " + entity);
        }
        this.entity = entity;
        this.anger = (IAngerable) entity;
        this.alertOthersOfSameType = alertOthersOfSameType;
    }

    @Override
    public boolean shouldExecute() {
        return anger.isAngry() && !anger.wasHurtByPlayerRecently();
    }

    @Override
    public void startExecuting() {
        anger.setAngerTime(0);
        anger.setAngerTarget(null);
        entity.setAttackTarget(null);
        entity.setRevengeTarget(null);
        if (this.alertOthersOfSameType) {
            double r = 20.0D;
            for (EntityLivingBase other : this.entity.world.getEntitiesWithinAABB(
                    EntityLivingBase.class,
                    this.entity.getEntityBoundingBox().grow(r, 6.0D, r))) {
                if (other != this.entity
                        && other.getClass() == this.entity.getClass()
                        && other instanceof IAngerable
                        && this.entity.canEntityBeSeen(other)
                        && !((IAngerable) other).isAngry()) {
                    ((IAngerable) other).func_230258_H__();
                }
            }
        }
    }
}
