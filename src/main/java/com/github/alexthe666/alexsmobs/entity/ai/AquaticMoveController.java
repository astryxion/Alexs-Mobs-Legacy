package com.github.alexthe666.alexsmobs.entity.ai;

import com.github.alexthe666.alexsmobs.entity.EntityGiantSquid;
import com.github.alexthe666.alexsmobs.entity.EntityWarpedToad;
import com.github.alexthe666.alexsmobs.entity.ISemiAquatic;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityMoveHelper;
import net.minecraft.util.math.MathHelper;

/** 1.12 counterpart to 1.16 aquatic {@code MovementController} for fish / water mobs. */
public class AquaticMoveController extends EntityMoveHelper {
    private final EntityLiving entity;
    private final float speedMulti;
    private final float yawLimit;

    public AquaticMoveController(EntityLiving entity, float speedMulti) {
        super(entity);
        this.entity = entity;
        this.speedMulti = speedMulti;
        this.yawLimit = 3.0F;
    }

    public AquaticMoveController(EntityLiving entity, float speedMulti, float yawLimit) {
        super(entity);
        this.entity = entity;
        this.speedMulti = speedMulti;
        this.yawLimit = yawLimit;
    }

    @Override
    public void onUpdateMoveHelper() {
        if (this.entity.isInWater() || this.entity instanceof EntityWarpedToad && this.entity.isInLava()) {
            this.entity.motionY += 0.005D;
        }
        if (this.entity instanceof ISemiAquatic && ((ISemiAquatic) this.entity).shouldStopMoving()) {
            this.entity.setAIMoveSpeed(0.0F);
            return;
        }
        if (this.action == Action.MOVE_TO && !this.entity.getNavigator().noPath()) {
            double d0 = this.posX - this.entity.posX;
            double d1 = this.posY - this.entity.posY;
            double d2 = this.posZ - this.entity.posZ;
            double d3 = MathHelper.sqrt(d0 * d0 + d1 * d1 + d2 * d2);
            d1 /= d3;
            float f = (float) (MathHelper.atan2(d2, d0) * (180D / Math.PI)) - 90.0F;
            if (entity instanceof EntityGiantSquid) {
                ((EntityGiantSquid) entity).directPitch(d0, d1, d2, d3);
            } else {
                this.entity.rotationYaw = this.limitAngle(this.entity.rotationYaw, f, this.yawLimit);
                this.entity.renderYawOffset = this.entity.rotationYaw;
            }
            float speedAttr = (float) this.entity.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getAttributeValue();
            float f1 = (float) (this.speed * speedAttr * this.speedMulti);
            this.entity.setAIMoveSpeed(f1 * 0.4F);
            this.entity.motionY += (double) this.entity.getAIMoveSpeed() * d1 * 0.6D;
        } else {
            this.entity.setAIMoveSpeed(0.0F);
        }
    }
}
