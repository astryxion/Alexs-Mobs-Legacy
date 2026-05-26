package com.github.alexthe666.alexsmobs.entity.util;

import com.github.alexthe666.alexsmobs.entity.EntityLaviathan;
import net.minecraft.util.math.MathHelper;

public class LaviathanNeckSolver {
    private int yawTimer;
    private float yawVariation;
    private int pitchTimer;
    private float pitchVariation;
    private float prevYawVariation;
    private float prevPitchVariation;

    public void resetRotations() {
        this.yawVariation = 0.0F;
        this.pitchVariation = 0.0F;
        this.prevYawVariation = 0.0F;
        this.prevPitchVariation = 0.0F;
    }

  /**
   * Calculates the swing amounts for the given entity (Y axis)
   *
   * @param maxAngle       the furthest this ChainBuffer can swing
   * @param bufferTime     the time it takes to swing this buffer in ticks
   * @param angleDecrement the angle to decrement by for each model piece
   * @param divisor        the amount to divide the swing amount by
   * @param entity         the entity with this ChainBuffer
   */
    public void calcYaw(float maxAngle, int bufferTime, float angleDecrement, float divisor, EntityLaviathan entity) {
        this.prevYawVariation = this.yawVariation;
        if (entity.renderYawOffset != entity.prevRenderYawOffset && Math.abs(this.yawVariation) < maxAngle) {
            this.yawVariation += (entity.prevRenderYawOffset - entity.renderYawOffset) / divisor;
        }
        if (this.yawVariation > 0.7F * angleDecrement) {
            if (this.yawTimer > bufferTime) {
                this.yawVariation -= angleDecrement;
                if (Math.abs(this.yawVariation) < angleDecrement) {
                    this.yawVariation = 0.0F;
                    this.yawTimer = 0;
                }
            } else {
                this.yawTimer++;
            }
        } else if (this.yawVariation < -0.7F * angleDecrement) {
            if (this.yawTimer > bufferTime) {
                this.yawVariation += angleDecrement;
                if (Math.abs(this.yawVariation) < angleDecrement) {
                    this.yawVariation = 0.0F;
                    this.yawTimer = 0;
                }
            } else {
                this.yawTimer++;
            }
        }
    }

    public void calcPitch(float maxAngle, int bufferTime, float angleDecrement, float divisor, EntityLaviathan entity) {
        this.prevPitchVariation = this.pitchVariation;
        if (entity.rotationPitch != entity.prevRotationPitch && Math.abs(this.pitchVariation) < maxAngle) {
            this.pitchVariation += (entity.prevRotationPitch - entity.rotationPitch) / divisor;
        }
        if (this.pitchVariation > 0.7F * angleDecrement) {
            if (this.pitchTimer > bufferTime) {
                this.pitchVariation -= angleDecrement;
                if (Math.abs(this.pitchVariation) < angleDecrement) {
                    this.pitchVariation = 0.0F;
                    this.pitchTimer = 0;
                }
            } else {
                this.pitchTimer++;
            }
        } else if (this.pitchVariation < -0.7F * angleDecrement) {
            if (this.pitchTimer > bufferTime) {
                this.pitchVariation += angleDecrement;
                if (Math.abs(this.pitchVariation) < angleDecrement) {
                    this.pitchVariation = 0.0F;
                    this.pitchTimer = 0;
                }
            } else {
                this.pitchTimer++;
            }
        }
    }

    public float getYawVariation(float partialTick) {
        if (partialTick == 0) {
            return this.yawVariation;
        } else {
            return this.prevYawVariation + (this.yawVariation - this.prevYawVariation) * partialTick;
        }
    }

    public float getPitchVariation(float partialTick) {
        if (partialTick == 0) {
            return this.pitchVariation;
        } else {
            return this.prevPitchVariation + (this.pitchVariation - this.prevPitchVariation) * partialTick;
        }
    }
}
