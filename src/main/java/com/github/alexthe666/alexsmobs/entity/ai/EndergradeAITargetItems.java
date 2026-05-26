package com.github.alexthe666.alexsmobs.entity.ai;

import com.github.alexthe666.alexsmobs.entity.EntityEndergrade;
import com.google.common.base.Predicate;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class EndergradeAITargetItems extends EntityAIBase {

    protected final Sorter theNearestAttackableTargetSorter;
    protected final Predicate<EntityItem> targetEntitySelector;
    protected int executionChance;
    protected boolean mustUpdate;
    protected EntityItem targetEntity;
    private final EntityEndergrade endergrade;

    public EndergradeAITargetItems(EntityEndergrade creature, boolean checkSight) {
        this(creature, checkSight, false);
    }

    public EndergradeAITargetItems(EntityEndergrade creature, boolean checkSight, boolean onlyNearby) {
        this(creature, 10, checkSight, onlyNearby);
    }

    public EndergradeAITargetItems(EntityEndergrade creature, int chance, boolean checkSight, boolean onlyNearby) {
        this.endergrade = creature;
        this.executionChance = chance;
        this.theNearestAttackableTargetSorter = new Sorter(creature);
        this.targetEntitySelector = item -> {
            ItemStack stack = item.getItem();
            return !stack.isEmpty() && endergrade.canTargetItem(stack);
        };
        this.setMutexBits(1);
    }

    @Override
    public boolean shouldExecute() {
        if (this.endergrade.isRiding() || (this.endergrade.isBeingRidden() && this.endergrade.getControllingPassenger() != null)) {
            return false;
        }
        if (!this.endergrade.getHeldItemMainhand().isEmpty()) {
            return false;
        }
        if (!this.mustUpdate) {
            long worldTime = this.endergrade.world.getTotalWorldTime() % 10;
            if (this.endergrade.getIdleTime() >= 100 && worldTime != 0) {
                return false;
            }
            if (this.endergrade.getRNG().nextInt(this.executionChance) != 0 && worldTime != 0) {
                return false;
            }
        }
        List<EntityItem> list = this.endergrade.world.getEntitiesWithinAABB(EntityItem.class, this.getTargetableArea(this.getTargetDistance()), this.targetEntitySelector);
        if (list.isEmpty()) {
            return false;
        } else {
            Collections.sort(list, this.theNearestAttackableTargetSorter);
            this.targetEntity = list.get(0);
            this.endergrade.stopWandering = true;
            this.endergrade.hasItemTarget = true;
            this.mustUpdate = false;
            return true;
        }
    }

    protected double getTargetDistance() {
        return 16.0D;
    }

    protected AxisAlignedBB getTargetableArea(double targetDistance) {
        return new AxisAlignedBB(-9, -9, -9, 9, 9, 9)
                .offset(this.endergrade.posX + 0.5D, this.endergrade.posY + 0.5D, this.endergrade.posZ + 0.5D);
    }

    @Override
    public void startExecuting() {
        this.endergrade.getMoveHelper().setMoveTo(this.targetEntity.posX, this.targetEntity.posY, this.targetEntity.posZ, 1.0D);
    }

    @Override
    public void resetTask() {
        this.targetEntity = null;
        this.endergrade.hasItemTarget = false;
        this.endergrade.stopWandering = false;
    }

    public void makeUpdate() {
        this.mustUpdate = true;
    }

    @Override
    public void updateTask() {
        if (this.targetEntity == null || !this.targetEntity.isEntityAlive()) {
            this.resetTask();
        } else {
            this.endergrade.getMoveHelper().setMoveTo(this.targetEntity.posX, this.targetEntity.posY, this.targetEntity.posZ, 1.0D);
        }
        if (this.targetEntity != null && this.targetEntity.isEntityAlive()
                && this.endergrade.getDistanceSq(this.targetEntity) < 2.0D
                && this.endergrade.getHeldItemMainhand().isEmpty()) {
            ItemStack duplicate = this.targetEntity.getItem().copy();
            endergrade.bite();
            duplicate.setCount(1);
            if (!this.endergrade.getHeldItemMainhand().isEmpty() && !this.endergrade.world.isRemote) {
                this.endergrade.entityDropItem(this.endergrade.getHeldItemMainhand(), 0.0F);
            }
            this.endergrade.setHeldItem(EnumHand.MAIN_HAND, duplicate);
            endergrade.onGetItem(targetEntity);
            this.targetEntity.getItem().shrink(1);
            resetTask();
        }
    }

    @Override
    public boolean shouldContinueExecuting() {
        return this.endergrade.getMoveHelper().isUpdating();
    }

    public static class Sorter implements Comparator<Entity> {
        private final Entity theEntity;

        public Sorter(Entity theEntityIn) {
            this.theEntity = theEntityIn;
        }

        @Override
        public int compare(Entity p_compare_1_, Entity p_compare_2_) {
            return Double.compare(this.theEntity.getDistanceSq(p_compare_1_), this.theEntity.getDistanceSq(p_compare_2_));
        }
    }
}
