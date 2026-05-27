package com.github.alexthe666.alexsmobs.entity.ai;

import com.google.common.base.Predicate;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.util.math.AxisAlignedBB;

import javax.annotation.Nullable;

public class EntityAINearestTarget3D extends EntityAINearestAttackableTarget {

    public EntityAINearestTarget3D(EntityCreature goalOwnerIn, Class<?> targetClassIn, boolean checkSight) {
        super(goalOwnerIn, targetClassIn, checkSight);
    }

    public EntityAINearestTarget3D(EntityCreature goalOwnerIn, Class<?> targetClassIn, boolean checkSight, boolean onlyNearby) {
        super(goalOwnerIn, targetClassIn, checkSight, onlyNearby);
    }

    public EntityAINearestTarget3D(EntityCreature goalOwnerIn, Class<?> targetClassIn, int targetChanceIn, boolean checkSight, boolean onlyNearby, @Nullable Predicate<? super EntityLivingBase> targetPredicate) {
        super(goalOwnerIn, targetClassIn, targetChanceIn, checkSight, onlyNearby, targetPredicate);
    }

    @Override
    protected AxisAlignedBB getTargetableArea(double targetDistance) {
        return this.taskOwner.getEntityBoundingBox().grow(targetDistance, targetDistance, targetDistance);
    }
}
