package com.github.alexthe666.alexsmobs.entity.ai;

import com.github.alexthe666.alexsmobs.entity.ITargetsDroppedItems;
import com.google.common.base.Predicate;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.AxisAlignedBB;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class CreatureAITargetItems extends EntityAIBase {

    protected final Sorter theNearestAttackableTargetSorter;
    protected final Predicate<EntityItem> targetEntitySelector;
    protected int executionChance;
    protected boolean mustUpdate;
    protected EntityItem targetEntity;
    private final ITargetsDroppedItems hunter;
    private final EntityCreature creature;
    private int tickThreshold;
    private float radius = 9F;
    private int walkCooldown = 0;

    public CreatureAITargetItems(EntityCreature creature, boolean checkSight) {
        this(creature, checkSight, false);
    }

    public CreatureAITargetItems(EntityCreature creature, boolean checkSight, int tickThreshold) {
        this(creature, checkSight, false, tickThreshold, 9);
    }

    public CreatureAITargetItems(EntityCreature creature, boolean checkSight, boolean onlyNearby) {
        this(creature, 10, checkSight, onlyNearby, null, 0);
    }

    public CreatureAITargetItems(EntityCreature creature, boolean checkSight, boolean onlyNearby, int tickThreshold, int radius) {
        this(creature, 10, checkSight, onlyNearby, null, tickThreshold);
        this.radius = radius;
    }

    public CreatureAITargetItems(EntityCreature creature, int chance, boolean checkSight, boolean onlyNearby, @Nullable final Predicate<? super EntityItem> targetSelector, int ticksExisted) {
        this.creature = creature;
        this.executionChance = chance;
        this.tickThreshold = ticksExisted;
        this.hunter = (ITargetsDroppedItems) creature;
        this.theNearestAttackableTargetSorter = new Sorter(creature);
        this.targetEntitySelector = item -> {
            if (item == null) {
                return false;
            }
            ItemStack stack = item.getItem();
            if (targetSelector != null && !targetSelector.apply(item)) {
                return false;
            }
            return !stack.isEmpty() && hunter.canTargetItem(stack) && item.ticksExisted > tickThreshold;
        };
        this.setMutexBits(1);
    }

    @Override
    public boolean shouldExecute() {
        if (this.creature.isRiding() || this.creature.isBeingRidden()) {
            return false;
        }
        if (!this.creature.getHeldItemMainhand().isEmpty()) {
            return false;
        }
        if (!this.mustUpdate) {
            long worldTime = this.creature.world.getTotalWorldTime() % 10;
            if (this.creature.getIdleTime() >= 100 && worldTime != 0) {
                return false;
            }
            if (this.creature.getRNG().nextInt(this.executionChance) != 0 && worldTime != 0) {
                return false;
            }
        }
        List<EntityItem> list = this.creature.world.getEntitiesWithinAABB(EntityItem.class, this.getTargetableArea(this.getTargetDistance()), this.targetEntitySelector);
        if (list.isEmpty()) {
            return false;
        } else {
            Collections.sort(list, this.theNearestAttackableTargetSorter);
            this.targetEntity = list.get(0);
            this.mustUpdate = false;
            this.hunter.onFindTarget(targetEntity);
            return true;
        }
    }

    protected double getTargetDistance() {
        return 16D;
    }

    protected AxisAlignedBB getTargetableArea(double targetDistance) {
        return new AxisAlignedBB(-radius, -radius, -radius, radius, radius, radius)
                .offset(this.creature.posX + 0.5D, this.creature.posY + 0.5D, this.creature.posZ + 0.5D);
    }

    @Override
    public void startExecuting() {
        moveTo();
    }

    protected void moveTo() {
        if (walkCooldown > 0) {
            walkCooldown--;
        } else {
            this.creature.getNavigator().tryMoveToXYZ(this.targetEntity.posX, this.targetEntity.posY, this.targetEntity.posZ, 1.0D);
            walkCooldown = 30 + this.creature.getRNG().nextInt(40);
        }
    }

    @Override
    public void resetTask() {
        this.creature.getNavigator().clearPath();
        this.targetEntity = null;
    }

    @Override
    public void updateTask() {
        if (this.targetEntity == null || !this.targetEntity.isEntityAlive()) {
            this.resetTask();
            this.creature.getNavigator().clearPath();
        } else {
            moveTo();
        }
        if (targetEntity != null && this.creature.canEntityBeSeen(targetEntity) && this.creature.width > 2D && this.creature.onGround) {
            this.creature.getMoveHelper().setMoveTo(targetEntity.posX, targetEntity.posY, targetEntity.posZ, 1.0D);
        }
        if (targetEntity != null && targetEntity.isEntityAlive()
                && this.creature.getDistanceSq(targetEntity) < this.hunter.getMaxDistToItem()
                && this.creature.getHeldItemMainhand().isEmpty()) {
            hunter.onGetItem(targetEntity);
            this.targetEntity.getItem().shrink(1);
            resetTask();
        }
    }

    public void makeUpdate() {
        this.mustUpdate = true;
    }

    @Override
    public boolean shouldContinueExecuting() {
        boolean path = this.creature.width > 2D || !this.creature.getNavigator().noPath();
        return path && targetEntity != null && targetEntity.isEntityAlive();
    }

    public static class Sorter implements Comparator<Entity> {
        private final Entity theEntity;

        public Sorter(Entity theEntityIn) {
            this.theEntity = theEntityIn;
        }

        public int compare(Entity p_compare_1_, Entity p_compare_2_) {
            double d0 = this.theEntity.getDistanceSq(p_compare_1_);
            double d1 = this.theEntity.getDistanceSq(p_compare_2_);
            return Double.compare(d0, d1);
        }
    }
}
