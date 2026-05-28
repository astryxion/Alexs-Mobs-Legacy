package com.github.alexthe666.alexsmobs.entity.ai;

import com.github.alexthe666.alexsmobs.entity.EntityRaccoon;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.player.EntityPlayer;

public class RaccoonAIBeg extends EntityAIBase {
    protected final EntityRaccoon raccoon;
    private final double speed;
    protected EntityPlayer closestPlayer;
    private int delayTemptCounter;
    private boolean isRunning;

    public RaccoonAIBeg(EntityRaccoon raccoon, double speed) {
        this.raccoon = raccoon;
        this.speed = speed;
        this.setMutexBits(3);
    }

    @Override
    public boolean shouldExecute() {
        if (this.delayTemptCounter > 0) {
            --this.delayTemptCounter;
            return false;
        }
        if (!this.raccoon.getHeldItemMainhand().isEmpty()) {
            return false;
        }
        this.closestPlayer = this.raccoon.world.getClosestPlayerToEntity(this.raccoon, 32.0D);
        if (this.closestPlayer == null) {
            return false;
        }
        return EntityRaccoon.isRaccoonFood(this.closestPlayer.getHeldItemMainhand()) || EntityRaccoon.isRaccoonFood(this.closestPlayer.getHeldItemOffhand());
    }

    @Override
    public boolean shouldContinueExecuting() {
        return this.raccoon.getHeldItemMainhand().isEmpty() && this.shouldExecute();
    }

    @Override
    public void startExecuting() {
        this.isRunning = true;
    }

    @Override
    public void resetTask() {
        this.closestPlayer = null;
        this.raccoon.getNavigator().clearPath();
        this.delayTemptCounter = 100;
        this.raccoon.setBegging(false);
        this.isRunning = false;
    }

    @Override
    public void updateTask() {
        this.raccoon.getLookHelper().setLookPositionWithEntity(this.closestPlayer, this.raccoon.getHorizontalFaceSpeed() + 20, (float) this.raccoon.getVerticalFaceSpeed());
        if (this.raccoon.getDistanceSq(this.closestPlayer) < 12.0D) {
            this.raccoon.getNavigator().clearPath();
            this.raccoon.setBegging(true);
        } else {
            this.raccoon.getNavigator().tryMoveToEntityLiving(this.closestPlayer, this.speed);
        }
    }

    public boolean isRunning() {
        return this.isRunning;
    }
}
