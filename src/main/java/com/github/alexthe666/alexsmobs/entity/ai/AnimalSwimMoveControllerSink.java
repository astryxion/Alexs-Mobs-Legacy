package com.github.alexthe666.alexsmobs.entity.ai;

import com.github.alexthe666.alexsmobs.entity.EntityMimicOctopus;
import com.github.alexthe666.alexsmobs.entity.ISemiAquatic;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityMoveHelper;
import net.minecraft.util.math.MathHelper;

public class AnimalSwimMoveControllerSink extends EntityMoveHelper {
    private final EntityLiving entity;
    private final float speedMulti;
    private final float ySpeedMod;
    private final float yawLimit;

    public AnimalSwimMoveControllerSink(EntityLiving entity, float speedMulti, float ySpeedMod) {
        this(entity, speedMulti, ySpeedMod, 10.0F);
    }

    public AnimalSwimMoveControllerSink(EntityLiving entity, float speedMulti, float ySpeedMod, float yawLimit) {
        super(entity);
        this.entity = entity;
        this.speedMulti = speedMulti;
        this.ySpeedMod = ySpeedMod;
        this.yawLimit = yawLimit;
    }

    @Override
    public void onUpdateMoveHelper() {
        if (entity instanceof ISemiAquatic && ((ISemiAquatic) entity).shouldStopMoving()) {
            this.entity.setAIMoveSpeed(0.0F);
            return;
        }
        if (this.action == Action.MOVE_TO && !this.entity.getNavigator().noPath()) {
            double dx = this.posX - this.entity.posX;
            double dy = this.posY - this.entity.posY;
            double dz = this.posZ - this.entity.posZ;
            double distSq = dx * dx + dy * dy + dz * dz;
            if (distSq < 2.500000277905201E-7D) {
                this.entity.moveForward = 0.0F;
            } else {
                float yaw = (float) (MathHelper.atan2(dz, dx) * (180D / Math.PI)) - 90.0F;
                this.entity.rotationYaw = this.limitAngle(this.entity.rotationYaw, yaw, yawLimit);
                this.entity.renderYawOffset = this.entity.rotationYaw;
                this.entity.rotationYawHead = this.entity.rotationYaw;
                float moveSpeed = (float) (this.speed * speedMulti * 3.0D * this.entity.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getAttributeValue());
                if (this.entity.isInWater()) {
                    if (dy > 0.0D && this.entity.collidedHorizontally) {
                        this.entity.motionY += 0.08D;
                    } else {
                        this.entity.motionY += (double) this.entity.getAIMoveSpeed() * dy * 0.6D * ySpeedMod;
                    }
                    this.entity.setAIMoveSpeed(moveSpeed * 0.02F);
                    float pitch = -((float) (MathHelper.atan2(dy, MathHelper.sqrt(dx * dx + dz * dz)) * (180D / Math.PI)));
                    pitch = MathHelper.clamp(MathHelper.wrapDegrees(pitch), -85.0F, 85.0F);
                    this.entity.rotationPitch = this.limitAngle(this.entity.rotationPitch, pitch, 5.0F);
                    float cos = MathHelper.cos(this.entity.rotationPitch * 0.017453292F);
                    float sin = MathHelper.sin(this.entity.rotationPitch * 0.017453292F);
                    this.entity.moveForward = cos * moveSpeed;
                    this.entity.moveVertical = -sin * moveSpeed;
                } else {
                    this.entity.setAIMoveSpeed(moveSpeed * 0.1F);
                }
            }
        } else {
            if (entity instanceof EntityMimicOctopus && !entity.onGround) {
                this.entity.motionY -= 0.02D;
            }
            this.entity.setAIMoveSpeed(0.0F);
            this.entity.moveStrafing = 0.0F;
            this.entity.moveVertical = 0.0F;
            this.entity.moveForward = 0.0F;
        }
    }
}
