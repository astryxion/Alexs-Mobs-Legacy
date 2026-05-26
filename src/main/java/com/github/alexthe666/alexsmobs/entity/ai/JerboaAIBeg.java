package com.github.alexthe666.alexsmobs.entity.ai;

import com.github.alexthe666.alexsmobs.entity.EntityJerboa;
import com.github.alexthe666.alexsmobs.misc.AMTagRegistry;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

public class JerboaAIBeg extends EntityAIBase {
    protected final EntityJerboa jerboa;
    private final double speed;
    protected EntityPlayer closestPlayer;
    private int delayTemptCounter;
    private boolean isRunning;

    public JerboaAIBeg(EntityJerboa jerboa, double speed) {
        this.jerboa = jerboa;
        this.speed = speed;
        this.setMutexBits(3);
    }

    @Override
    public boolean shouldExecute() {
        if (this.delayTemptCounter > 0) {
            --this.delayTemptCounter;
            return false;
        }
        if (this.jerboa.isInLove()) {
            return false;
        }
        this.closestPlayer = this.jerboa.world.getClosestPlayerToEntity(this.jerboa, 32.0D);
        if (this.closestPlayer == null) {
            return false;
        }
        return isFood(this.closestPlayer.getHeldItemMainhand()) || isFood(this.closestPlayer.getHeldItemOffhand());
    }

    private boolean isFood(ItemStack stack) {
        return !stack.isEmpty() && (AMTagRegistry.itemInTag(AMTagRegistry.JERBOA_BEGS_FOR, stack.getItem()) || jerboa.isBreedingItem(stack));
    }

    @Override
    public boolean shouldContinueExecuting() {
        return this.jerboa.getHeldItemMainhand().isEmpty() && this.shouldExecute() && !this.jerboa.isInLove();
    }

    @Override
    public void startExecuting() {
        this.isRunning = true;
    }

    @Override
    public void resetTask() {
        this.closestPlayer = null;
        this.jerboa.getNavigator().clearPath();
        this.delayTemptCounter = 100;
        this.jerboa.setBegging(false);
        this.isRunning = false;
    }

    @Override
    public void updateTask() {
        this.jerboa.getLookHelper().setLookPositionWithEntity(this.closestPlayer, 30.0F, (float) this.jerboa.getVerticalFaceSpeed());
        if (this.jerboa.getDistanceSq(this.closestPlayer) < 12.0D) {
            this.jerboa.getNavigator().clearPath();
            this.jerboa.setBegging(true);
        } else {
            this.jerboa.getNavigator().tryMoveToEntityLiving(this.closestPlayer, this.speed);
        }
    }

    public boolean isRunning() {
        return this.isRunning;
    }
}
