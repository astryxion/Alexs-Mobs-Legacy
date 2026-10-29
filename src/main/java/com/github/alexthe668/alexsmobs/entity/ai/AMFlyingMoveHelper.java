package com.github.alexthe668.alexsmobs.entity.ai;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityMoveHelper;
import net.minecraft.util.math.MathHelper;

/**
 * 1.12 counterpart of vanilla {@code FlyingMoveControl} / {@code FlyingMovementController}.
 * Flies use this in 1.16 and 1.20. {@link FlightMoveController} keeps adding velocity every tick,
 * which makes them much faster than the original.
 */
public class AMFlyingMoveHelper extends EntityMoveHelper {

    private final int maxTurn;
    private final boolean hoversInPlace;

    public AMFlyingMoveHelper(EntityLiving entity, int maxTurn, boolean hoversInPlace) {
        super(entity);
        this.maxTurn = maxTurn;
        this.hoversInPlace = hoversInPlace;
    }

    @Override
    public void onUpdateMoveHelper() {
        if (this.action == Action.MOVE_TO) {
            this.action = Action.WAIT;
            this.entity.setNoGravity(true);
            double dx = this.posX - this.entity.posX;
            double dy = this.posY - this.entity.posY;
            double dz = this.posZ - this.entity.posZ;
            double distSq = dx * dx + dy * dy + dz * dz;
            if (distSq < 2.500000277905201E-7D) {
                this.entity.setMoveVertical(0.0F);
                this.entity.setMoveForward(0.0F);
                return;
            }
            float yaw = (float) (MathHelper.atan2(dz, dx) * (180D / Math.PI)) - 90.0F;
            this.entity.rotationYaw = this.limitAngle(this.entity.rotationYaw, yaw, 90.0F);
            float speed;
            if (this.entity.onGround) {
                speed = (float) (this.speed * this.entity.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getAttributeValue());
            } else {
                speed = (float) (this.speed * this.entity.getEntityAttribute(SharedMonsterAttributes.FLYING_SPEED).getAttributeValue());
            }
            this.entity.setAIMoveSpeed(speed);
            double horizontal = MathHelper.sqrt(dx * dx + dz * dz);
            if (Math.abs(dy) > 1.0E-5F || Math.abs(horizontal) > 1.0E-5F) {
                float pitch = (float) (-(MathHelper.atan2(dy, horizontal) * (180D / Math.PI)));
                this.entity.rotationPitch = this.limitAngle(this.entity.rotationPitch, pitch, (float) this.maxTurn);
                this.entity.setMoveVertical(dy > 0.0D ? speed : -speed);
            }
        } else {
            if (!this.hoversInPlace) {
                this.entity.setNoGravity(false);
            }
            this.entity.setMoveVertical(0.0F);
            this.entity.setMoveForward(0.0F);
        }
    }
}
