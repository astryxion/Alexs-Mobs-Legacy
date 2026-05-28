package com.github.alexthe666.alexsmobs.entity.ai;

import com.github.alexthe666.alexsmobs.misc.AMTagRegistry;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;

/**
 * {@link net.minecraft.entity.ai.EntityAITempt} for roadrunners, but ignores creative/spectator players.
 */
public class RoadrunnerAITempt extends EntityAIBase {

    private final EntityCreature temptedEntity;
    private final double speed;
    private double targetX;
    private double targetY;
    private double targetZ;
    private double pitch;
    private double yaw;
    private EntityPlayer temptingPlayer;
    private int delayTemptCounter;
    private final boolean scaredByPlayerMovement;

    public RoadrunnerAITempt(EntityCreature temptedEntityIn, double speedIn, boolean scaredByPlayerMovementIn) {
        this.temptedEntity = temptedEntityIn;
        this.speed = speedIn;
        this.scaredByPlayerMovement = scaredByPlayerMovementIn;
        this.setMutexBits(3);
    }

    private static boolean isNonCombatPlayer(EntityPlayer player) {
        return player.capabilities.isCreativeMode
                || (player instanceof EntityPlayerMP && ((EntityPlayerMP) player).isSpectator());
    }

    private static boolean isTempting(ItemStack stack) {
        return !stack.isEmpty() && AMTagRegistry.itemInTag(AMTagRegistry.INSECT_ITEMS, stack.getItem());
    }

    @Override
    public boolean shouldExecute() {
        if (this.delayTemptCounter > 0) {
            --this.delayTemptCounter;
            return false;
        }
        this.temptingPlayer = this.temptedEntity.world.getClosestPlayerToEntity(this.temptedEntity, 10.0D);
        if (this.temptingPlayer == null || isNonCombatPlayer(this.temptingPlayer)) {
            return false;
        }
        return isTempting(this.temptingPlayer.getHeldItemMainhand()) || isTempting(this.temptingPlayer.getHeldItemOffhand());
    }

    @Override
    public boolean shouldContinueExecuting() {
        if (this.scaredByPlayerMovement) {
            if (this.temptedEntity.getDistanceSq(this.temptingPlayer) < 36.0D) {
                if (this.temptingPlayer.getDistanceSq(this.targetX, this.targetY, this.targetZ) > 0.010000000000000002D) {
                    return false;
                }
                if (Math.abs(this.temptingPlayer.rotationPitch - this.pitch) > 5.0D || Math.abs(this.temptingPlayer.rotationYaw - this.yaw) > 5.0D) {
                    return false;
                }
            } else {
                this.targetX = this.temptingPlayer.posX;
                this.targetY = this.temptingPlayer.posY;
                this.targetZ = this.temptingPlayer.posZ;
            }
            this.pitch = this.temptingPlayer.rotationPitch;
            this.yaw = this.temptingPlayer.rotationYaw;
        }
        return this.shouldExecute();
    }

    @Override
    public void startExecuting() {
        this.targetX = this.temptingPlayer.posX;
        this.targetY = this.temptingPlayer.posY;
        this.targetZ = this.temptingPlayer.posZ;
    }

    @Override
    public void resetTask() {
        this.temptingPlayer = null;
        this.temptedEntity.getNavigator().clearPath();
        this.delayTemptCounter = 100;
    }

    @Override
    public void updateTask() {
        this.temptedEntity.getLookHelper().setLookPositionWithEntity(this.temptingPlayer,
                (float) (this.temptedEntity.getHorizontalFaceSpeed() + 20), (float) this.temptedEntity.getVerticalFaceSpeed());
        if (this.temptedEntity.getDistanceSq(this.temptingPlayer) < 6.25D) {
            this.temptedEntity.getNavigator().clearPath();
        } else {
            this.temptedEntity.getNavigator().tryMoveToEntityLiving(this.temptingPlayer, this.speed);
        }
    }
}
