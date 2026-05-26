package com.github.alexthe666.alexsmobs.entity.ai;

import com.github.alexthe666.alexsmobs.entity.EntityCaiman;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.Vec3d;

public class CaimanAIMelee extends EntityAIBase {
    private final EntityCaiman caiman;
    private int grabTime = 0;

    public CaimanAIMelee(EntityCaiman caiman) {
        this.caiman = caiman;
        this.setMutexBits(3);
    }

    @Override
    public boolean shouldExecute() {
        EntityLivingBase target = this.caiman.getAttackTarget();
        return target != null && target.isEntityAlive();
    }

    @Override
    public void resetTask() {
        this.caiman.setHeldMobId(-1);
        this.grabTime = 0;
    }

    @Override
    public void updateTask() {
        if (this.grabTime < 0) {
            this.grabTime++;
        }
        EntityLivingBase target = this.caiman.getAttackTarget();
        if (target == null) {
            return;
        }
        double bbWidth = (this.caiman.width + target.width) / 2D;
        double dist = this.caiman.getDistance(target);
        boolean flag = false;
        if (dist < bbWidth + 2F) {
            if (this.grabTime >= 0) {
                if (this.grabTime % 25 == 0) {
                    DamageSource source = this.caiman.isTamed() ? DamageSource.DROWN : DamageSource.causeMobDamage(this.caiman);
                    target.attackEntityFrom(source, (float) this.caiman.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue());
                }
                this.grabTime++;
                Vec3d shakePreyPos = this.caiman.getShakePreyPos();
                Vec3d minus = new Vec3d(shakePreyPos.x - target.posX, 0, shakePreyPos.z - target.posZ).normalize();
                target.motionX = target.motionX * 0.6D + minus.x * 0.35D;
                target.motionZ = target.motionZ * 0.6D + minus.z * 0.35D;
                flag = true;
                if (this.grabTime > this.getGrabDuration()) {
                    this.grabTime = -10;
                }
            }
        }
        this.caiman.setHeldMobId(flag ? target.getEntityId() : -1);
        if (dist > bbWidth && !flag) {
            this.caiman.getLookHelper().setLookPositionWithEntity(target, 30.0F, 30.0F);
            this.caiman.getNavigator().tryMoveToEntityLiving(target, 1.2D);
        }
    }

    private int getGrabDuration() {
        if (this.caiman.isTamed() && this.caiman.tameAttackFlag) {
            return 300;
        }
        return 2;
    }
}
