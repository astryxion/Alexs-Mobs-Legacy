package com.github.alexthe666.alexsmobs.entity.ai;

import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.AxisAlignedBB;

import java.util.Iterator;
import java.util.List;

public class TameableAIHurtByTarget extends EntityAIBase {

    private final EntityCreature creature;
    private boolean entityCallsForHelp;
    private int revengeTimerOld;
    private final Class<?>[] excludedReinforcementTypes;
    private Class<?>[] reinforcementTypes;

    public TameableAIHurtByTarget(EntityCreature creatureIn, Class<?>... excludeReinforcementTypes) {
        this.creature = creatureIn;
        this.excludedReinforcementTypes = excludeReinforcementTypes;
        this.setMutexBits(1);
    }

    @Override
    public boolean shouldExecute() {
        int i = this.creature.getRevengeTimer();
        EntityLivingBase revengeTarget = this.creature.getRevengeTarget();
        if (i != this.revengeTimerOld && revengeTarget != null) {
            for (Class<?> oclass : this.excludedReinforcementTypes) {
                if (oclass.isAssignableFrom(revengeTarget.getClass())) {
                    return false;
                }
            }
            return this.isSuitableTarget(revengeTarget);
        } else {
            return false;
        }
    }

    public TameableAIHurtByTarget setCallsForHelp(Class<?>... reinforcementTypesIn) {
        this.entityCallsForHelp = true;
        this.reinforcementTypes = reinforcementTypesIn;
        return this;
    }

    @Override
    public void startExecuting() {
        this.creature.setAttackTarget(this.creature.getRevengeTarget());
        this.revengeTimerOld = this.creature.getRevengeTimer();
        if (this.entityCallsForHelp) {
            this.alertOthers();
        }
    }

    protected void alertOthers() {
        double d0 = this.getTargetDistance();
        AxisAlignedBB axisalignedbb = this.creature.getEntityBoundingBox().grow(d0, 10.0D, d0);
        List<? extends EntityCreature> list = this.creature.world.getEntitiesWithinAABB(this.creature.getClass(), axisalignedbb);
        Iterator<? extends EntityCreature> iterator = list.iterator();

        while (true) {
            EntityCreature mobentity;
            while (true) {
                if (!iterator.hasNext()) {
                    return;
                }

                mobentity = iterator.next();
                if (this.creature != mobentity && mobentity.getAttackTarget() == null && (!(this.creature instanceof EntityTameable) || ((EntityTameable) this.creature).getOwner() == ((EntityTameable) mobentity).getOwner()) && !mobentity.isOnSameTeam(this.creature.getRevengeTarget())) {
                    if (this.reinforcementTypes == null) {
                        break;
                    }

                    boolean flag = false;

                    for (Class<?> oclass : this.reinforcementTypes) {
                        if (mobentity.getClass() == oclass) {
                            flag = true;
                            break;
                        }
                    }

                    if (!flag) {
                        break;
                    }
                }
            }

            this.setAttackTarget(mobentity, this.creature.getRevengeTarget());
        }
    }

    protected void setAttackTarget(EntityCreature mobIn, EntityLivingBase targetIn) {
        mobIn.setAttackTarget(targetIn);
    }

    private boolean isSuitableTarget(EntityLivingBase target) {
        return target.isEntityAlive() && this.creature.canEntityBeSeen(target);
    }

    private double getTargetDistance() {
        return this.creature instanceof EntityMob ? ((EntityMob) this.creature).getEntityAttribute(net.minecraft.entity.SharedMonsterAttributes.FOLLOW_RANGE).getAttributeValue() : 16.0D;
    }
}
